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
package com.compomics.pride_asa_pipeline.core.model.instrumentation;

import java.io.File;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.CachedDataAccessController;

/**
 *
 * @author Kenneth Verheggen
 */
public class ExperimentAnnotation {

    public DataSource source;

    public ExperimentAnnotation(File prideXML) {
        init(prideXML);
    }

    private void init(File prideXML) {
        //Load the prideXML
        source = new DataSource(prideXML);
        //Init
        source.init();
        //refine the mass tolerances using the peptides?
    }

    private void loadSpectra() {
        CachedDataAccessController parser = source.getParser();
        for (Comparable proteinID : parser.getProteinIds()) {
            for (Comparable petideID : parser.getPeptideIds(proteinID)) {
                uk.ac.ebi.pride.utilities.data.core.Peptide anIdentifiedPeptide = parser.getPeptideByIndex(proteinID, petideID);
                double precursorMassError = (anIdentifiedPeptide.getSpectrumIdentification().getCalculatedMassToCharge() - anIdentifiedPeptide.getSpectrumIdentification().getExperimentalMassToCharge()) * anIdentifiedPeptide.getPrecursorCharge();
                double[][] massIntensityMap = anIdentifiedPeptide.getSpectrum().getMassIntensityMap();
            }
        }
    }



}
