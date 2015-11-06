/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.logic.inference;

import com.compomics.pride_asa_pipeline.core.exceptions.ParameterExtractionException;
import com.compomics.pride_asa_pipeline.core.logic.DbSpectrumAnnotator;
import com.compomics.pride_asa_pipeline.core.logic.inference.enzyme.EnzymePredictor;
import com.compomics.pride_asa_pipeline.core.logic.inference.machine.MassAccuraccyPredictor;
import com.compomics.pride_asa_pipeline.core.logic.inference.modification.ModificationPredictor;
import com.compomics.pride_asa_pipeline.core.spring.ApplicationContextProvider;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.massspectrometry.Charge;
import java.io.IOException;
import java.util.ArrayList;
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
    DbSpectrumAnnotator spectrumAnnotator;
    /*
     * The search parameters 
     */
    private SearchParameters parameters;

    public ParameterExtractor(String assay) throws ParameterExtractionException {
        try {
            init(assay);
        } catch (IOException | XmlPullParserException e) {
            throw new ParameterExtractionException(e.getMessage());
        }
    }

    private void init(String assay) throws IOException, XmlPullParserException {
        //load the spectrumAnnotator ---> make sure to use the right springXMLConfig using the webservice repositories
        ApplicationContextProvider.getInstance().setDefaultApplicationContext();
        spectrumAnnotator = (DbSpectrumAnnotator) ApplicationContextProvider.getInstance().getBean("dbSpectrumAnnotator");
        //get assay
        spectrumAnnotator.initIdentifications(assay);
        LOGGER.info("Spectrumannotator delivered was initialized");
        spectrumAnnotator.annotate(assay);
        //--------------------------------
        List<String> peptideSequences = new ArrayList<>();
        for (Peptide aPeptide : spectrumAnnotator.getIdentifications().getCompletePeptides()) {
            peptideSequences.add(aPeptide.getSequenceString());
        }
        EnzymePredictor enzymePredictor = new EnzymePredictor(peptideSequences);
        //--------------------------------
        //try to find the used modifications
        ModificationPredictor modificationPredictor = new ModificationPredictor(spectrumAnnotator.getSpectrumAnnotatorResult(), spectrumAnnotator.getModificationService());
        //--------------------------------
        //recalibrate errors
        MassAccuraccyPredictor machinePredictor = new MassAccuraccyPredictor(spectrumAnnotator.getSpectrumAnnotatorResult(), spectrumAnnotator.getMassDeltaExplainer());
        //construct a parameter object
        parameters = new SearchParameters();

        parameters.setEnzyme(enzymePredictor.getMostLikelyEnzyme());
        parameters.setnMissedCleavages(enzymePredictor.getMissedCleavages());

        parameters.setPtmSettings(modificationPredictor.getPtmSettings());

        parameters.setPrecursorAccuracyDalton(machinePredictor.getRecalibratedPrecursorAccuraccy());
        parameters.setFragmentAccuracyType(SearchParameters.MassAccuracyType.DA);
        parameters.setFragmentIonAccuracy(machinePredictor.getRecalibratedFragmentIonAccuraccy());
        parameters.setMinChargeSearched(new Charge(Charge.PLUS, machinePredictor.getRecalibratedMinCharge()));
        parameters.setMaxChargeSearched(new Charge(Charge.PLUS, machinePredictor.getRecalibratedMaxCharge()));
    }

    /*
     * Returns the inferred search parameters
     */
    public SearchParameters getParameters() {
        return parameters;
    }

}
