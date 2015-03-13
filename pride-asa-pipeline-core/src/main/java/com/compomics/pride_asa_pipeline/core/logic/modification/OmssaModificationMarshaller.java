/*
 *

 */
package com.compomics.pride_asa_pipeline.core.logic.modification;

import com.compomics.omssa.xsd.UserModCollection;
import com.compomics.pride_asa_pipeline.model.Modification;
import java.util.Set;
import org.jdom2.JDOMException;
import org.springframework.core.io.Resource;

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
      /**
     * Marshalls the modifications used in the pipeline to the OMSSA format
     *
     * @param searchGuiModificationsResource
     * @return the set of searchGUI modifications with their prevalence value in PRIDE
     * @throws org.jdom2.JDOMException
     */
    Set<Modification> unmarshall(Resource searchGuiModificationsResource) throws JDOMException;
}
