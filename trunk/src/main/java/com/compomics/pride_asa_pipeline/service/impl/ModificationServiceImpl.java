/*
 *

 */
package com.compomics.pride_asa_pipeline.service.impl;

import com.compomics.omssa.xsd.UserModCollection;
import com.compomics.pride_asa_pipeline.logic.modification.ModificationMarshaller;
import com.compomics.pride_asa_pipeline.logic.modification.OmssaModificationMarshaller;
import com.compomics.pride_asa_pipeline.model.*;
import com.compomics.pride_asa_pipeline.service.ModificationService;
import static com.compomics.pride_asa_pipeline.service.ModificationService.MASS_DELTA_TOLERANCE;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import org.jdom2.JDOMException;
import org.springframework.core.io.Resource;

import java.util.*;
import org.apache.log4j.Logger;

/**
 *
 * @author Niels Hulstaert
 */
public abstract class ModificationServiceImpl implements ModificationService {

    private static final Logger LOGGER = Logger.getLogger(ModificationServiceImpl.class);
    private static final String[] ms1ModificationNames = {"itraq", "tmt"};
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
        Map<Modification, Integer> modifications = new HashMap<>();

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
    public Map<Modification, Double> estimateModificationRate(Map<Modification, Integer> usedModifications, SpectrumAnnotatorResult spectrumAnnotatorResult, double fixedModificationThreshold) {

        // calculate number of peptides that contain
        HashMultiset<AminoAcid> aminoAcidHashMultiset = HashMultiset.create();
        List<Identification> identifications = spectrumAnnotatorResult.getIdentifications();
        for (Identification identification : identifications) {
            String sequenceString = identification.getPeptide().getSequenceString();
            for (AminoAcid aa : EnumSet.allOf(AminoAcid.class)) {
                if (sequenceString.contains("" + aa.letter())) {
                    aminoAcidHashMultiset.add(aa);
                }
            }
        }

        // convert to aminoacid/integer map
        Map<AminoAcid, Integer> aminoAcidCounts = Maps.newHashMap();
        for (Multiset.Entry entry : aminoAcidHashMultiset.entrySet()) {
            aminoAcidCounts.put((AminoAcid) entry.getElement(), entry.getCount());
        }

        // run over used modifications and their frequencies,
        // verify which Modifications are nearly always present when the
        // affected amino acid is in the peptide sequence
        Map<Modification, Double> result = Maps.newHashMap();
        for (Modification usedModifiation : usedModifications.keySet()) {
            double modificationCount = new Double(usedModifications.get(usedModifiation));
            double aminoAcidCount = 0;
            for (AminoAcid affectedAminoAcid : usedModifiation.getAffectedAminoAcids()) {
                aminoAcidCount += aminoAcidCounts.get(affectedAminoAcid);
            }

            // Fix the fixed modification treshold at 95%
            double modificationRate = modificationCount / aminoAcidCount;
            result.put(usedModifiation, modificationRate);
        }

        return result;

    }

    @Override
    public UserModCollection getModificationsAsUserModCollection(SpectrumAnnotatorResult spectrumAnnotatorResult) {
        Set<Modification> modifications = getUsedModifications(spectrumAnnotatorResult).keySet();
        return omssaModificationMarshaller.marshallModifications(modifications);
    }

    @Override
    public Set<Modification> filterModifications(ModificationHolder modificationHolder, Set<Modification> modifications) {
        Set<Modification> conflictingModifications = new HashSet<>();

        //first, check for mods with too many affected amino acids/locations
        for (Modification modification : modifications) {
            //only look for non-terminal mods            
            if (modification.getLocation().equals(Modification.Location.NON_TERMINAL) && modification.getAffectedAminoAcids().size() > MAX_AFFECTED_AMINO_ACIDS) {
                LOGGER.info("Excluded PRIDE modification " + modification.getAccession() + " (" + modification.getName() + ") with too many affected amino acids.");
                conflictingModifications.add(modification);
            }
        }

        //second, check for mods with equal masses within the pride mods
        Modification[] modificationsArray = modifications.toArray(new Modification[0]);
        for (int i = 0; i < modificationsArray.length; i++) {
            for (int j = (modificationsArray.length - 1); j > i && j >= 0; j--) {
                //first, check for equal masses
                if ((Math.abs(modificationsArray[i].getMonoIsotopicMassShift() - modificationsArray[j].getMonoIsotopicMassShift())) < MASS_DELTA_TOLERANCE) {
                    //second, check for same location
                    if (modificationsArray[i].getLocation().equals(modificationsArray[j].getLocation())) {
                        //third, check for same affected amino acids
                        for (AminoAcid aminoAcid : modificationsArray[i].getAffectedAminoAcids()) {
                            if (modificationsArray[j].getAffectedAminoAcids().contains(aminoAcid)) {
                                LOGGER.info("Excluded PRIDE modification " + modificationsArray[j].getAccession() + " (" + modificationsArray[j].getName() + ") with equal mass.");
                                conflictingModifications.add(modificationsArray[j]);
                                //break as soon as one of the affected amino acids is the same
                                break;
                            }
                        }
                    }
                }
            }
        }

        //third, check for modifications with the same mass compared to the pipeline modifications
        for (Modification modification : modificationHolder.getAllModifications()) {
            for (Modification modificationToCheck : modifications) {
                //first, check for equal masses
                if ((Math.abs(modification.getMonoIsotopicMassShift() - modificationToCheck.getMonoIsotopicMassShift())) < MASS_DELTA_TOLERANCE) {
                    //second, check for same location
                    if (modification.getLocation().equals(modificationToCheck.getLocation())) {
                        //third, check for same affected amino acids
                        for (AminoAcid aminoAcid : modification.getAffectedAminoAcids()) {
                            if (modificationToCheck.getAffectedAminoAcids().contains(aminoAcid)) {
                                LOGGER.info("Excluded PRIDE modification " + modificationToCheck.getAccession() + " (" + modificationToCheck.getName() + ") with equal mass.");
                                conflictingModifications.add(modificationToCheck);
                                //break as soon as one of the affected amino acids is the same
                                break;
                            }
                        }
                    }
                }
            }
        }

        return conflictingModifications;
    }

    /**
     * Adds a modification to the modifications. If the modification is already
     * present, check if the location needs to be updated.
     *
     * @param modification the modification
     * @param modifications the map of modifications (key: modification name;
     * value: modification)
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
            //check for a MS1 modification       
            for (String ms1ModName : ms1ModificationNames) {
                if (modification.getName().toLowerCase().contains(ms1ModName)) {
                    modification.setType(Modification.Type.MS1);
                }
            }
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
        pipelineModifications = new HashSet<>();
        pipelineModifications.addAll(modificationMarshaller.unmarshall(modificationsResource));
    }
}
