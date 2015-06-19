/*
 *

 */
package com.compomics.pride_asa_pipeline.core.service;

import com.compomics.pride_asa_pipeline.core.logic.modification.InputType;
import com.compomics.pride_asa_pipeline.model.Modification;
import org.jdom2.JDOMException;
import org.springframework.core.io.Resource;

import java.util.Collection;
import java.util.Set;

/**
 * @author Niels Hulstaert
 */
public interface PipelineModificationService {

    /**
     * This method will read and parse the modification definitions specified in the modification resource into a Set of
     * Modification objects. Note: the modification resource is only parsed once and the so acquired modification
     * objects cached in the list for later retrieval. Changes to the modification resource after the first call of this
     * method will therefore have no effect.
     *
     * @param modificationsResource the modifications XML resource
     * @param inputType             the modification resource input type
     * @return the set of modifications as specified in the modification resource or null if the modification resource
     * does not exist or could not be parsed.
     * @throws JDOMException
     */
    Set<Modification> loadPipelineModifications(Resource modificationsResource, InputType inputType) throws JDOMException;

    /**
     * Saves the pipeline modifications to the pride_asap_modifications.xml resource.
     *
     * @param modificationsResource the modifications XML resource
     * @param pipelineModifications the collections of pipeline modifications
     */
    void savePipelineModifications(Resource modificationsResource, Collection<Modification> pipelineModifications);

    /**
     * Imports the pipeline modifications from a given the modifications resource.
     *
     * @param modificationsResource the modifications resource to be imported
     * @param inputType             the modification resource input type
     * @return the modifications set
     * @throws JDOMException
     */
    Set<Modification> importPipelineModifications(Resource modificationsResource, InputType inputType) throws JDOMException;
}
