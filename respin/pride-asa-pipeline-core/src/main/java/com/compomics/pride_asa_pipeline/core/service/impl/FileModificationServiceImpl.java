package com.compomics.pride_asa_pipeline.core.service.impl;

import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.core.service.FileModificationService;
import com.compomics.respindataextractor.dataextraction.extractors.parameters.FileParser;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Niels Hulstaert
 */
public class FileModificationServiceImpl extends ModificationServiceImpl implements FileModificationService {

    private FileParser fileParser;    

    public FileModificationServiceImpl() {        
    }   

    @Override
    public void setFileParser(FileParser fileParser) {
        this.fileParser = fileParser;
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
       
}
