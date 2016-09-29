/*
 *

 */
package com.compomics.pride_asa_pipeline.core.logic;

import com.compomics.pride_asa_pipeline.core.logic.modification.InputType;
import com.compomics.pride_asa_pipeline.model.ModificationHolder;
import com.compomics.pride_asa_pipeline.model.AASequenceMassUnknownException;
import com.compomics.pride_asa_pipeline.model.ModificationCombination;
import com.compomics.pride_asa_pipeline.model.AminoAcidSequence;
import com.compomics.pride_asa_pipeline.model.UnknownAAException;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.core.logic.impl.ModificationCombinationSolverImpl;
import com.compomics.pride_asa_pipeline.core.service.PipelineModificationService;
import com.compomics.pride_asa_pipeline.core.util.ResourceUtils;
import java.io.IOException;
import java.util.Set;
import static org.junit.Assert.assertEquals;
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
public class ModCombSolverEqualMassesTest {

    private ModificationCombinationSolver modificationCombinationSolver;
    @Autowired
    private PipelineModificationService modificationService;

    @Before
    public void initialize() throws IOException, JDOMException {
        //Use @Before instead of @BeforeClass because @Autowired doesn't work with static,
        //so check if the modification holder is already set.
        if (modificationCombinationSolver == null) {
            //add the pipeline modifications
            ModificationHolder modificationHolder = new ModificationHolder();
            Resource modificationsResource = ResourceUtils.getResourceByRelativePath("modifications_equal_mass.xml");
            modificationHolder.addModifications(modificationService.loadPipelineModifications(modificationsResource, InputType.PRIDE_ASAP));

            modificationCombinationSolver = new ModificationCombinationSolverImpl(modificationHolder);
        }
    }

    /**
     * Tests the findModificationCombinations method with 2 modifications with
     * the same mass. Each modification can only reside on one AA, so there
     * should be only one modification combination.
     *
     * @throws UnknownAAException
     * @throws AASequenceMassUnknownException
     */
    @Test
    public void testFindModificationCombinations() throws UnknownAAException, AASequenceMassUnknownException {
        //init peptide with mass delta of 288D        
        Peptide peptide = new Peptide(1, 1263.84D, new AminoAcidSequence("LPLQDVYK"));

        Set<ModificationCombination> modificationCombinations = modificationCombinationSolver.findModificationCombinations(peptide, 2, peptide.calculateMassDelta(), 2.0);

        assertEquals(1, modificationCombinations.size());
    }
}
