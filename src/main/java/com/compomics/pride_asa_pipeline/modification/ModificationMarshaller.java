/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.modification;

import com.compomics.pride_asa_pipeline.model.Modification;
import java.io.File;
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
     * @param modificationFile the modifications XML file to be parsed
     * @return the set of modifications
     */
    Set<Modification> unmarshall(File modificationFile);

    /**
     * Marshalls the collection of modifications to the modifications.xml file
     *
     * 
     * @param modificationFilePath the modification file path
     * @param modifications the modifications collection
     */
    void marshall(String modificationFilePath, Collection<Modification> modifications);
}
