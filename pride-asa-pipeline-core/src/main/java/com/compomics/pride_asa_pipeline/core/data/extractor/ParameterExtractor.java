package com.compomics.pride_asa_pipeline.core.data.extractor;

import com.compomics.pride_asa_pipeline.core.exceptions.ParameterExtractionException;
import com.compomics.pride_asa_pipeline.core.logic.DbSpectrumAnnotator;
import com.compomics.pride_asa_pipeline.core.logic.inference.IdentificationFilter;
import com.compomics.pride_asa_pipeline.core.logic.inference.enzyme.EnzymePredictor;
import com.compomics.pride_asa_pipeline.core.logic.inference.ionaccuracy.PrecursorIonErrorPredictor;
import com.compomics.pride_asa_pipeline.core.logic.inference.ionaccuracy.FragmentIonErrorPredictor;
import com.compomics.pride_asa_pipeline.core.logic.inference.additional.contaminants.MassScanResult;
import com.compomics.pride_asa_pipeline.core.logic.inference.modification.ModificationPredictor;
import com.compomics.pride_asa_pipeline.core.logic.inference.report.InferenceReportGenerator;
import com.compomics.pride_asa_pipeline.core.logic.inference.report.impl.ContaminationReportGenerator;
import com.compomics.pride_asa_pipeline.core.logic.inference.report.impl.EnzymeReportGenerator;
import com.compomics.pride_asa_pipeline.core.logic.inference.report.impl.FragmentIonReporter;
import com.compomics.pride_asa_pipeline.core.logic.inference.report.impl.ModificationReportGenerator;
import com.compomics.pride_asa_pipeline.core.logic.inference.report.impl.PrecursorIonReporter;
import com.compomics.pride_asa_pipeline.core.logic.inference.report.impl.TotalReportGenerator;
import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileModificationRepository;
import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileSpectrumRepository;
import com.compomics.pride_asa_pipeline.core.service.impl.DbModificationServiceImpl;
import com.compomics.pride_asa_pipeline.core.service.impl.DbSpectrumServiceImpl;
import com.compomics.pride_asa_pipeline.core.spring.ApplicationContextProvider;
import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Peptide;
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
import org.apache.log4j.Logger;
import org.xmlpull.v1.XmlPullParserException;
import uk.ac.ebi.pride.archive.web.service.model.assay.AssayDetail;

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
    private double qualityPercentile = 90;
    private FragmentIonErrorPredictor fragmentIonErrorPredictor;
    private PrecursorIonErrorPredictor precursorIonErrorPredictor;
    private ModificationPredictor modificationPredictor;
    private EnzymePredictor enzymePredictor;

    /**
     * An extractor for parameters
     *
     * @param assay the assay to extract
     * @throws ParameterExtractionException when an error occurs
     */
    public ParameterExtractor(String assay) throws ParameterExtractionException {
        try {
            //load the spectrumAnnotator ---> make sure to use the right springXMLConfig using the webservice repositories
            ApplicationContextProvider.getInstance().setDefaultApplicationContext();
            spectrumAnnotator = (DbSpectrumAnnotator) ApplicationContextProvider.getInstance().getBean("dbSpectrumAnnotator");
            init(assay);
        } catch (IOException | XmlPullParserException e) {
            throw new ParameterExtractionException(e.getMessage());
        }
    }

    private void init(String assay) throws IOException, XmlPullParserException {
        //get assay
        FileSpectrumRepository fileSpectrumRepository = new FileSpectrumRepository(assay);
        ((DbSpectrumServiceImpl) spectrumAnnotator.getSpectrumService()).setSpectrumRepository(new FileSpectrumRepository(assay));
        ((DbModificationServiceImpl) spectrumAnnotator.getModificationService()).setModificationRepository(new FileModificationRepository(assay));

        spectrumAnnotator.initIdentifications(assay);
        LOGGER.info("Spectrumannotator delivered was initialized");
        spectrumAnnotator.annotate(assay);
        //--------------------------------

        // USE ALL THE IDENTIFICATIONS FOR THE PEPTIDE SEQUENCES AS THE ALL HAVE USEFUL INFORMATION
        List<String> peptideSequences = new ArrayList<>();
        for (Peptide aPeptide : spectrumAnnotator.getIdentifications().getCompletePeptides()) {
            peptideSequences.add(aPeptide.getSequenceString());
        }
        enzymePredictor = new EnzymePredictor(peptideSequences);

        //--------------------------------
        // USE ALL THE IDENTIFICATIONS FOR THE MODIFICATIONS AS THE ALL MIGHT HAVE USEFUL INFORMATION
        modificationPredictor = new ModificationPredictor(assay, spectrumAnnotator.getSpectrumAnnotatorResult(), spectrumAnnotator.getModificationService());

        //--------------------------------
        //recalibrate errors
        //precursor needs to be very accurate (considering mods / isotopes / etc)
        LOGGER.info("Using the " + (100 - qualityPercentile) + " % best identifications for precursor accuracy estimation");
        //USE ONLY THE HIGH QUALITY HITS FOR MASS ACCURACCIES, THESE WILL USUALLY NOT HAVE MISSING MODIFICATIONS ETC
        List<Identification> experimentIdentifications = spectrumAnnotator.getIdentifications().getCompleteIdentifications();
        IdentificationFilter filter = new IdentificationFilter(experimentIdentifications);
        List<Identification> topPrecursorHits = filter.getTopPrecursorHits(90);

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

    public void remediateParametersWithAnnotation(String assay) throws IOException {
        AnalyzerData data = getAnalyzerData(assay);
        if (data != null) {
            LOGGER.info("Remediating erronous estimations...");
            if (parameters.getFragmentIonAccuracy() == 0 || parameters.getFragmentIonAccuracy() > data.getFragmentAccuraccy()) {
                LOGGER.info("Remediating fragment accuracy to match " + data.getAnalyzerFamily().toString() + " analyzers.");
                parameters.setFragmentIonAccuracy(data.getFragmentAccuraccy());
                parameters.setFragmentAccuracyType(SearchParameters.MassAccuracyType.DA);

                TotalReportGenerator.setFragmentAccMethod("Used annotated machine parameters : " + data.getAnalyzerFamily().toString());
            }
            if (parameters.getPrecursorAccuracy() == 0 || parameters.getPrecursorAccuracy() > data.getPrecursorAccuraccy()) {
                LOGGER.info("Remediating precursor accuracy to match " + data.getAnalyzerFamily().toString() + " analyzers.");
                parameters.setPrecursorAccuracy(data.getPrecursorAccuraccy());

                TotalReportGenerator.setPrecursorAccMethod("Used annotated machine parameters : " + data.getAnalyzerFamily().toString());
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

    public void printReports() throws IOException {
        List<InferenceReportGenerator> reportGenerators = new ArrayList<>();
        reportGenerators.add(new EnzymeReportGenerator(enzymePredictor));
        reportGenerators.add(new FragmentIonReporter(fragmentIonErrorPredictor));
        reportGenerators.add(new PrecursorIonReporter(precursorIonErrorPredictor));
        reportGenerators.add(new ModificationReportGenerator(modificationPredictor));
        reportGenerators.add(new ContaminationReportGenerator(precursorIonErrorPredictor.getRecalibratedPrecursorAccuraccy(), fragmentIonErrorPredictor.getFragmentIonAccuraccy()));
        for (InferenceReportGenerator reportGenerator : reportGenerators) {
            reportGenerator.writeReport(System.out);
        }
    }

    public void printReports(File outputFolder) throws IOException {
        List<InferenceReportGenerator> reportGenerators = new ArrayList<>();
        reportGenerators.add(new EnzymeReportGenerator(enzymePredictor));
        reportGenerators.add(new FragmentIonReporter(fragmentIonErrorPredictor));
        reportGenerators.add(new PrecursorIonReporter(precursorIonErrorPredictor));
        reportGenerators.add(new ModificationReportGenerator(modificationPredictor));
        reportGenerators.add(new ContaminationReportGenerator(precursorIonErrorPredictor.getRecalibratedPrecursorAccuraccy(), fragmentIonErrorPredictor.getFragmentIonAccuraccy()));
        reportGenerators.add(new TotalReportGenerator());
        for (InferenceReportGenerator reportGenerator : reportGenerators) {
            LOGGER.info("Exporting " + reportGenerator.getReportName());
            File outputFile = new File(outputFolder, reportGenerator.getReportName());
            try (FileOutputStream out = new FileOutputStream(outputFile)) {
                reportGenerator.writeReport(out);
            }
        }
    }

}
