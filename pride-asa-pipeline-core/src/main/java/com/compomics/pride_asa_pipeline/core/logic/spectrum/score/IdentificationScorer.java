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
package com.compomics.pride_asa_pipeline.core.logic.spectrum.score;

import com.compomics.pride_asa_pipeline.model.AnnotationData;
import com.compomics.pride_asa_pipeline.model.Peak;
import com.compomics.pride_asa_pipeline.model.Peptide;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: niels Date: 27/10/11 Time: 16:58 To change
 * this template use File | Settings | File Templates.
 */
public interface IdentificationScorer {

    /**
     * Scores the identification; the peptide is matched against the
     * spectrum peaks. The scoring result and fragment ion annotations are
     * returned as annotation data.
     *
     * @param peptide the peptide to score the spectrum against
     * @param peaks the spectrum peaks
     * @param fragmentMassError the fragment mass error
     * @return the annotation data (scoring result + fragment ion annotations)
     */
    AnnotationData score(Peptide peptide, List<Peak> peaks, double fragmentMassError);
}
