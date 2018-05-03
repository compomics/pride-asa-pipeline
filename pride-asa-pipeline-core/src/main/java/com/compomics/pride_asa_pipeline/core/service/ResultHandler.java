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
package com.compomics.pride_asa_pipeline.core.service;

import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.core.model.SpectrumAnnotatorResult;
import java.io.File;
import java.util.Set;

/**
 *
 * @author Niels Hulstaert
 */
public interface ResultHandler {
    
    /**
     * Writes the result of the annotation pipeline to file. 
     * 
     * @param spectrumAnnotatorResult the spectrum annotator result
     * @return the file that was written to
     */
    File writeResultToFile(SpectrumAnnotatorResult spectrumAnnotatorResult);
    
    /**
     * Reads the result file and returns the SpectrumAnnotatorResult.
     *
     * @param resultFile the spectrum annotation pipeline result
     * @return the spectrum annotator result
     */
    SpectrumAnnotatorResult readResultFromFile(File resultFile);
    
    /**
     * Writes the used modifications in the annotation pipeline to file. 
     * 
     * @param experimentAccession the experiment accession
     * @param usedModifications the used modifications
     */
    void writeUsedModificationsToFile(String experimentAccession, Set<Modification> usedModifications);
    
}
