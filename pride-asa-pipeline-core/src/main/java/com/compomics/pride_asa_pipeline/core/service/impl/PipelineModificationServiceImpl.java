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
package com.compomics.pride_asa_pipeline.core.service.impl;

import com.compomics.pride_asa_pipeline.core.logic.modification.InputType;
import com.compomics.pride_asa_pipeline.core.logic.modification.ModificationMarshaller;
import com.compomics.pride_asa_pipeline.core.logic.modification.OmssaModificationMarshaller;
import com.compomics.pride_asa_pipeline.core.service.PipelineModificationService;
import com.compomics.pride_asa_pipeline.model.Modification;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.jdom2.JDOMException;
import org.springframework.core.io.Resource;

/**
 * @author Niels Hulstaert
 */
public class PipelineModificationServiceImpl implements PipelineModificationService {

    protected ModificationMarshaller modificationMarshaller;
    protected OmssaModificationMarshaller omssaModificationMarshaller;
    /**
     * The set of pipeline modifications.
     */
    protected Set<Modification> pipelineModifications;

    public ModificationMarshaller getModificationMarshaller() {
        return modificationMarshaller;
    }

    public void setModificationMarshaller(ModificationMarshaller modificationMarshaller) {
        this.modificationMarshaller = modificationMarshaller;
    }

    public OmssaModificationMarshaller getOmssaModificationMarshaller() {
        return omssaModificationMarshaller;
    }

    public void setOmssaModificationMarshaller(OmssaModificationMarshaller omssaModificationMarshaller) {
        this.omssaModificationMarshaller = omssaModificationMarshaller;
    }

    @Override
    public Set<Modification> loadPipelineModifications(Resource modificationsResource, InputType inputType) throws JDOMException {
        //return the modifications or first unmarshall them from the specified
        //configuration file if not done so before
        if (pipelineModifications == null) {
            loadPipelineModificationsFromResource(modificationsResource, inputType);
        }
        return pipelineModifications;
    }

    @Override
    public void savePipelineModifications(Resource modificationsResource, Collection<Modification> newPipelineModifications) {
        modificationMarshaller.marshall(modificationsResource, newPipelineModifications);
        //replace the current pipeline modifications
        pipelineModifications.clear();
        for (Modification modification : newPipelineModifications) {
            pipelineModifications.add(modification);
        }
    }

    @Override
    public Set<Modification> importPipelineModifications(Resource modificationsResource, InputType inputType) throws JDOMException {
        Set<Modification> modifications = modificationMarshaller.unmarshall(modificationsResource);
        loadPipelineModifications(modificationsResource, inputType);
        return modifications;
    }

    /**
     * Load the modifications from file. The modification set will be initialized.
     *
     * @param modificationsResource the modifications resource
     * @throws JDOMException thrown in case of a xml parse exception
     */
    private void loadPipelineModificationsFromResource(Resource modificationsResource, InputType inputType) throws JDOMException {
        pipelineModifications = new HashSet<>();
        switch (inputType) {
            case PRIDE_ASAP:
                pipelineModifications.addAll(modificationMarshaller.unmarshall(modificationsResource));
                break;
            case OMSSA:
                pipelineModifications.addAll(omssaModificationMarshaller.unmarshall(modificationsResource));
        }
    }

}
