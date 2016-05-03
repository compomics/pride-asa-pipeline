package com.compomics.pride_asa_pipeline.core.data.extractor;

import com.compomics.pride_asa_pipeline.core.exceptions.ParameterExtractionException;
import com.compomics.pride_asa_pipeline.core.logic.DbSpectrumAnnotator;
import com.compomics.pride_asa_pipeline.core.logic.inference.IdentificationFilter;
import com.compomics.pride_asa_pipeline.core.logic.inference.enzyme.EnzymePredictor;
import com.compomics.pride_asa_pipeline.core.logic.inference.ionaccuracy.PrecursorIonErrorPredictor;
import com.compomics.pride_asa_pipeline.core.logic.inference.ionaccuracy.FragmentIonErrorPredictor;
import com.compomics.pride_asa_pipeline.core.logic.inference.additional.contaminants.MassScanResult;
import com.compomics.pride_asa_pipeline.core.logic.inference.modification.ModificationPredictor;
import com.compomics.pride_asa_pipeline.core.util.report.ExtractionReportGenerator;
import com.compomics.pride_asa_pipeline.core.util.report.impl.ContaminationReportGenerator;
import com.compomics.pride_asa_pipeline.core.util.report.impl.EnzymeReportGenerator;
import com.compomics.pride_asa_pipeline.core.util.report.impl.FragmentIonReporter;
import com.compomics.pride_asa_pipeline.core.util.report.impl.ModificationReportGenerator;
import com.compomics.pride_asa_pipeline.core.util.report.impl.PrecursorIonReporter;
import com.compomics.pride_asa_pipeline.core.util.report.impl.TotalReportGenerator;
import com.compomics.pride_asa_pipeline.core.model.modification.ModificationAdapter;
import com.compomics.pride_asa_pipeline.core.model.modification.impl.UtilitiesPTMAdapter;
import com.compomics.pride_asa_pipeline.core.model.modification.source.PRIDEModificationFactory;
import com.compomics.pride_asa_pipeline.core.repository.impl.FileResultHandlerImpl3;
import com.compomics.pride_asa_pipeline.core.repository.impl.combo.FileExperimentModificationRepository;
import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileModificationRepository;
import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileSpectrumRepository;
import com.compomics.pride_asa_pipeline.core.service.impl.DbModificationServiceImpl;
import com.compomics.pride_asa_pipeline.core.service.impl.DbSpectrumServiceImpl;
import com.compomics.pride_asa_pipeline.core.spring.ApplicationContextProvider;
import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.model.PipelineExplanationType;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.pride.PrideWebService;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Set;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.xmlpull.v1.XmlPullParserException;
import uk.ac.ebi.pride.archive.web.service.model.assay.AssayDetail;
import uk.ac.ebi.pridemod.model.PTM;

/**
 *
 * @author Kenneth
 */
public class ParameterExtractor {

    /*
     * The Logger
     */
    private static final Logger LOGGER = Logger.getLogger(ParameterExtractor.class);
    /*
     * The spectrum annotator
     */
    private DbSpectrumAnnotator spectrumAnnotator;
    /*
     * The search parameters 
     */
    private SearchParameters parameters;
    /*
     * The quality percentile
     */
    private final double QUALITY_PERCENTILE = 90;
    /**
     * Predictor for fragment ion errors
     */
    private FragmentIonErrorPredictor fragmentIonErrorPredictor;
        /**
     * Predictor for precursor ion errors
     */
    private PrecursorIonErrorPredictor precursorIonErrorPredictor;
        /**
     * Predictor for modifications
     */
    private ModificationPredictor modificationPredictor;
        /**
     * Predictor for enzyme information
     */
    private EnzymePredictor enzymePredictor;
        /**
     * boolean indicating if there are results to be printed
     */
    private boolean printableReports = true;
        /**
     * Information on the used instrumentation
     */
    private AnalyzerData analyzerData;

