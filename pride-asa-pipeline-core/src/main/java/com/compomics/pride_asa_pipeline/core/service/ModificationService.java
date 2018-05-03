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

import com.compomics.omssa.xsd.UserModCollection;
import com.compomics.pride_asa_pipeline.core.model.ModificationHolder;
import com.compomics.pride_asa_pipeline.core.model.SpectrumAnnotatorResult;
import com.compomics.pride_asa_pipeline.model.Modification;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Niels Hulstaert
 */
public interface ModificationService {

    /**
     * The mass delta tolerance value used for comparing mod masses
     */
    public static final double MASS_DELTA_TOLERANCE = 0.1;
    /**
     * The maximum number of affected amino acids a non-terminal (PRIDE)
     * modification is allowed to have
     */
    public static final int MAX_AFFECTED_AMINO_ACIDS = 10;    

    /**
     * Gets the modifications as a map (key: modification, value: occurence
     * count) that were actually used in the pipeline; i.e. modifications that
     * could be combined the explain a certain mass delta for a precursor.
     *
     * @param spectrumAnnotatorResult the spectrum annotator result
     * @return the used modifications
     */
    Map<Modification, Integer> getUsedModifications(SpectrumAnnotatorResult spectrumAnnotatorResult);

    /**
     * Gets the modifications as a map (key: modification, value: occurence
     * count) that were actually used in the pipeline; i.e. modifications that
     * could be combined the explain a certain mass delta for a precursor.
     *
     * @param usedModifications map returned by the getUsedModifications method
     * @param spectrumAnnotatorResult the spectrum annotator result
     * @param aFixedModificationThreshold
     * @return a Map with the used modifications as keys, and Double values that
     * are True when the corresponding Modification. e.g the rate is 0.95 if 95%
     * of all Identifications that can be targeted by a modification are
     * actually modified
     */
    Map<Modification, Double> estimateModificationRate(Map<Modification, Integer> usedModifications, SpectrumAnnotatorResult spectrumAnnotatorResult, double aFixedModificationThreshold);

    /**
     * Gets the modifications that were actually used in the pipeline; i.e.
     * modifications that could be combined the explain a certain mass delta for
     * a precursor.
     *
     * @param spectrumAnnotatorResult the spectrum annotator result
     * @return the used modifications
     */
    UserModCollection getModificationsAsUserModCollection(SpectrumAnnotatorResult spectrumAnnotatorResult);

    /**
     * Finds the modifications from the given set that have a modification in
     * the modification holder with the same monoisotopic mass (with an error
     * tolerance MASS_DELTA_TOLERANCE). Also check modifications that could
     * occur on too many positions.
     *
     * @param modificationHolder the ModificationHolder
     * @param modifications the given set of modifications
     * @return the set of filtered modifications
     */
    Set<Modification> filterModifications(ModificationHolder modificationHolder, Set<Modification> modifications);
}
