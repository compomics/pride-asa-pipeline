/*
 *

 */
package com.compomics.pride_asa_pipeline.logic;

import com.compomics.pride_asa_pipeline.model.*;
import com.compomics.pride_asa_pipeline.service.DbModificationService;
import com.compomics.pride_asa_pipeline.util.ResourceUtils;
import java.io.IOException;
import java.util.Set;
import static junit.framework.Assert.assertEquals;
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

    @Autowired
    private ModificationCombinationSolver modificationCombinationSolver;
    @Autowired
    private DbModificationService dbModificationService;

    @Before
    public void initialize() throws IOException, JDOMException {
        //Use @Before instead of @BeforeClass because @Autowired doesn't work with static,
        //so check if the modification holder is already set.
        if (modificationCombinationSolver.getModificationHolder() == null) {
            //add the pipeline modifications
            ModificationHolder modificationHolder = new ModificationHolder();
            Resource modificationsResource = ResourceUtils.getResourceByRelativePath("modifications_equal_mass.xml");
            modificationHolder.addModifications(dbModificationService.loadPipelineModifications(modificationsResource));

            //set the modification combination holder
            modificationCombinationSolver.setModificationHolder(modificationHolder);
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