          /**
     * An extractor for parameters
     *
     * @param assay the assay to extract
     * @param analyzerData the type of instrument used when not available through the webservice
     * @param modRepository an existing modification repository (mainly used to avoid reparsing of the file)
     * @throws ParameterExtractionException when an error occurs
     */
    public ParameterExtractor(String assay,AnalyzerData analyzerData,FileExperimentModificationRepository modRepository) throws ParameterExtractionException {
            //load the spectrumAnnotator ---> make sure to use the right springXMLConfig using the webservice repositories
            ApplicationContextProvider.getInstance().setDefaultApplicationContext();
            spectrumAnnotator = (DbSpectrumAnnotator) ApplicationContextProvider.getInstance().getBean("dbSpectrumAnnotator");
            spectrumAnnotator.setModificationRepository(modRepository);
            this.analyzerData=analyzerData;
            init(assay);
    }
    
       /**
     * An extractor for parameters
     *
     * @param assay the assay to extract
     * @param analyzerData the type of instrument used when not available through the webservice
     * @throws ParameterExtractionException when an error occurs
     */
    public ParameterExtractor(String assay,AnalyzerData analyzerData) throws ParameterExtractionException {
            //load the spectrumAnnotator ---> make sure to use the right springXMLConfig using the webservice repositories
            ApplicationContextProvider.getInstance().setDefaultApplicationContext();
            spectrumAnnotator = (DbSpectrumAnnotator) ApplicationContextProvider.getInstance().getBean("dbSpectrumAnnotator");
            this.analyzerData=analyzerData;
            init(assay);
    }
    /**
     * An extractor for parameters
     *
     * @param assay the assay to extract
     * @throws ParameterExtractionException when an error occurs
     */
    public ParameterExtractor(String assay) throws ParameterExtractionException {
  
            //load the spectrumAnnotator ---> make sure to use the right springXMLConfig using the webservice repositories
            ApplicationContextProvider.getInstance().setDefaultApplicationContext();
            spectrumAnnotator = (DbSpectrumAnnotator) ApplicationContextProvider.getInstance().getBean("dbSpectrumAnnotator");
            init(assay);    
    }

