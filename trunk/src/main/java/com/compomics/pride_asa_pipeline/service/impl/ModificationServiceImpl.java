/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.service.impl;

import com.compomics.omssa.xsd.UserModCollection;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.Modification.Location;
import com.compomics.pride_asa_pipeline.model.ModifiedPeptide;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.model.SpectrumAnnotatorResult;
import com.compomics.pride_asa_pipeline.modification.ModificationMarshaller;
import com.compomics.pride_asa_pipeline.modification.OmssaModiciationMarshaller;
import com.compomics.pride_asa_pipeline.repository.ModificationRepository;
import com.compomics.pride_asa_pipeline.service.ModificationService;
import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;
import org.jdom2.JDOMException;

/**
 *
 * @author niels
 */
public class ModificationServiceImpl implements ModificationService {

    private static final Logger LOGGER = Logger.getLogger(ModificationServiceImpl.class);
    private ModificationMarshaller modificationMarshaller;
    private ModificationRepository modificationRepository;
    private OmssaModiciationMarshaller omssaModiciationMarshaller;
    private Set<Modification> pipelineModifications;

    public ModificationMarshaller getModificationMarshaller() {
        return modificationMarshaller;
    }

    public void setModificationMarshaller(ModificationMarshaller modificationMarshaller) {
        this.modificationMarshaller = modificationMarshaller;
    }

    public ModificationRepository getModificationRepository() {
        return modificationRepository;
    }

    public void setModificationRepository(ModificationRepository modificationRepository) {
        this.modificationRepository = modificationRepository;
    }

    public OmssaModiciationMarshaller getOmssaModiciationMarshaller() {
        return omssaModiciationMarshaller;
    }

    public void setOmssaModiciationMarshaller(OmssaModiciationMarshaller omssaModiciationMarshaller) {
        this.omssaModiciationMarshaller = omssaModiciationMarshaller;
    }

    @Override
    public Set<Modification> loadPipelineModifications(File modificationsFile) throws JDOMException {
        //return the modifications or first unmarshall them from the specified
        //configuration file if not done so before
        if (pipelineModifications == null) {
            loadPipelineModificationsFromFile(modificationsFile);
        }
        return pipelineModifications;
    }

    @Override
    public void savePipelineModifications(File modificationsFile, Collection<Modification> newPipelineModifications) {
        modificationMarshaller.marshall(modificationsFile, newPipelineModifications);
        //replace the current pipeline modifications
        pipelineModifications.clear();
        for (Modification modification : newPipelineModifications) {
            pipelineModifications.add(modification);
        }
    }

    @Override
    public Set<Modification> importPipelineModifications(File modificationsFile) throws JDOMException {
        Set<Modification> modifications = modificationMarshaller.unmarshall(modificationsFile);

        return modifications;
    }

    @Override
    public Set<Modification> loadExperimentModifications(List<Peptide> completePeptides) {
        Map<String, Modification> modificationMap = new HashMap<String, Modification>();

        //iterate over the complete peptides and retrieve for each peptide the modifications stored in pride
        List<Modification> modificationList = null;
        for (Peptide peptide : completePeptides) {
            modificationList = modificationRepository.getModificationsByPeptideId(peptide.getPeptideId());
            //add the peptide modifications to the modifications
            for (Modification modification : modificationList) {
                addModificationToModifications(modification, modificationMap);
            }
        }

        //add modifications to set
        Set<Modification> modifications = new HashSet<Modification>();
        modifications.addAll(modificationMap.values());

        return modifications;
    }

    @Override
    public Set<Modification> loadExperimentModifications(long experimentId) {

        Set<String> modificationNames = Sets.newHashSet();
        Set<Modification> modificationSet = Sets.newHashSet();

        List<Modification> lModificationsByExperimentId = modificationRepository.getModificationsByExperimentId(experimentId);
        for (Modification lModification : lModificationsByExperimentId) {
            boolean lAdd = modificationNames.add(lModification.getAccession()
                    + "_"
                    + Joiner.on("").join(lModification.getAffectedAminoAcids())
                    + "_" + lModification.getMassShift());
            if(lAdd){
                // Unique Unimod + location + mass combination
                modificationSet.add(lModification);
            }

        }
        return modificationSet;
    }

    @Override
    public Set<Modification> getUsedModifications(SpectrumAnnotatorResult spectrumAnnotatorResult) {
        Set<Modification> modifications = new HashSet<Modification>();
        for (Identification identification : spectrumAnnotatorResult.getModifiedPrecursors()) {
            ModifiedPeptide modifiedPeptide = (ModifiedPeptide) identification.getPeptide();
            if (modifiedPeptide.getNTermMod() != null) {
                modifications.add((Modification) modifiedPeptide.getNTermMod());
            }
            if (modifiedPeptide.getNTModifications() != null) {
                for (int i = 0; i < modifiedPeptide.getNTModifications().length; i++) {
                    Modification modification = (Modification) modifiedPeptide.getNTModifications()[i];
                    if (modification != null) {
                        modifications.add(modification);
                    }
                }
            }
            if (modifiedPeptide.getCTermMod() != null) {
                modifications.add((Modification) modifiedPeptide.getNTermMod());
            }
        }

        return modifications;
    }

    @Override
    public UserModCollection getModificationsAsUserModCollection(SpectrumAnnotatorResult spectrumAnnotatorResult) {
        Set<Modification> modifications = getUsedModifications(spectrumAnnotatorResult);
        return omssaModiciationMarshaller.marshallModifications(modifications);
    }

    /**
     * Adds a modification to the modifications. If the modification is already
     * present, check if the location needs to be updated.
     *
     * @param modification
     * @param modifications
     */
    private void addModificationToModifications(Modification modification, Map<String, Modification> modifications) {
        if (modifications.containsKey(modification.getName())) {
            Modification presentModification = modifications.get(modification.getName());
            //update affected amino acids
            presentModification.getAffectedAminoAcids().addAll(modification.getAffectedAminoAcids());
            //check if location is the same as the present one
            //if the location differs, update it to Location.NON_TERMINAL
            if (!presentModification.getLocation().equals(Location.NON_TERMINAL) && !presentModification.getLocation().equals(modification.getLocation())) {
                presentModification.setLocation(Location.NON_TERMINAL);
            }
        } else {
            modifications.put(modification.getName(), modification);
        }
    }

    /**
     * Loads the modifications from file.
     *
     * @param modificationFileName the modifications file name
     */
    private void loadPipelineModificationsFromFile(File modificationsFile) throws JDOMException {
        pipelineModifications = new HashSet<Modification>();
        pipelineModifications.addAll(modificationMarshaller.unmarshall(modificationsFile));
    }
}
