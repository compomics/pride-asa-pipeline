package com.compomics.pride_asa_pipeline.core.service.impl;

import com.compomics.pride_asa_pipeline.core.repository.FileParser;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.core.service.FileModificationService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.CachedDataAccessController;

/**
 *
 * @author Niels Hulstaert
 */
public class FileModificationServiceImpl extends ModificationServiceImpl implements FileModificationService {

    private FileParser fileParser;
    private String assayIdentifier;

    public FileModificationServiceImpl() {
    }

    @Override
    public Set<Modification> loadExperimentModifications() {
        Map<String, Modification> modificationMap = new HashMap<>();

        //get modifications from parser
        List<Modification> modificationList = fileParser.getModifications();
        for (Modification modification : modificationList) {
            addModificationToModifications(modification, modificationMap);
        }

        //add modifications to set
        Set<Modification> modifications = new HashSet<>();
        modifications.addAll(modificationMap.values());

        return modifications;
    }

    @Override
    public void setActiveAssay(String assayIdentifier) {
       this.assayIdentifier=assayIdentifier;
    }


}