    private void init(String assay) throws ParameterExtractionException {
        //get assay
        try {
            TotalReportGenerator.setAssay(assay);
            FileSpectrumRepository fileSpectrumRepository = new FileSpectrumRepository(assay);
            ((DbSpectrumServiceImpl) spectrumAnnotator.getSpectrumService()).setSpectrumRepository(new FileSpectrumRepository(assay));
            ((DbModificationServiceImpl) spectrumAnnotator.getModificationService()).setModificationRepository(new FileModificationRepository(assay));

            spectrumAnnotator.initIdentifications(assay);
            LOGGER.debug("Spectrumannotator delivered was initialized");
            spectrumAnnotator.annotate(assay);
            //--------------------------------

             //recalibrate errors
                //precursor needs to be very accurate (considering mods / isotopes / etc)
                LOGGER.debug("Using the " + (100 - QUALITY_PERCENTILE) + " % best identifications for precursor accuracy estimation");
                List<Identification> experimentIdentifications = spectrumAnnotator.getIdentifications().getCompleteIdentifications();
               
                //USE ONLY THE HIGH QUALITY HITS FOR MASS ACCURACCIES, THESE WILL USUALLY NOT HAVE MISSING MODIFICATIONS ETC
                IdentificationFilter filter = new IdentificationFilter(experimentIdentifications);
                List<Identification> topPrecursorHits = filter.getTopPrecursorHits(QUALITY_PERCENTILE);

                precursorIonErrorPredictor = new PrecursorIonErrorPredictor(topPrecursorHits);

                //fragment ion is harder, more leanway should be given
                HashMap<Peptide, double[]> mzValueMap = new HashMap<>();
                //just use all of them
                // List<Identification> topFragmentIonHits = filter.getTopFragmentIonHits(75);
                for (Identification anExpIdentification : experimentIdentifications) {
                    double[] mzValuesBySpectrumId = fileSpectrumRepository.getMzValuesBySpectrumId(anExpIdentification.getSpectrumId());
                    Peptide peptide = anExpIdentification.getPeptide();
                    mzValueMap.put(peptide, mzValuesBySpectrumId);

                }
                MassScanResult.scanFragmentIonContamination(topPrecursorHits);
                //FragmentIonErrorPredictor fragmentIonErrorPredictor = new IterativeFragmentIonErrorPredictor(mzValueMap);
                fragmentIonErrorPredictor = new FragmentIonErrorPredictor(mzValueMap);
            
            // USE ALL THE IDENTIFICATIONS FOR THE PEPTIDE SEQUENCES AS THE ALL HAVE USEFUL INFORMATION
            List<String> peptideSequences = new ArrayList<>();
            List<Peptide> completePeptides = spectrumAnnotator.getIdentifications().getCompletePeptides();
            if (completePeptides.isEmpty()) {
                LOGGER.error("There are no identifications to work with, using defaults...");
                useDefaults(assay);
            } else {

                for (Peptide aPeptide : completePeptides) {
                    peptideSequences.add(aPeptide.getSequenceString());
                }

                enzymePredictor = new EnzymePredictor(peptideSequences);

                //--------------------------------
                // USE ALL THE IDENTIFICATIONS FOR THE MODIFICATIONS AS THE ALL MIGHT HAVE USEFUL INFORMATION
                modificationPredictor = new ModificationPredictor(assay, spectrumAnnotator.getModificationHolder(),spectrumAnnotator.getModRepository());

                //--------------------------------
               

                //construct a parameter object
                parameters = new SearchParameters();

                parameters.setEnzyme(enzymePredictor.getMostLikelyEnzyme());
                parameters.setnMissedCleavages(enzymePredictor.getMissedCleavages());

                parameters.setPtmSettings(modificationPredictor.getPtmSettings());

                double predictedPrecursorMassError = precursorIonErrorPredictor.getRecalibratedPrecursorAccuraccy();

                parameters.setPrecursorAccuracy(predictedPrecursorMassError);
                parameters.setPrecursorAccuracyType(SearchParameters.MassAccuracyType.DA);

                parameters.setMinChargeSearched(new Charge(Charge.PLUS, precursorIonErrorPredictor.getRecalibratedMinCharge()));
                parameters.setMaxChargeSearched(new Charge(Charge.PLUS, precursorIonErrorPredictor.getRecalibratedMaxCharge()));

                double predictedFragmentMassError = fragmentIonErrorPredictor.getFragmentIonAccuraccy();

                parameters.setFragmentAccuracyType(SearchParameters.MassAccuracyType.DA);
                parameters.setFragmentIonAccuracy(predictedFragmentMassError);
                remediateParametersWithAnnotation(assay);
                TotalReportGenerator.setFragmentAcc(parameters.getFragmentIonAccuracy());
                TotalReportGenerator.setPrecursorAcc(parameters.getPrecursorAccuracy());
            }
        } catch (Exception e) {
              // useDefaults(assay);
              e.printStackTrace();
                 throw new ParameterExtractionException(e.getMessage());
        }
    }

