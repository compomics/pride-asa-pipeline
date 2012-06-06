/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.repository;

import com.compomics.pride_asa_pipeline.model.Modification;
import java.util.List;

/**
 *
 * @author niels
 */
public interface ModificationRepository {
     
     /**
      * Gets the peptide modifications
      * 
      * @param peptideId the pride peptide ID
      * @return the peptide modifications
      */
     List<Modification> getModificationsByPeptideId(long peptideId);
    
}
