/* 
 * Copyright 2018 compomics.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.compomics.pride_asa_pipeline.core.logic.inference;


import com.compomics.pride_asa_pipeline.core.logic.inference.enzyme.EnzymePredictor;
import com.compomics.pride_asa_pipeline.core.logic.inference.modification.ModificationPredictor;
import com.compomics.pride_asa_pipeline.core.logic.spectrum.annotation.impl.SpectrumAnnotatorImpl;
import com.compomics.pride_asa_pipeline.core.model.exception.ParameterExtractionException;
import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileModificationRepository;
import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileSpectrumRepository;
import com.compomics.pride_asa_pipeline.core.service.impl.PrideModificationServiceImpl;
import com.compomics.pride_asa_pipeline.core.service.impl.SpectrumServiceImpl;
import com.compomics.pride_asa_pipeline.core.spring.ApplicationContextProvider;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.massspectrometry.Charge;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.compomics.pride_asa_pipeline.core.gui.PipelineProgressMonitor;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author Kenneth
 */
public class ParameterExtractor {

    /*
     * The spectrum annotator
     */
    private SpectrumAnnotatorImpl spectrumAnnotator;
    /*
     * The search parameters 
     */
    private SearchParameters parameters;

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
            spectrumAnnotator = (SpectrumAnnotatorImpl) ApplicationContextProvider.getInstance().getBean("spectrumAnnotator");
            init(assay);
        } catch (IOException | XmlPullParserException e) {
            throw new ParameterExtractionException(e.getMessage());
        }
    }

    private void init(String assay) throws IOException, XmlPullParserException {
        //get assay
        FileSpectrumRepository fileSpectrumRepository = new FileSpectrumRepository(assay);
        ((SpectrumServiceImpl) spectrumAnnotator.getSpectrumService()).setSpectrumRepository(new FileSpectrumRepository(assay));
        ((PrideModificationServiceImpl) spectrumAnnotator.getModificationService()).setModificationRepository(new FileModificationRepository(assay));

        spectrumAnnotator.initIdentifications(assay);
        PipelineProgressMonitor.info("Spectrumannotator delivered was initialized");
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
        //--------------------------------

        //construct a parameter object
        parameters = new SearchParameters();

        parameters.setEnzyme(enzymePredictor.getMostLikelyEnzyme());
        parameters.setnMissedCleavages(enzymePredictor.getMissedCleavages());

        parameters.setPtmSettings(modificationPredictor.getPtmSettings());

        parameters.setPrecursorAccuracy(spectrumAnnotator.getInstrumentation().getPrecAcc());
        parameters.setPrecursorAccuracyType(SearchParameters.MassAccuracyType.PPM);
        parameters.setMinChargeSearched(new Charge(Charge.PLUS, spectrumAnnotator.getInstrumentation().getPossibleCharges().first()));
        parameters.setMaxChargeSearched(new Charge(Charge.PLUS, spectrumAnnotator.getInstrumentation().getPossibleCharges().last()));

        parameters.setFragmentAccuracyType(SearchParameters.MassAccuracyType.DA);
        parameters.setFragmentIonAccuracy(spectrumAnnotator.getInstrumentation().getFragAcc());

    }

    /*
     * Returns the inferred search parameters
     */
    public SearchParameters getParameters() {
        
        return parameters;
    }

}
