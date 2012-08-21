/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.model.comparator;

import com.compomics.pride_asa_pipeline.gui.wrapper.IdentificationGuiWrapper;
import java.util.Comparator;

/**
 *
 * @author niels
 */
public class IdentificationGuiWrapperComparator implements Comparator<IdentificationGuiWrapper> {

    @Override
    public int compare(IdentificationGuiWrapper o1, IdentificationGuiWrapper o2) {
        return o1.getIdentification().getPeptide().getSequenceString().compareTo(o2.getIdentification().getPeptide().getSequenceString()); 
    }
}
