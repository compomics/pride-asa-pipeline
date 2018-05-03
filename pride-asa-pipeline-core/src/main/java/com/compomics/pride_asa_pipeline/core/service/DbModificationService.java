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
import com.compomics.pride_asa_pipeline.model.Peptide;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Niels Hulstaert
 */
public interface DbModificationService extends ModificationService {    
    
    /**
     * Loads the experiment modifications from pride
     *
     * @param completePeptides the experiment complete peptides
     * @return the list of modifications
     */
    Set<Modification> loadExperimentModifications(List<Peptide> completePeptides);

    /**
     * Loads the experiment modifications from a pride experiment
     *
     * @param experimentId the experiment identifier
     * @return the list of modifications
     */
    Set<Modification> loadExperimentModifications(String experimentId);   
}
