/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.logic;

import com.compomics.pride_asa_pipeline.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.model.*;
import com.compomics.pride_asa_pipeline.service.ModificationService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static junit.framework.Assert.*;

/**
 *
 * @author Niels Hulstaert
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:springXMLConfig.xml")
public class MassDeltaExplainerTest {

    @Autowired
    private MassDeltaExplainer massDeltaExplainer;
    @Autowired
    private ModificationCombinationSolver modificationCombinationSolver;
    @Autowired
    private ModificationService modificationService;

    @Before
    public void initialize() {
        //add the pipeline modifications
        ModificationHolder modificationHolder = new ModificationHolder();
        modificationHolder.addModifications(modificationService.loadPipelineModifications(PropertiesConfigurationHolder.getInstance().getString("modification.pipeline_modifications_file_name")));

        //set the modification combination holder
        modificationCombinationSolver.setModificationHolder(modificationHolder);                                        
    }
    
    @Test
    public void testExplainCompleteIndentifications() throws UnknownAAException{
        List<Identification> identifications = new ArrayList<Identification>();
        
        //add identifications
        Peptide peptide = new Peptide(1, 1649.8D, new AminoAcidSequence("AAKENNYLENNART"));
        Identification identification_1 = new Identification(peptide, null, 0, 0);
        identifications.add(identification_1);
        peptide = new Peptide(1, 1706.8D, new AminoAcidSequence("AAKENNYLENNART"));
        Identification identification_2 = new Identification(peptide, null, 0, 0);
        identifications.add(identification_2);
        peptide = new Peptide(1, 1748.8D, new AminoAcidSequence("AAKENNYLENNART"));
        Identification identification_3 = new Identification(peptide, null, 0, 0);
        identifications.add(identification_3);
        peptide = new Peptide(1, 1752.8D, new AminoAcidSequence("AAKENNYLENNART"));
        Identification identification_4 = new Identification(peptide, null, 0, 0);
        identifications.add(identification_4);
        
        Map<Identification, Set<ModificationCombination>> explanations = massDeltaExplainer.explainCompleteIndentifications(identifications);
        
        //the mass deltas of three identifications can be explained,
        //so the the size of the map should be equal to 3
        assertEquals(3, explanations.size());
        //check if the modification combination sets are not empty
        assertFalse(explanations.get(identification_1).isEmpty());
        assertFalse(explanations.get(identification_2).isEmpty());
        assertFalse(explanations.get(identification_3).isEmpty());        
    }
}
