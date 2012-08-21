/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.logic.modification;

import com.compomics.omssa.xsd.UserModCollection;
import com.compomics.pride_asa_pipeline.model.Modification;
import java.util.Set;

/**
 *
 * @author Niels Hulstaert
 */
public interface OmssaModiciationMarshaller {

    /**
     * Marshalls the modifications used in the pipeline to the OMSSA format
     *
     * @param modificationSet: the set of found modifications
     * @return the usermod collection
     */
    UserModCollection marshallModifications(Set<Modification> modificationSet);
    
}
