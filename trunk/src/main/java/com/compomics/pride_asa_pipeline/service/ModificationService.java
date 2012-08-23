/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.service;

import com.compomics.omssa.xsd.UserModCollection;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.model.SpectrumAnnotatorResult;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jdom2.JDOMException;
import org.springframework.core.io.Resource;

/**
 *
 * @author niels
 */
public interface ModificationService {

    /**
     * This method will read and parse the modification definitions specified in
     * the modification resource into a List of Modification objects. Note: the
     * modification resource is only parsed once and the so acquired
     * modification objects cached in the list for later retrieval. Changes to
     * the modification resource after the first call of this method will
     * therefore have no effect.
     *
     * @param modificationsResource the modifications XML resource
     * @return the set of modifications as specified in the modification
     * resource or null if the modification resource does not exist or could not
     * be parsed.
     * @exception JDOMException
     */
    Set<Modification> loadPipelineModifications(Resource modificationsResource) throws JDOMException;

    /**
     * Saves the pipeline modifications to the modifications.xml resource.
     *
     * @param modificationsResource the modifications XML resource
     * @param pipelineModifications the collections of pipeline modifications
     */
    void savePipelineModifications(Resource modificationsResource, Collection<Modification> pipelineModifications);

    /**
     * Imports the pipeline modifications from a given the modifications
     * resource.
     *
     * @param modificationsResource the modifications resource to be imported
     * @param pipelineModifications the collections of pipeline modifications
     * @return the modifications set
     * @exception JDOMException
     */
    Set<Modification> importPipelineModifications(Resource modificationsResource) throws JDOMException;

    /**
     * Loads the experiment modifications from pride
     *
     * @param completePeptides the experiment complete peptides
     * @return the list of modifications
     */
    Set<Modification> loadExperimentModifications(List<Peptide> completePeptides);

    /**
     * Loads the experiment modifications from a pride experiment
     *
     * @param experimentId the experiment identifier
     * @return the list of modifications
     */
    Set<Modification> loadExperimentModifications(long experimentId);

    /**
     * Gets the modifications as a map (key: modification, value: occurence
     * count) that were actually used in the pipeline; i.e. modifications that
     * could be combined the explain a certain mass delta for a precursor.
     *
     * @param spectrumAnnotatorResult the spectrum annotator result
     * @return the used modifications
     */
    Map<Modification, Integer> getUsedModifications(SpectrumAnnotatorResult spectrumAnnotatorResult);

    /**
     * Gets the modifications that were actually used in the pipeline; i.e.
     * modifications that could be combined the explain a certain mass delta for
     * a precursor.
     *
     * @param spectrumAnnotatorResult the spectrum annotator result
     * @return the used modifications
     */
    UserModCollection getModificationsAsUserModCollection(SpectrumAnnotatorResult spectrumAnnotatorResult);
}
