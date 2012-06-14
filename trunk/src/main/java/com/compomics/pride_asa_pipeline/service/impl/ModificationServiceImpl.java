/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.service.impl;

import com.compomics.omssa.xsd.UserModCollection;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.Modification.Location;
import com.compomics.pride_asa_pipeline.model.ModifiedPeptide;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.model.SpectrumAnnotatorResult;
import com.compomics.pride_asa_pipeline.modification.ModificationMarshaller;
import com.compomics.pride_asa_pipeline.modification.OmssaModiciationMarshaller;
import com.compomics.pride_asa_pipeline.repository.ModificationRepository;
import com.compomics.pride_asa_pipeline.service.ModificationService;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import org.apache.log4j.Logger;

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
    public Set<Modification> loadPipelineModifications(String modificationsFilePath) {
        //return the modifications or first unmarshall them from the specified
        //configuration file if not done so before
        if (pipelineModifications == null) {
            loadPipelineModificationsFromFile(modificationsFilePath);
        }
        return pipelineModifications;
    }

    @Override
    public void savePipelineModifications(String modificationsFilePath, Collection<Modification> newPipelineModifications) {
        modificationMarshaller.marshall(modificationsFilePath, newPipelineModifications);
        //replace the current pipeline modifications
        pipelineModifications.clear();
        for (Modification modification : newPipelineModifications) {
            pipelineModifications.add(modification);
        }
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
    public Set<Modification> getUsedModifications(SpectrumAnnotatorResult spectrumAnnotatorResult) {
        Set<Modification> modifications = new HashSet<Modification>();
        for (ModifiedPeptide modifiedPeptide : spectrumAnnotatorResult.getModifiedPrecursors().values()) {
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
     * @param modificationFilePath the modifications file path
     */
    private void loadPipelineModificationsFromFile(String modificationsFilePath) {
        pipelineModifications = new HashSet<Modification>();
        File modificationFile = null;
        //check if it exists
        URL url = this.getClass().getClassLoader().getResource(modificationsFilePath);
        try {
            modificationFile = new File(url.toURI());
        } catch (URISyntaxException e) {
            //this should not happen, since we get the URL from the ClassLoader
            LOGGER.error("URL " + url.toString() + " for resource could did not match expected syntax!");
        }
        if (modificationFile == null || !modificationFile.exists() || !modificationFile.canRead()) {
            LOGGER.warn("Specified modification file " + modificationsFilePath + " does not exist or could not be accessed! ");
        } else {
            //read the file and create Modification objects for all its entries                
            pipelineModifications.addAll(modificationMarshaller.unmarshall(modificationFile));
        }
    }
}
