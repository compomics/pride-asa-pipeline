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
package com.compomics.pride_asa_pipeline.core.logic.inference.modification;

/**
 *
 * @author Kenneth Verheggen
 */
public enum QuantMethod {
ITRAQ_QUANTIFIED, 
TMT_QUANTIFIED, 
O18_QUANTIFIED(258), 
AQUA_QUANTIFIED, 
ICAT_QUANTIFIED, 
ICPL_QUANTIFIED, 
SILAC_QUANTIFIED, 
TIC_QUANTIFIED, 
SILAC_HEAVY_REAGENT(259,267),
SILAC_MEDIUM_REAGENT,
SILAC_LIGHT_REAGENT,
ICAT_HEAVY_REAGENT,
ICAT_LIGHT_REAGENT, 
ICPL_0_REAGENT(365), 
ICPL_4_REAGENT(687), 
ICPL_6_REAGENT(364), 
ICPL_10_REAGENT(866), 
ITRAQ_113_REAGENT(730), 
ITRAQ_114_REAGENT(532,730), 
ITRAQ_115_REAGENT(533,731), 
ITRAQ_116_REAGENT(730), 
ITRAQ_117_REAGENT(730), 
ITRAQ_118_REAGENT(731), 
ITRAQ_119_REAGENT(731), 
ITRAQ_121_REAGENT(731), 
TMT_126_REAGENT, 
TMT_127_REAGENT, 
TMT_128_REAGENT, 
TMT_129_REAGENT, 
TMT_130_REAGENT, 
TMT_131_REAGENT;

   
    
    
    private final int[] unimodIDs;

    private QuantMethod(int... unimodIDs) {
        this.unimodIDs = unimodIDs;
    }
    
    public int[] getAssociatedPSINames(){
        return unimodIDs;
    }
}
