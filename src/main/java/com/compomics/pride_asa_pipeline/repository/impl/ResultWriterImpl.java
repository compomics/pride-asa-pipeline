/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.repository.impl;

import com.compomics.pride_asa_pipeline.model.comparator.FragmentIonAnnotationComparator;
import com.compomics.pride_asa_pipeline.model.FragmentIonAnnotation;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.ModificationFacade;
import com.compomics.pride_asa_pipeline.model.ModifiedPeptide;
import com.compomics.pride_asa_pipeline.repository.ResultWriter;
import com.google.common.base.Joiner;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author niels
 */
public class ResultWriterImpl implements ResultWriter {

    private static final Logger LOGGER = Logger.getLogger(ResultWriterImpl.class);
    private static final String FRAGMENT_ION_TYPE_DELIMITER = ";";
    private static final String FRAGMENT_ION_NUMBER_DELIMITER = "|";
    private static final String MODIFICATIONS_DELIMITER = ";";

    @Override
    public void writeResult(File resultFile, List<Identification> identifications) {
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(resultFile)));

            //print identification header
            pw.println("spectrum_id" + "\t" + "peptide_sequence" + "\t" + "fragment_ions" + "\t" + "modifications");

            String fragmentIonsString = "";
            String modificationsString = "";
            //print identification
            for (Identification identification : identifications) {
                //check if there are fragment ion annotations
                if (identification.getAnnotationData() != null && identification.getAnnotationData().getFragmentIonAnnotations() != null) {
                    fragmentIonsString = getFragmentIonsString(identification.getAnnotationData().getFragmentIonAnnotations());
                } else {
                    fragmentIonsString = "N/A";
                }

                //check if there are modifications
                if (identification.getPeptide() instanceof ModifiedPeptide) {
                    modificationsString = getModificationsString((ModifiedPeptide) identification.getPeptide());
                } else {
                    modificationsString = "N/A";
                }

                pw.print(identification.getSpectrumId() + "\t" + identification.getPeptide().getSequenceString() + "\t" + fragmentIonsString
                        + "\t" + modificationsString);

                pw.println();
            }

            pw.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private String getModificationsString(ModifiedPeptide modifiedPeptide) {
        Joiner modificationsJoiner = Joiner.on(MODIFICATIONS_DELIMITER);
        List<String> modifications = new ArrayList<String>();
        if (modifiedPeptide.getNTermMod() != null) {
            modifications.add("NT_" + modifiedPeptide.getNTermMod().getName());
        }
        if (modifiedPeptide.getNTModifications() != null) {
            for (int i = 0; i < modifiedPeptide.getNTModifications().length; i++) {
                ModificationFacade modificationFacade = modifiedPeptide.getNTModifications()[i];
                if (modificationFacade != null) {
                    modifications.add(i + "_" + modificationFacade.getName());
                }
            }
        }
        if (modifiedPeptide.getCTermMod() != null) {
            modifications.add("CT_" + "_" + modifiedPeptide.getCTermMod().getName());
        }

        return "mods[" + modificationsJoiner.join(modifications) + "]";
    }

    private String getFragmentIonsString(List<FragmentIonAnnotation> fragmentIonAnnotations) {
        //first sort the fragment ion annotations before iterating over them
        Collections.sort(fragmentIonAnnotations, new FragmentIonAnnotationComparator());

        List<String> fragmentIonsByIonType = new ArrayList<String>();
        List<String> fragmentIonNumbersByIonType = new ArrayList<String>();
        Joiner fragmentIonTypeJoiner = Joiner.on(FRAGMENT_ION_TYPE_DELIMITER);
        Joiner fragmentIonNumberJoiner = Joiner.on(FRAGMENT_ION_NUMBER_DELIMITER);
        String currentIonType = getIonType(fragmentIonAnnotations.get(0));                
        
        for (int i = 0; i < fragmentIonAnnotations.size(); i++) {
            //check if the ion type is still the same
            if (currentIonType.equals(getIonType(fragmentIonAnnotations.get(i)))) {
                //add fragment ion number to the correct ion type
                fragmentIonNumbersByIonType.add(String.valueOf(fragmentIonAnnotations.get(i).getFragment_ion_number()));    
            } else {
                //join fragment ion numbers by ion type
                fragmentIonsByIonType.add(currentIonType + "(" + fragmentIonNumberJoiner.join(fragmentIonNumbersByIonType) + ")");
                //clear fragment ion number list and add first element
                fragmentIonNumbersByIonType.clear();
                fragmentIonNumbersByIonType.add(String.valueOf(fragmentIonAnnotations.get(i).getFragment_ion_number()));
                //change current ion type
                currentIonType = getIonType(fragmentIonAnnotations.get(i));
            }
        }
        //add the last fragment ion type        
        fragmentIonsByIonType.add(currentIonType + "(" + fragmentIonNumberJoiner.join(fragmentIonNumbersByIonType) + ")");                

        return "ions[" + fragmentIonTypeJoiner.join(fragmentIonsByIonType) + "]";
    }

    private String getIonType(FragmentIonAnnotation fragmentIonAnnotation) {
        return fragmentIonAnnotation.getIon_type_name() + "_" + fragmentIonAnnotation.getIon_charge() + "+";
    }
}
