/*
 *

 */
package com.compomics.pride_asa_pipeline.core.logic.modification;

import com.compomics.omssa.xsd.UserModCollection;
import com.compomics.pride_asa_pipeline.model.Modification;
import java.util.Set;

/**
 *
 * @author Niels Hulstaert Hulstaert
 */
public interface OmssaModificationMarshaller {

    /**
     * Marshalls the modifications used in the pipeline to the OMSSA format
     *
     * @param modificationSet the set of found modifications
     * @return the usermod collection
     */
    UserModCollection marshallModifications(Set<Modification> modificationSet);
    
}
