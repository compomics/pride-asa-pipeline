
package com.compomics.pride_asa_pipeline.repository.impl;

import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.repository.ModificationRepository;
import com.compomics.pride_asa_pipeline.repository.PrideXmlParser;
import java.util.List;

/**
 *
 * @author Niels Hulstaert
 */
public class PrideXmlModificationRepository implements ModificationRepository {
    
    private PrideXmlParser prideXmlParser;
     
    @Override
    public List<Modification> getModificationsByPeptideId(long peptideId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Modification> getModificationsByExperimentId(long experimentId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
