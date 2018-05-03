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

import com.compomics.pride_asa_pipeline.model.FragmentIonAnnotation;
import java.io.Serializable;
import java.util.Comparator;

/**
 *
 * @author Niels Hulstaert
 */
public class FragmentIonAnnotationComparator implements Comparator<FragmentIonAnnotation>{
       
    @Override
    public int compare(FragmentIonAnnotation o1, FragmentIonAnnotation o2) {
        //first check the ion type
        if(!o1.getIon_type_name().equals(o2.getIon_type_name())){
            return o1.getIon_type_name().compareTo(o2.getIon_type_name());
        }        
        else {
            //if equal, check charge
            if(o1.getIon_charge() != o2.getIon_charge()){
                return Integer.valueOf(o1.getIon_charge()).compareTo(Integer.valueOf(o2.getIon_charge()));
            }
            else{
                //if equal, check fragment ion number
                return Integer.valueOf(o1.getFragment_ion_number()).compareTo(Integer.valueOf(o2.getFragment_ion_number()));
            }
        }
    }
            
}
