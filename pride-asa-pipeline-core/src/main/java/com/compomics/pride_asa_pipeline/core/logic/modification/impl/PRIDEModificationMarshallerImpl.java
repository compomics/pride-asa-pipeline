/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.logic.modification.impl;

import com.compomics.pride_asa_pipeline.core.logic.modification.ModificationMarshaller;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.util.io.json.JsonMarshaller;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.log4j.Logger;
import org.jdom2.JDOMException;
import org.springframework.core.io.Resource;

/**
 *
 * @author Kenneth
 */
public class PRIDEModificationMarshallerImpl implements ModificationMarshaller {

    private static final JsonMarshaller marshaller = new JsonMarshaller();
    private static final Logger LOGGER = Logger.getLogger(PRIDEModificationMarshallerImpl.class);

    @Override
    public Set<Modification> unmarshall(Resource modificationsResource) throws JDOMException {
        LinkedHashSet<Modification> unimodSet = null;
        try {
            File resourceFile = modificationsResource.getFile();
            unimodSet = (LinkedHashSet<Modification>) marshaller.fromJson(LinkedHashSet.class, resourceFile);
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
        return unimodSet;
    }

    @Override
    public void marshall(Resource modificationsResource, Collection<Modification> modifications) {
        try {
            marshaller.saveObjectToJson(modifications, modificationsResource.getFile());
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
    }

}
