/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.logic;

import com.compomics.pride_asa_pipeline.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.model.*;
import com.compomics.pride_asa_pipeline.service.ModificationService;
import com.compomics.pride_asa_pipeline.util.ResourceUtils;
import java.io.IOException;
import java.util.Set;
import static junit.framework.Assert.assertEquals;
import org.apache.log4j.Logger;
import org.jdom2.JDOMException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Niels Hulstaert Hulstaert
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:springXMLConfig.xml")
public class ModificationCombinationSolverTest {

    private static final Logger LOGGER = Logger.getLogger(ModificationCombinationSolverTest.class);
    private static final String peptideSequenceString = "AAKENNYLENNART";
    @Autowired
    private ModificationCombinationSolver modificationCombinationSolver;
    @Autowired
    private ModificationService modificationService;

    @Before
    public void initialize() throws IOException, JDOMException {
        //Use @Before instead of @BeforeClass because @Autowired doesn't work with static,
        //so check if the modification holder is already set.
        if (modificationCombinationSolver.getModificationHolder() == null) {
            //add the pipeline modifications
            ModificationHolder modificationHolder = new ModificationHolder();
            Resource modificationsResource = ResourceUtils.getResourceByRelativePath(PropertiesConfigurationHolder.getInstance().getString("modification.pipeline_modifications_file"));
            modificationHolder.addModifications(modificationService.loadPipelineModifications(modificationsResource));

            //set the modification combination holder
            modificationCombinationSolver.setModificationHolder(modificationHolder);
        }
    }

    @Test
    public void testFindModificationCombinations_1() throws UnknownAAException, AASequenceMassUnknownException {
        //init peptide with mass delta of 42D        
        Peptide peptide = new Peptide(1, 1649.8D, new AminoAcidSequence(peptideSequenceString));

        Set<ModificationCombination> modificationCombinations = modificationCombinationSolver.findModificationCombinations(peptide, 1, peptide.calculateMassDelta(), 0.3);

        //two modifications with mass of 42D can explain the mass delta, so the size of the set must be 2.
        assertEquals(2, modificationCombinations.size());
        for (ModificationCombination modificationCombination : modificationCombinations) {
            //each modification combination should only contain 1 modification
            assertEquals(1, modificationCombination.getSize());
        }
    }

    @Test
    public void testFindModificationCombinations_2() throws UnknownAAException, AASequenceMassUnknownException {
        //init peptide with mass delta of 42D        
        Peptide peptide = new Peptide(1, 1748.8D, new AminoAcidSequence(peptideSequenceString));

        Set<ModificationCombination> modificationCombinations = modificationCombinationSolver.findModificationCombinations(peptide, 3, peptide.calculateMassDelta(), 0.3);

        //only the 3 modifications combined can explain the mass delta, so the size must be 1.
        assertEquals(1, modificationCombinations.size());
        for (ModificationCombination modificationCombination : modificationCombinations) {
            //the modification combination should contain the three modifications
            assertEquals(3, modificationCombination.getSize());
        }
    }
}
