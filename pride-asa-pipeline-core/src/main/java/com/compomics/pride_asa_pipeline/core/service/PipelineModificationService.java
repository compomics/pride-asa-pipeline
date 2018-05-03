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
