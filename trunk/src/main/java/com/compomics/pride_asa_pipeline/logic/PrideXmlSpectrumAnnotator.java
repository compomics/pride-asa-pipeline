package com.compomics.pride_asa_pipeline.logic;

import com.compomics.pride_asa_pipeline.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.model.MassRecalibrationResult;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.ModificationHolder;
import com.compomics.pride_asa_pipeline.model.SpectrumAnnotatorResult;
import com.compomics.pride_asa_pipeline.repository.PrideXmlParser;
import com.compomics.pride_asa_pipeline.service.PrideXmlExperimentService;
import com.compomics.pride_asa_pipeline.service.PrideXmlModificationService;
import com.compomics.pride_asa_pipeline.service.PrideXmlSpectrumService;
import com.compomics.pride_asa_pipeline.util.ResourceUtils;
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
public class PrideXmlSpectrumAnnotator extends AbstractSpectrumAnnotator {

    private static final Logger LOGGER = Logger.getLogger(PrideXmlSpectrumAnnotator.class);
    /**
     * Boolean that keeps track of the init state of the pride XML file
     */
    private boolean prideXmlLoaded;
    /**
     * Beans
     */
    private PrideXmlParser prideXmlParser;
    private PrideXmlExperimentService experimentService;

    /**
     * Getters and setters
     */
    public PrideXmlExperimentService getExperimentService() {
        return experimentService;
    }

    public void setExperimentService(PrideXmlExperimentService experimentService) {
        this.experimentService = experimentService;
    }    

    public PrideXmlParser getPrideXmlParser() {
        return prideXmlParser;
    }

    public void setPrideXmlParser(PrideXmlParser prideXmlParser) {
        this.prideXmlParser = prideXmlParser;
    }

    /**
     * Public methods
     */
    public void initBean() {
        //set PrideXmlParser instance
        experimentService.setPrideXmlParser(prideXmlParser);
        ((PrideXmlSpectrumService) spectrumService).setPrideXmlParser(prideXmlParser);
        ((PrideXmlModificationService) modificationService).setPrideXmlParser(prideXmlParser);
    }

    public void initIdentifications(File experimentPrideXmlFile) {
        //@todo get a name take makes sense
        String experimentAccession = experimentPrideXmlFile.getName().substring(0, experimentPrideXmlFile.getName().indexOf(".xml"));

        areModificationsLoaded = false;

        LOGGER.debug("Creating new SpectrumAnnotatorResult for experiment " + experimentAccession);
        spectrumAnnotatorResult = new SpectrumAnnotatorResult(experimentAccession);

        LOGGER.debug("Loading charge states for experiment " + experimentAccession);
        initChargeStates();

        LOGGER.info("loading identifications for experiment " + experimentAccession);
        loadExperimentIdentifications(experimentPrideXmlFile);
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
            modificationHolder.addModifications(modificationService.loadPipelineModifications(modificationsResource));
        } catch (JDOMException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        //add the modifications found in pride for the given experiment
        if (PropertiesConfigurationHolder.getInstance().getBoolean("spectrumannotator.include_pride_xml_modifications")) {
            prideModifications = ((PrideXmlModificationService)modificationService).loadExperimentModifications();
        }

        //update the initialization status
        areModificationsLoaded = true;

        return prideModifications;
    }

    @Override
    public void clearPipeline() {
        areModificationsLoaded = false;
        prideXmlLoaded = false;
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
     * Loads the experiment identifications.
     *
     * @param experimentPrideXmlFile the experiment pride XML file
     */
    private void loadExperimentIdentifications(File experimentPrideXmlFile) {
        //init the service if necessary
        if (!prideXmlLoaded) {
            experimentService.init(experimentPrideXmlFile);
        }

        //load the identifications for the given experiment
        identifications = experimentService.loadExperimentIdentifications(experimentPrideXmlFile);
        //update the considered charge states (if necessary)
        experimentService.updateChargeStates(consideredChargeStates);
    }
}
