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
package com.compomics.pride_asa_pipeline.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Florian Reisinger Date: 20-Aug-2009
 * @since 0.1
 */
public class Identifications {

    List<Identification> completeIdentifications;
    List<Identification> incompleteIdentifications;
    List<Peptide> completePeptides;
    List<Peptide> incompletePeptides;
    
    public Identifications(){
        completeIdentifications = new ArrayList<Identification>();
        incompleteIdentifications = new ArrayList<Identification>();
        completePeptides = new ArrayList<Peptide>();
        incompletePeptides = new ArrayList<Peptide>();
    }
    /**
     * Gets the complete identifications
     *
     * @return the complete identifications
     */
    public List<Identification> getCompleteIdentifications() {
        return completeIdentifications;
    }

    /**
     * Sets the complete identifications
     *
     * @param completeIdentifications the complete identifications
     */
    public void setCompleteIdentifications(List<Identification> completeIdentifications) {
        this.completeIdentifications = completeIdentifications;
    }

    /**
     * Gets the incomplete identifications
     *
     * @return the incomplete identifications
     */
    public List<Identification> getIncompleteIdentifications() {
        return incompleteIdentifications;
    }

    /**
     * Sets the incomplete identifications
     *
     * @param incompleteIdentifications the incomplete identifications
     */
    public void setIncompleteIdentifications(List<Identification> incompleteIdentifications) {
        this.incompleteIdentifications = incompleteIdentifications;
    }

    /**
     * Gets the complete peptides
     * 
     * @return the complete peptides
     */
    public List<Peptide> getCompletePeptides() {
        return completePeptides;
    }

    /**
     * Gets the incomplete peptides
     * 
     * @return the incomplete peptides
     */
    public List<Peptide> getIncompletePeptides() {
        return incompletePeptides;
    }
            
    public long getSize() {
        return completeIdentifications.size() + incompleteIdentifications.size();
    }
    
    /**
     * Adds the identification to the identifications
     * 
     * @param identification the identification
     */
    public void addIdentification(Identification identification) {
        if (identification.getPeptide().getSequence().isAllMassesKnown()) {
            completeIdentifications.add(identification);
            completePeptides.add(identification.getPeptide());
        } else {
            incompleteIdentifications.add(identification);
            incompletePeptides.add(identification.getPeptide());
        }
    }        
    
}