    public void remediateParametersWithAnnotation(String assay) throws IOException {
        if(analyzerData==null){
        analyzerData = getAnalyzerData(assay);
        }
        if (analyzerData != null) {
            double minimalAccuracy = 0.001; //~5ppm for orbitrap family
            if (analyzerData.getAnalyzerFamily().equals(AnalyzerData.ANALYZER_FAMILY.FT)
                    | analyzerData.getAnalyzerFamily().equals(AnalyzerData.ANALYZER_FAMILY.TOF
                    )) {
                minimalAccuracy = 0.010; // ~50ppm for others?;
            }
           LOGGER.info("Remediating erronous estimations...");
            if (parameters.getPrecursorAccuracy() < minimalAccuracy) {
                TotalReportGenerator.setPrecursorAccMethod("Using default, minimal machine --> inferred accuracy (" + parameters.getFragmentIonAccuracy() + ") too high for " + analyzerData.getAnalyzerFamily().toString());
                parameters.setPrecursorAccuracy(minimalAccuracy);
                parameters.setPrecursorAccuracyType(SearchParameters.MassAccuracyType.DA);
            } else if (parameters.getPrecursorAccuracy() > analyzerData.getFragmentMassError()) {
                LOGGER.info("Remediating fragment accuracy to match " + analyzerData.getAnalyzerFamily().toString() + " analyzers.");
                parameters.setPrecursorAccuracy(analyzerData.getFragmentMassError());
                parameters.setPrecursorAccuracyType(SearchParameters.MassAccuracyType.DA);
                TotalReportGenerator.setPrecursorAccMethod("Used annotated machine parameters : " + analyzerData.getAnalyzerFamily().toString());
            }

            if (parameters.getFragmentIonAccuracy() < parameters.getPrecursorAccuracy()) {
                TotalReportGenerator.setFragmentAccMethod("Precursor < Fragment --> inferred accuracy (" + parameters.getFragmentIonAccuracy() + ") set to match precursor");
                parameters.setFragmentIonAccuracy(parameters.getPrecursorAccuracy());
                parameters.setFragmentAccuracyType(SearchParameters.MassAccuracyType.DA);
            } else if (parameters.getFragmentIonAccuracy() < minimalAccuracy) {
                TotalReportGenerator.setFragmentAccMethod("Using default, minimal machine --> inferred accuracy (" + parameters.getFragmentIonAccuracy() + ") too high for " + analyzerData.getAnalyzerFamily().toString());
                parameters.setFragmentIonAccuracy(minimalAccuracy);
                parameters.setFragmentAccuracyType(SearchParameters.MassAccuracyType.DA);
            } else if (parameters.getFragmentIonAccuracy() > analyzerData.getFragmentMassError()) {
                LOGGER.info("Remediating fragment accuracy to match " + analyzerData.getAnalyzerFamily().toString() + " analyzers.");
                parameters.setFragmentIonAccuracy(analyzerData.getFragmentMassError());
                parameters.setFragmentAccuracyType(SearchParameters.MassAccuracyType.DA);
                TotalReportGenerator.setFragmentAccMethod("Used annotated machine parameters : " + analyzerData.getAnalyzerFamily().toString());
            }

        }
    }

