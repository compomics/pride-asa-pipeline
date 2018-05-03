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

import com.compomics.pride_asa_pipeline.model.Identification;
import java.util.Comparator;

/**
 *
 * @author Niels Hulstaert
 */
public class IdentificationSequenceComparator implements Comparator<Identification> {    
    
    @Override
    public int compare(Identification identification1, Identification identification2) {
        return identification1.getPeptide().getSequenceString().compareTo(identification2.getPeptide().getSequenceString());
    }
        
}
