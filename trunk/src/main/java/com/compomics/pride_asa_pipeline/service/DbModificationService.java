/*
 *

 */
package com.compomics.pride_asa_pipeline.service;

import com.compomics.omssa.xsd.UserModCollection;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.model.SpectrumAnnotatorResult;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jdom2.JDOMException;
import org.springframework.core.io.Resource;

/**
 *
 * @author Niels Hulstaert
 */
public interface DbModificationService extends ModificationService {    
    
    /**
     * Loads the experiment modifications from pride
     *
     * @param completePeptides the experiment complete peptides
     * @return the list of modifications
     */
    Set<Modification> loadExperimentModifications(List<Peptide> completePeptides);

    /**
     * Loads the experiment modifications from a pride experiment
     *
     * @param experimentId the experiment identifier
     * @return the list of modifications
     */
    Set<Modification> loadExperimentModifications(long experimentId);   
}
