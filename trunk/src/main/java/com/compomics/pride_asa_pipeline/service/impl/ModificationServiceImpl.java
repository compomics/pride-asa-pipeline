/*
 *

 */
package com.compomics.pride_asa_pipeline.service.impl;

import com.compomics.omssa.xsd.UserModCollection;
import com.compomics.pride_asa_pipeline.logic.modification.ModificationMarshaller;
import com.compomics.pride_asa_pipeline.logic.modification.OmssaModificationMarshaller;
import com.compomics.pride_asa_pipeline.model.*;
import com.compomics.pride_asa_pipeline.service.ModificationService;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import org.jdom2.JDOMException;
import org.springframework.core.io.Resource;

import java.util.*;

/**
 *
 * @author Niels Hulstaert
 */
public abstract class ModificationServiceImpl implements ModificationService {

    protected ModificationMarshaller modificationMarshaller;
    protected OmssaModificationMarshaller omssaModificationMarshaller;
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
    public Set<Modification> loadPipelineModifications(Resource modificationsResource) throws JDOMException {
        //return the modifications or first unmarshall them from the specified
        //configuration file if not done so before
        if (pipelineModifications == null) {
            loadPipelineModificationsFromResource(modificationsResource);
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

        return modifications;
    }

    @Override
    public Map<Modification, Integer> getUsedModifications(SpectrumAnnotatorResult spectrumAnnotatorResult) {
        Map<Modification, Integer> modifications = new HashMap<Modification, Integer>();

        for (Identification identification : spectrumAnnotatorResult.getModifiedPrecursors()) {
            ModifiedPeptide modifiedPeptide = (ModifiedPeptide) identification.getPeptide();
            if (modifiedPeptide.getNTermMod() != null) {
                addModificationToMap(modifications, (Modification) modifiedPeptide.getNTermMod());
            }
            if (modifiedPeptide.getNTModifications() != null) {
                for (int i = 0; i < modifiedPeptide.getNTModifications().length; i++) {
                    Modification modification = (Modification) modifiedPeptide.getNTModifications()[i];
                    if (modification != null) {
                        addModificationToMap(modifications, modification);
                    }
                }
            }
            if (modifiedPeptide.getCTermMod() != null) {
                addModificationToMap(modifications, (Modification) modifiedPeptide.getCTermMod());
            }
        }

        return modifications;
    }

    @Override
    public Map<Modification, Double> estimateModificationRate(Map<Modification, Integer> usedModifications, SpectrumAnnotatorResult spectrumAnnotatorResult, double aFixedModificationThreshold) {

        // calculate number of peptides that contain
        HashMultiset<AminoAcid> lAminoAcidHashMultiset = HashMultiset.create();
        List<Identification> lIdentifications = spectrumAnnotatorResult.getIdentifications();
        for (Identification lIdentification : lIdentifications) {
            String lSequenceString = lIdentification.getPeptide().getSequenceString();
            for (AminoAcid aa : EnumSet.allOf(AminoAcid.class)) {
                if(lSequenceString.contains("" + aa.letter())){
                    lAminoAcidHashMultiset.add(aa);
                }
            }
        }

        // convert to aminoacid/integer map
        HashMap<AminoAcid, Integer> lAminoAcidCounts = Maps.newHashMap();
        for (Multiset.Entry lEntry : lAminoAcidHashMultiset.entrySet()) {
            lAminoAcidCounts.put((AminoAcid) lEntry.getElement(), lEntry.getCount());
        }


        // run over used modifications and their frequencies,
        // verify which Modifications are nearly always present when the
        // affected amino acid is in the peptide sequence
        HashMap<Modification, Double> lResult = Maps.newHashMap();
        for (Modification lUsedModifiation : usedModifications.keySet()) {
            double lModificationCount = new Double(usedModifications.get(lUsedModifiation));
            double lAminoAcidCount = 0;
            for (AminoAcid lAffectedAminoAcid : lUsedModifiation.getAffectedAminoAcids()) {
                lAminoAcidCount += lAminoAcidCounts.get(lAffectedAminoAcid);
            }

            // Fix the fixed modification treshold at 95%
            double lModificationRate =  lModificationCount / lAminoAcidCount;
            lResult.put(lUsedModifiation, lModificationRate);
        }

        return lResult;

    }

    @Override
    public UserModCollection getModificationsAsUserModCollection(SpectrumAnnotatorResult spectrumAnnotatorResult) {
        Set<Modification> modifications = getUsedModifications(spectrumAnnotatorResult).keySet();
        return omssaModificationMarshaller.marshallModifications(modifications);
    }
    
     /**
     * Adds a modification to the modifications. If the modification is already
     * present, check if the location needs to be updated.
     *
     * @param modification
     * @param modifications
     */
    protected void addModificationToModifications(Modification modification, Map<String, Modification> modifications) {
        if (modifications.containsKey(modification.getName())) {
            Modification presentModification = modifications.get(modification.getName());
            //update affected amino acids
            presentModification.getAffectedAminoAcids().addAll(modification.getAffectedAminoAcids());
            //check if location is the same as the present one
            //if the location differs, update it to Location.NON_TERMINAL
            if (!presentModification.getLocation().equals(Modification.Location.NON_TERMINAL) && !presentModification.getLocation().equals(modification.getLocation())) {
                presentModification.setLocation(Modification.Location.NON_TERMINAL);
            }
        } else {
            modifications.put(modification.getName(), modification);
        }
    }    

    /**
     * Adds a modification to the modifications map. If already present, the
     * occurence count is incremented.
     *
     * @param modifications the modifications map (key: modification, value:
     * modification occurence count)
     * @param modification the modification
     */
    private void addModificationToMap(Map<Modification, Integer> modifications, Modification modification) {
        if (modifications.containsKey(modification)) {
            modifications.put(modification, modifications.get(modification) + 1);
        } else {
            modifications.put(modification, 1);
        }
    }

    /**
     * Loads the modifications from file.
     *
     * @param modificationFileName the modifications file name
     */
    private void loadPipelineModificationsFromResource(Resource modificationsResource) throws JDOMException {
        pipelineModifications = new HashSet<Modification>();
        pipelineModifications.addAll(modificationMarshaller.unmarshall(modificationsResource));
    }
}
