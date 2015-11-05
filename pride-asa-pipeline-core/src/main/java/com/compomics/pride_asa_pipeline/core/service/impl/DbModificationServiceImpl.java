/*
 *

 */
package com.compomics.pride_asa_pipeline.core.service.impl;

import com.compomics.pride_asa_pipeline.core.repository.ModificationRepository;
import com.compomics.pride_asa_pipeline.core.service.DbModificationService;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Niels Hulstaert
 */
public class DbModificationServiceImpl extends ModificationServiceImpl implements DbModificationService {

    private ModificationRepository modificationRepository;

    public ModificationRepository getModificationRepository() {
        return modificationRepository;
    }

    public void setModificationRepository(ModificationRepository modificationRepository) {
        this.modificationRepository = modificationRepository;
    }

    @Override
    public Set<Modification> loadExperimentModifications(List<Peptide> completePeptides) {
        Map<String, Modification> modificationMap = new HashMap<>();

        //iterate over the complete peptides and retrieve for each peptide the modifications stored in pride
        List<Modification> modificationList;
        for (Peptide peptide : completePeptides) {
            modificationList = modificationRepository.getModificationsByPeptideId(peptide.getPeptideId());
            //add the peptide modifications to the modifications
            for (Modification modification : modificationList) {
                addModificationToModifications(modification, modificationMap);
            }
        }

        //add modifications to set
        Set<Modification> modifications = new HashSet<>();
        modifications.addAll(modificationMap.values());

        return modifications;
    }

    @Override
    public Set<Modification> loadExperimentModifications(String experimentId) {

        Set<String> modificationNames = Sets.newHashSet();
        Set<Modification> modificationSet = Sets.newHashSet();

        List<Modification> lModificationsByExperimentId = modificationRepository.getModificationsByExperimentId(experimentId);
        for (Modification lModification : lModificationsByExperimentId) {
            boolean lAdd = modificationNames.add(lModification.getAccession()
                    + "_"
                    + Joiner.on("").join(lModification.getAffectedAminoAcids())
                    + "_" + lModification.getMassShift());
            if (lAdd) {
                // Unique Unimod + location + mass combination
                modificationSet.add(lModification);
            }

        }
        return modificationSet;
    }
    
}
