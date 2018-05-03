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
package com.compomics.pride_asa_pipeline.model.comparator;

import com.compomics.pride_asa_pipeline.model.Peak;
import java.util.Comparator;

/**
 * @author Florian Reisinger
 *         Date: 15-Jun-2010
 * @since $version
 */
public class PeakMzComparator implements Comparator<Peak> {
    
    @Override
    public int compare(Peak p1, Peak p2) {
        //compare the two provided peaks according to there m/z value
        return Double.compare(p1.getMzRatio(), p2.getMzRatio());
    }
}
