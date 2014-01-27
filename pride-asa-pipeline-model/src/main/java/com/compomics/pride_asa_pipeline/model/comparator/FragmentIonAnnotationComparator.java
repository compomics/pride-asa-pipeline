/*
 *

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
