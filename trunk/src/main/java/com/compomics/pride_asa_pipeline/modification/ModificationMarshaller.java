/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.modification;

import com.compomics.pride_asa_pipeline.model.Modification;
import java.util.Collection;
import java.util.Set;

/**
 *
 * @author niels
 */
public interface ModificationMarshaller {

    /**
     * Unmarchalls the given modifications XML file and returns a set of
     * modifications
     *
     * @param modificationsFileName the modifications XML file to be parsed path
     * @return the set of modifications
     */
    Set<Modification> unmarshall(String modificationsFileName);

    /**
     * Marshalls the collection of modifications to the modifications.xml file.
     * Returns true if the modifications file could be written, false otherwise.
     *
     * @param modificationsFileName the modifications file name
     * @param modifications the modifications collection
     * @return the success boolean
     */
    boolean marshall(String modificationsFileName, Collection<Modification> modifications);
}
