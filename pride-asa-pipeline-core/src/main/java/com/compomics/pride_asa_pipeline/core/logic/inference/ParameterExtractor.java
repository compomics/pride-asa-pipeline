package com.compomics.pride_asa_pipeline.core.logic.inference;

import com.compomics.pride_asa_pipeline.core.exceptions.ParameterExtractionException;
import com.compomics.pride_asa_pipeline.core.logic.DbSpectrumAnnotator;
import com.compomics.pride_asa_pipeline.core.logic.inference.enzyme.EnzymePredictor;
import com.compomics.pride_asa_pipeline.core.logic.inference.machine.PrecursorIonErrorPredictor;
import com.compomics.pride_asa_pipeline.core.logic.inference.massdeficit.FragmentIonErrorPredictor;
import com.compomics.pride_asa_pipeline.core.logic.inference.modification.ModificationPredictor;
import com.compomics.pride_asa_pipeline.core.logic.inference.report.impl.ModificationReportGenerator;
import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileModificationRepository;
import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileSpectrumRepository;
import com.compomics.pride_asa_pipeline.core.service.impl.DbModificationServiceImpl;
import com.compomics.pride_asa_pipeline.core.service.impl.DbSpectrumServiceImpl;
import com.compomics.pride_asa_pipeline.core.spring.ApplicationContextProvider;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.massspectrometry.Charge;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;
import org.xmlpull.v1.XmlPullParserException;

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
        EnzymePredictor enzymePredictor = new EnzymePredictor(peptideSequences);

        //--------------------------------
        // USE ALL THE IDENTIFICATIONS FOR THE MODIFICATIONS AS THE ALL MIGHT HAVE USEFUL INFORMATION
        ModificationPredictor modificationPredictor = new ModificationPredictor(spectrumAnnotator.getSpectrumAnnotatorResult(), spectrumAnnotator.getModificationService());
        new ModificationReportGenerator(modificationPredictor).writeReport(System.out);
        //--------------------------------
        //recalibrate errors
        LOGGER.info("Using the " + (100 - qualityPercentile) + " % best identifications");
        //USE ONLY THE HIGH QUALITY HITS FOR MASS ACCURACCIES, THESE WILL USUALLY NOT HAVE MISSING MODIFICATIONS ETC
        List<Identification> experimentIdentifications = spectrumAnnotator.getIdentifications().getCompleteIdentifications();
        IdentificationFilter filter = new IdentificationFilter(experimentIdentifications);

        HashMap<Peptide, double[]> mzValueMap = new HashMap<>();
        for (Identification anExpIdentification : filter.getTopFragmentIonHits(qualityPercentile)) {
            double[] mzValuesBySpectrumId = fileSpectrumRepository.getMzValuesBySpectrumId(anExpIdentification.getSpectrumId());
            Peptide peptide = anExpIdentification.getPeptide();
            mzValueMap.put(peptide, mzValuesBySpectrumId);
        }

        //FragmentIonErrorPredictor fragmentIonErrorPredictor = new IterativeFragmentIonErrorPredictor(mzValueMap);
        FragmentIonErrorPredictor fragmentIonErrorPredictor = new FragmentIonErrorPredictor(mzValueMap);

        PrecursorIonErrorPredictor precursorIonErrorPredictor = new PrecursorIonErrorPredictor(filter.getTopPrecursorHits(qualityPercentile));
        //construct a parameter object
        parameters = new SearchParameters();

        parameters.setEnzyme(enzymePredictor.getMostLikelyEnzyme());
        parameters.setnMissedCleavages(enzymePredictor.getMissedCleavages());

        parameters.setPtmSettings(modificationPredictor.getPtmSettings());

        parameters.setPrecursorAccuracy(precursorIonErrorPredictor.getRecalibratedPrecursorAccuraccy());
        parameters.setPrecursorAccuracyType(SearchParameters.MassAccuracyType.DA);
        parameters.setMinChargeSearched(new Charge(Charge.PLUS, precursorIonErrorPredictor.getRecalibratedMinCharge()));
        parameters.setMaxChargeSearched(new Charge(Charge.PLUS, precursorIonErrorPredictor.getRecalibratedMaxCharge()));

        parameters.setFragmentAccuracyType(SearchParameters.MassAccuracyType.DA);
        parameters.setFragmentIonAccuracy(fragmentIonErrorPredictor.getFragmentIonAccuraccy());

    }

    /*
     * Returns the inferred search parameters
     */
    public SearchParameters getParameters() {
        return parameters;
    }

}
