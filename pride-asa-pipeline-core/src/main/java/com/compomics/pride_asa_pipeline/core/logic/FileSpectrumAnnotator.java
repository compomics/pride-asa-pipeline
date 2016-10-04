package com.compomics.pride_asa_pipeline.core.logic;

import com.compomics.pride_asa_pipeline.core.cache.ParserCache;
import com.compomics.pride_asa_pipeline.core.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.core.logic.modification.InputType;
import com.compomics.pride_asa_pipeline.core.logic.recalibration.MassRecalibrationResult;
import com.compomics.pride_asa_pipeline.core.service.FileExperimentService;
import com.compomics.pride_asa_pipeline.core.service.FileModificationService;
import com.compomics.pride_asa_pipeline.core.service.FileSpectrumService;
import com.compomics.pride_asa_pipeline.core.util.IOUtils;
import com.compomics.pride_asa_pipeline.core.util.ResourceUtils;
import com.compomics.pride_asa_pipeline.model.MGFExtractionException;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.ModificationHolder;
import com.compomics.pride_asa_pipeline.model.modification.source.PRIDEModificationFactory;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.jdom2.JDOMException;
import org.springframework.core.io.Resource;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.CachedDataAccessController;

/**
 *
 * @author Niels Hulstaert
 */
/**
 *
 * @author Kenneth Verheggen
 */
public class FileSpectrumAnnotator extends AbstractSpectrumAnnotator<File> {

    private static final Logger LOGGER = Logger.getLogger(FileSpectrumAnnotator.class);
    /**
     * Boolean that keeps track of the init state of the identifications file
     */
    private boolean fileLoaded;
    /**
     * Beans
     */
    public FileExperimentService experimentService;
    /**
     * Boolean indicating if the file should be loaded in memory
     */
    private boolean IN_MEMORY = false;

    /**
     * Getters and setters
     *
     * @return the experiment service
     */
    public FileExperimentService getExperimentService() {
        return experimentService;
    }

    public void setExperimentService(FileExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    @Override
    public void initIdentifications(File identificationsFile) {
        try {
            initCachedDataControllers(identificationsFile);
        } catch (IOException | MGFExtractionException ex) {
            LOGGER.error(ex);
        }

        //@todo get a name take makes sense
        String experimentAccession = identificationsFile.getName().substring(0, identificationsFile.getName().indexOf(".xml"));

        areModificationsLoaded = false;

        LOGGER.debug("Creating new SpectrumAnnotatorResult for experiment " + experimentAccession);
        spectrumAnnotatorResult = new SpectrumAnnotatorResult(experimentAccession);

        LOGGER.debug("Loading charge states for experiment " + experimentAccession);
        initChargeStates();

        LOGGER.info("loading identifications for experiment " + experimentAccession);
        loadExperimentIdentifications();
        LOGGER.debug("Finished loading identifications for experiment " + experimentAccession);

        ///////////////////////////////////////////////////////////////////////
        //FIRST STEP: find the systematic mass error (if there is one)
        //get analyzer data
        analyzerData = experimentService.getAnalyzerData();
        LOGGER.info("finding systematic mass errors");
        MassRecalibrationResult massRecalibrationResult = findSystematicMassError(identifications.getCompletePeptides());
        LOGGER.debug("Finished finding systematic mass errors:" + "\n" + massRecalibrationResult.toString());
        spectrumAnnotatorResult.setMassRecalibrationResult(massRecalibrationResult);
    }

    public Set<Modification> initModifications() {
        Set<Modification> prideModifications = new HashSet<>();

        //For the solver we need a ModificationHolder (contains all considered modifications)
        modificationHolder = new ModificationHolder();

        //add the pipeline modifications
        Resource modificationsResource = ResourceUtils.getResourceByRelativePath(PropertiesConfigurationHolder.getInstance().getString("modification.pipeline_modifications_file"));
        try {
            modificationHolder.addModifications(pipelineModificationService.importPipelineModifications(modificationsResource, InputType.PRIDE_ASAP));
        } catch (JDOMException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        //add the modifications found in pride for the given experiment
        if (PropertiesConfigurationHolder.getInstance().getBoolean("spectrumannotator.include_pride_xml_modifications")) {
            prideModifications = ((FileModificationService) modificationService).loadExperimentModifications();
        }

        //update the initialization status
        areModificationsLoaded = true;

        return prideModifications;
    }

    @Override
    public void clearPipeline() {
        areModificationsLoaded = false;
        fileLoaded = false;
        consideredChargeStates = null;
        identifications = null;
        spectrumAnnotatorResult = null;
        analyzerData = null;
        modificationHolder = null;
    }

    @Override
    public void clearTmpResources() {
        experimentService.clear();
        //clear pipeline as well
        clearPipeline();
    }

    /**
     * Private methods
     */
    /**
     * Init the file parser.
     *
     * @param identificationsFile the identifications file
     */
    private void initCachedDataControllers(File identificationsFile) throws IOException, MGFExtractionException {
        //check if the file is gzipped
        //if so, unzip it in the same directory
        String assayIdentifier = identificationsFile.getName();
        if (identificationsFile.getName().endsWith(".gz")) {
            String unzippedFileName = identificationsFile.getName().substring(0, identificationsFile.getName().indexOf(".gz"));
            File unzippedIdentificationsFile = new File(identificationsFile.getParentFile(), unzippedFileName);
            IOUtils.unzip(identificationsFile, unzippedIdentificationsFile);
            identificationsFile = unzippedIdentificationsFile;
        }
        try {
            //init the parser
            CachedDataAccessController parser = ParserCache.getInstance().getParser(assayIdentifier, identificationsFile, IN_MEMORY);

            //set the file parser
            //set PrideXmlParser instance
            if(experimentService instanceof FileExperimentService){
            ((FileExperimentService)experimentService).setActiveAssay(assayIdentifier);
            }
            ((FileSpectrumService) spectrumService).setActiveAssay(assayIdentifier);
            ((FileModificationService) modificationService).setActiveAssay(assayIdentifier);
        } catch (TimeoutException | InterruptedException | ExecutionException ex) {
            java.util.logging.Logger.getLogger(FileSpectrumAnnotator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Loads the experiment identifications.
     *
     * @param identificationsFile the experiment pride XML file
     */
    private void loadExperimentIdentifications() {
        //load the identifications for the given experiment
        identifications = experimentService.loadExperimentIdentifications();
        //update the considered charge states (if necessary)
        experimentService.updateChargeStates(consideredChargeStates);
    }

    @Override
    public Set<Modification> initModifications(Resource modificationsResource, InputType inputType) {
        modificationHolder=new ModificationHolder();
        return new HashSet<>(PRIDEModificationFactory.getAsapMods());
    }
}
