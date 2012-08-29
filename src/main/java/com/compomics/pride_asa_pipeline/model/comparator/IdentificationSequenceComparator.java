/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
