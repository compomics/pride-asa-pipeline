/*
 *

 */
package com.compomics.pride_asa_pipeline.logic.modification;

import com.compomics.pride_asa_pipeline.model.Modification;
import java.io.File;
import java.util.Collection;
import java.util.Set;
import org.jdom2.JDOMException;
import org.springframework.core.io.Resource;

/**
 *
 * @author Niels Hulstaert
 */
public interface ModificationMarshaller {

    /**
     * Unmarchalls the given modifications XML resource and returns a set of
     * modifications
     *
     * @param modificationsResource the modifications XML resource to be parsed
     * @return the set of modifications
     * @exception JDOMException
     */
    Set<Modification> unmarshall(Resource modificationsResource) throws JDOMException;

    /**
     * Marshalls the collection of modifications to the modifications.xml file.
     *
     * @param modificationsResource the modifications resource
     * @param modifications the modifications collection
     */
    void marshall(Resource modificationsResource, Collection<Modification> modifications);
}
