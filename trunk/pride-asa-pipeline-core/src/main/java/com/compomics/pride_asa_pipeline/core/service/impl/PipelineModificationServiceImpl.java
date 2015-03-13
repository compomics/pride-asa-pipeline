/*
 *

 */
package com.compomics.pride_asa_pipeline.core.service.impl;

import com.compomics.pride_asa_pipeline.core.logic.modification.ModificationMarshaller;
import com.compomics.pride_asa_pipeline.core.logic.modification.OmssaModificationMarshaller;
import com.compomics.pride_asa_pipeline.core.logic.modification.impl.OmssaModificationMarshallerImpl;
import com.compomics.pride_asa_pipeline.core.service.PipelineModificationService;
import com.compomics.pride_asa_pipeline.core.util.ResourceUtils;
import com.compomics.pride_asa_pipeline.model.Modification;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.jdom2.JDOMException;
import org.springframework.core.io.Resource;

/**
 *
 * @author Niels Hulstaert
 */
public class PipelineModificationServiceImpl implements PipelineModificationService {

    protected ModificationMarshaller modificationMarshaller;
    protected OmssaModificationMarshaller omssaModificationMarshaller = new OmssaModificationMarshallerImpl();
    protected Set<Modification> pipelineModifications;

    public ModificationMarshaller getModificationMarshaller() {
        return modificationMarshaller;
    }

    public void setModificationMarshaller(ModificationMarshaller modificationMarshaller) {
        this.modificationMarshaller = modificationMarshaller;
    }

    @Override
    public Set<Modification> loadPipelineModifications(Resource modificationsResource) throws JDOMException {
        //return the modifications or first unmarshall them from the specified
        //configuration file if not done so before
        if (pipelineModifications == null) {
           // loadPipelineModificationsFromResource(modificationsResource);
            includeSearchGUIModifications(pipelineModifications);
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
    public Set<Modification> importPipelineModifications(Resource modificationsResource) throws JDOMException {
        Set<Modification> modifications = modificationMarshaller.unmarshall(modificationsResource);
        loadPipelineModifications(modificationsResource);
        return modifications;
    }

    /**
     * Load the modifications from file.
     *
     * @param modificationFileName the modifications file name
     */
    private void loadPipelineModificationsFromResource(Resource modificationsResource) throws JDOMException {
        pipelineModifications = new HashSet<>();
        pipelineModifications.addAll(modificationMarshaller.unmarshall(modificationsResource));
    }

    /**
     * Load the modifications from file.
     *
     * @param modificationFileName the modifications file name
     */
    private void includeSearchGUIModifications(Set<Modification> mods) throws JDOMException {
        mods.addAll(omssaModificationMarshaller.unmarshall(ResourceUtils.getResourceByRelativePath("/resources/searchGUI_to_pride_mods_common.xml")));  
    }
}
