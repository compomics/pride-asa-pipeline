package com.compomics.pride_asa_pipeline.service.impl;

import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.repository.PrideXmlParser;
import com.compomics.pride_asa_pipeline.service.PrideXmlModificationService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Niels Hulstaert
 */
public class PrideXmlModificationServiceImpl extends ModificationServiceImpl implements PrideXmlModificationService {

    private PrideXmlParser prideXmlParser;
    
    public PrideXmlParser getPrideXmlParser() {
        return prideXmlParser;
    }

    public void setPrideXmlParser(PrideXmlParser prideXmlParser) {
        this.prideXmlParser = prideXmlParser;
    }
           
    @Override
    public Set<Modification> loadExperimentModifications() {
        Map<String, Modification> modificationMap = new HashMap<String, Modification>();

        //get modifications from parser
        List<Modification> modificationList = prideXmlParser.getModifications();
        for (Modification modification : modificationList) {
            addModificationToModifications(modification, modificationMap);
        }

        //add modifications to set
        Set<Modification> modifications = new HashSet<Modification>();
        modifications.addAll(modificationMap.values());

        return modifications;
    }    
    
   
}