    private AnalyzerData getAnalyzerData(String assay) throws IOException {
        AnalyzerData analyzerData = null;
        try {
            AssayDetail assayDetail = PrideWebService.getAssayDetail(assay);
            Set<String> instrumentNames = assayDetail.getInstrumentNames();
            analyzerData = AnalyzerData.getAnalyzerDataByAnalyzerType("");
            if (instrumentNames.size() > 0) {
                LOGGER.warn("There are multiple instruments, selecting lowest precursor accuraccy...");
            }
            for (String anInstrumentName : instrumentNames) {
                AnalyzerData temp = AnalyzerData.getAnalyzerDataByAnalyzerType(anInstrumentName);
                //worst precursor has benefit
                if (analyzerData.getPrecursorAccuraccy() > temp.getPrecursorAccuraccy()) {
                    analyzerData = temp;
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Could not retrieve analyzer data from pride webservice.");
        }
        return analyzerData;
    }

    /*
     * Returns the inferred search parameters
     */
    public SearchParameters getParameters() {
        return parameters;
    }

    public List<ExtractionReportGenerator> getReportGenerators() {
        List<ExtractionReportGenerator> reportGenerators = new ArrayList<>();
        reportGenerators.add(new EnzymeReportGenerator(enzymePredictor));
        reportGenerators.add(new FragmentIonReporter(fragmentIonErrorPredictor));
        reportGenerators.add(new PrecursorIonReporter(precursorIonErrorPredictor));
        reportGenerators.add(new ModificationReportGenerator(modificationPredictor));
        reportGenerators.add(new ContaminationReportGenerator(precursorIonErrorPredictor.getRecalibratedPrecursorAccuraccy(), fragmentIonErrorPredictor.getFragmentIonAccuraccy()));
        reportGenerators.add(new TotalReportGenerator());

        return reportGenerators;
    }

    public void printReports() throws IOException {
        for (ExtractionReportGenerator reportGenerator : getReportGenerators()) {
            reportGenerator.writeReport(System.out);
        }
    }

    public void printReports(File outputFolder) throws IOException {
        if (printableReports) {
            LOGGER.info("Exporting reports...");
            for (ExtractionReportGenerator reportGenerator : getReportGenerators()) {
                LOGGER.debug("Exporting " + reportGenerator.getReportName());
                File outputFile = new File(outputFolder, reportGenerator.getReportName());
                try (FileOutputStream out = new FileOutputStream(outputFile)) {
                    reportGenerator.writeReport(out);
                }
            }
            printPRIDEAsapIdentificationResult(outputFolder);
        } else {
            LOGGER.info("Printing of reports was skipped : no metadata inference performed");
        }
    }

    public void printPRIDEAsapIdentificationResult(File outputFolder) {
        LOGGER.debug("Exporting PRIDE Asap identification reports");
        FileResultHandlerImpl3 fileResultHandler = new FileResultHandlerImpl3();
        List<Identification> completeIdentifications = spectrumAnnotator.getIdentifications().getCompleteIdentifications();
        for (Identification ident : completeIdentifications) {
            if (ident.getPipelineExplanationType() == null) {
                ident.setPipelineExplanationType(PipelineExplanationType.UNEXPLAINED);
            }
        }
        fileResultHandler.writeResult(new File(outputFolder, "complete_id.txt"), completeIdentifications);
    }

    public void useDefaults(String assay) throws IOException, XmlPullParserException {
        printableReports = false;
        parameters = new SearchParameters();
        parameters.setEnzyme(new EnzymePredictor().getMostLikelyEnzyme());
        parameters.setnMissedCleavages(2);
        PtmSettings ptmSettings = new PtmSettings();
        ModificationAdapter adapter = new UtilitiesPTMAdapter();
        PRIDEModificationFactory ptmFactory = PRIDEModificationFactory.getInstance();
        //add carbamidomethyl c
        com.compomics.util.experiment.biology.PTM carbamidomethylC;
        try {
            carbamidomethylC = (com.compomics.util.experiment.biology.PTM) ptmFactory.getModification(adapter, ptmFactory.getModificationNameFromAccession("UNIMOD:940"));
       
        ptmSettings.addFixedModification(carbamidomethylC);
        //add oxidation of m
        com.compomics.util.experiment.biology.PTM oxidationM
                = (com.compomics.util.experiment.biology.PTM) ptmFactory.getModification(adapter, ptmFactory.getModificationNameFromAccession("UNIMOD:35"));
        ptmSettings.addVariableModification(oxidationM);
        } catch (ParameterExtractionException ex) {
              LOGGER.error("Could not load default modifications. Reason :" + ex);
        }
        parameters.setPtmSettings(ptmSettings);

        //try to get from machine...
        AnalyzerData data = getAnalyzerData(assay);

        double predictedPrecursorMassError = 1.0;
        double predictedFragmentMassError = 1.0;
        if (data != null) {
            LOGGER.debug("Getting mass errors from machine type : " + data.getAnalyzerFamily());
            predictedPrecursorMassError = data.getPrecursorMassError();
            predictedFragmentMassError = data.getFragmentMassError();
        }

        parameters.setPrecursorAccuracy(predictedPrecursorMassError);
        parameters.setPrecursorAccuracyType(SearchParameters.MassAccuracyType.DA);
        parameters.setFragmentAccuracyType(SearchParameters.MassAccuracyType.DA);
        parameters.setFragmentIonAccuracy(predictedFragmentMassError);

        parameters.setMinChargeSearched(new Charge(Charge.PLUS, 2));
        parameters.setMaxChargeSearched(new Charge(Charge.PLUS, 5));

        TotalReportGenerator.setEnzymeMethod("Defaulted");
        TotalReportGenerator.setFragmentAccMethod("Defaulted");
        TotalReportGenerator.setPrecursorAccMethod("Defaulted");
        TotalReportGenerator.setPtmSettingsMethod("Defaulted");

    }

    public void clear(){
        if(spectrumAnnotator!=null){
            spectrumAnnotator.clearPipeline();
            spectrumAnnotator.clearTmpResources();
        }
        
        if(fragmentIonErrorPredictor!=null){
            fragmentIonErrorPredictor.clear();
        };
    if(precursorIonErrorPredictor!=null)
    {
        precursorIonErrorPredictor.clear();
    }}

    void setModRepository(FileExperimentModificationRepository modificationRepository) {
       spectrumAnnotator.setModificationRepository(modificationRepository);
    }
    
    
}
