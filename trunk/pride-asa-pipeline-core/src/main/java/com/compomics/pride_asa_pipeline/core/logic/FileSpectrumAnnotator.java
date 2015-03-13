package com.compomics.pride_asa_pipeline.core.logic;

import com.compomics.pride_asa_pipeline.core.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.core.model.MassRecalibrationResult;
import com.compomics.pride_asa_pipeline.core.model.ModificationHolder;
import com.compomics.pride_asa_pipeline.core.model.SpectrumAnnotatorResult;
import com.compomics.pride_asa_pipeline.core.repository.FileParser;
import com.compomics.pride_asa_pipeline.core.repository.factory.FileParserFactory;
import com.compomics.pride_asa_pipeline.core.service.FileExperimentService;
import com.compomics.pride_asa_pipeline.core.service.FileModificationService;
import com.compomics.pride_asa_pipeline.core.service.FileSpectrumService;
import com.compomics.pride_asa_pipeline.core.util.IOUtils;
import com.compomics.pride_asa_pipeline.core.util.ResourceUtils;
import com.compomics.pride_asa_pipeline.model.Modification;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;
import org.jdom2.JDOMException;
import org.springframework.core.io.Resource;

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
     * Boolean that keeps track of the init state of the identifications file.
     */
    private boolean fileLoaded;
    /**
     * Beans.
     */
    private FileExperimentService experimentService;
    private FileParser fileParser;

    /**
     * Getters and setters.
     */
    public FileExperimentService getExperimentService() {
        return experimentService;
    }

    public void setExperimentService(FileExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    @Override
    public void initIdentifications(File identificationsFile) {

        //@todo get a name take makes sense
        String fileName = identificationsFile.getName();
        String experimentAccession = fileName.substring(0, fileName.lastIndexOf("."));

        areModificationsLoaded = false;

        LOGGER.debug("Creating new SpectrumAnnotatorResult for experiment " + experimentAccession);
        spectrumAnnotatorResult = new SpectrumAnnotatorResult(experimentAccession);

        LOGGER.debug("Loading charge states for experiment " + experimentAccession);
        initChargeStates();

        LOGGER.info("loading identifications for experiment " + experimentAccession);
        loadExperimentIdentifications(identificationsFile);
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

    @Override
    public Set<Modification> initModifications() {
        Set<Modification> prideModifications = new HashSet<>();

        //For the solver we need a ModificationHolder (contains all considered modifications)
        modificationHolder = new ModificationHolder();

        //add the pipeline modifications
        Resource modificationsResource = ResourceUtils.getResourceByRelativePath(PropertiesConfigurationHolder.getInstance().getString("modification.pipeline_modifications_file"));
        try {
            Set<Modification> loadPipelineModifications = pipelineModificationService.loadPipelineModifications(modificationsResource);
            modificationHolder.addModifications(loadPipelineModifications);
        } catch (JDOMException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        //add the modifications found in pride for the given experiment
        //   if (PropertiesConfigurationHolder.getInstance().getBoolean("spectrumannotator.include_pride_xml_modifications")) {
        //       prideModifications = ((FileModificationService) modificationService).loadExperimentModifications();
        //   }
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
    public void setFileParser(File identificationsFile, File peakFile) throws Exception {
        //check if the file is gzipped
        //if so, unzip it in the same directory
        if (identificationsFile.getName().endsWith(".gz")) {
            String unzippedFileName = identificationsFile.getName().substring(0, identificationsFile.getName().indexOf(".gz"));
            File unzippedIdentificationsFile = new File(identificationsFile.getParentFile(), unzippedFileName);
            IOUtils.unzip(identificationsFile, unzippedIdentificationsFile);
            identificationsFile = unzippedIdentificationsFile;
        }
        //init the parser
        fileParser = FileParserFactory.getFileParser(identificationsFile);
        fileParser.attachSpectra(peakFile);

        //set the file parser
        //set PrideXmlParser instance
        experimentService.setFileParser(fileParser);
        ((FileSpectrumService) spectrumService).setFileParser(fileParser);
        ((FileModificationService) modificationService).setFileParser(fileParser);
    }

    /**
     * Init the file parser.
     *
     * @param identificationsFile the identifications file
     */
    public void setFileParser(FileParser existingFileParser) {
        this.fileParser = existingFileParser;
        experimentService.setFileParser(fileParser);
        ((FileSpectrumService) spectrumService).setFileParser(fileParser);
        ((FileModificationService) modificationService).setFileParser(fileParser);
    }

    /**
     * Loads the experiment identifications.
     *
     * @param identificationsFile the experiment pride XML file
     */
    private void loadExperimentIdentifications(File identificationsFile) {
        //init the service if necessary
        if (!fileLoaded) {
            experimentService.init(identificationsFile);
        }

        //load the identifications for the given experiment
        identifications = experimentService.loadExperimentIdentifications();
        //update the considered charge states (if necessary)
        experimentService.updateChargeStates(consideredChargeStates);
    }

}
