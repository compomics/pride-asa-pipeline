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
package com.compomics.pride_asa_pipeline.core.repository;

import com.compomics.pride_asa_pipeline.model.Modification;
import java.util.List;

/**
 *
 * @author Niels Hulstaert
 */
/**
 *
 * @author Kenneth Verheggen
 */
public interface ModificationRepository {

    /**
     * Gets the peptide modifications
     *
     * @param peptideId the pride peptide ID
     * @return the peptide modifications
     */
    List<Modification> getModificationsByPeptideId(long peptideId);

    /**
     * Gets the peptide modifications
     *
     * @param experimentId the pride experiment ID
     * @return the peptide modifications that are annotated in the Pride public
     * database instance
     */
    List<Modification> getModificationsByExperimentId(String experimentId);

}
