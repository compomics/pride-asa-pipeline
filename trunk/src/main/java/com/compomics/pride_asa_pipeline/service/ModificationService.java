/*
 *

 */
package com.compomics.pride_asa_pipeline.service;

import com.compomics.omssa.xsd.UserModCollection;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.ModificationHolder;
import com.compomics.pride_asa_pipeline.model.SpectrumAnnotatorResult;
import org.jdom2.JDOMException;
import org.springframework.core.io.Resource;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Niels Hulstaert
 */
public interface ModificationService {

    /**
     * The mass delta tolerance value used for comparing mod masses
     */
    public static final double MASS_DELTA_TOLERANCE = 0.1;
    /**
     * The maximum number of affected amino acids a non-terminal (PRIDE)
     * modification is allowed to have
     */
    public static final int MAX_AFFECTED_AMINO_ACIDS = 10;

    /**
     * This method will read and parse the modification definitions specified in
     * the modification resource into a Set of Modification objects. Note: the
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
     * Saves the pipeline modifications to the pride_asap_modifications.xml
     * resource.
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
     * @return the modifications set
     * @exception JDOMException
     */
    Set<Modification> importPipelineModifications(Resource modificationsResource) throws JDOMException;

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
     * Gets the modifications as a map (key: modification, value: occurence
     * count) that were actually used in the pipeline; i.e. modifications that
     * could be combined the explain a certain mass delta for a precursor.
     *
     * @param usedModifications map returned by the getUsedModifications method
     * @param spectrumAnnotatorResult the spectrum annotator result
     * @return a Map with the used modifications as keys, and Double values that
     * are True when the corresponding Modification. e.g the rate is 0.95 if 95%
     * of all Identifications that can be targeted by a modification are
     * actually modified
     */
    Map<Modification, Double> estimateModificationRate(Map<Modification, Integer> usedModifications, SpectrumAnnotatorResult spectrumAnnotatorResult, double aFixedModificationThreshold);

    /**
     * Gets the modifications that were actually used in the pipeline; i.e.
     * modifications that could be combined the explain a certain mass delta for
     * a precursor.
     *
     * @param spectrumAnnotatorResult the spectrum annotator result
     * @return the used modifications
     */
    UserModCollection getModificationsAsUserModCollection(SpectrumAnnotatorResult spectrumAnnotatorResult);

    /**
     * Finds the modifications from the given set that have a modification in
     * the modification holder with the same monoisotopic mass (with an error
     * tolerance MASS_DELTA_TOLERANCE). Also check modifications that could
     * occur on too many positions.
     *
     * @param modificationHolder the ModificationHolder
     * @param modifications the given set of modifications
     * @return the set of filtered modifications
     */
    Set<Modification> filterModifications(ModificationHolder modificationHolder, Set<Modification> modifications);
}
