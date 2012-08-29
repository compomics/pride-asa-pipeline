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
public class IdentificationSpectrumIdComparator implements Comparator<Identification> {

    @Override
    public int compare(Identification identification1, Identification identification2) {
        return Double.compare(identification1.getSpectrumId(), identification2.getSpectrumId());
    }
        
}
