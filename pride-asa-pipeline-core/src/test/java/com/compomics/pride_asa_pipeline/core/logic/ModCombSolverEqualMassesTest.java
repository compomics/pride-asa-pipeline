/* 
 * Copyright 2018 compomics.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.compomics.pride_asa_pipeline.core.logic;

import com.compomics.pride_asa_pipeline.core.logic.modification.InputType;
import com.compomics.pride_asa_pipeline.core.model.ModificationHolder;
import com.compomics.pride_asa_pipeline.core.service.impl.PipelineModificationServiceImpl;
import com.compomics.pride_asa_pipeline.model.AASequenceMassUnknownException;
import com.compomics.pride_asa_pipeline.core.model.ModificationCombination;
import com.compomics.pride_asa_pipeline.model.AminoAcidSequence;
import com.compomics.pride_asa_pipeline.model.UnknownAAException;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.core.logic.impl.ModificationCombinationSolverImpl;
import com.compomics.pride_asa_pipeline.core.service.PipelineModificationService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import org.jdom2.JDOMException;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Niels Hulstaert Hulstaert
 */

public class ModCombSolverEqualMassesTest {

    private ModificationCombinationSolver modificationCombinationSolver;

    private PipelineModificationService modificationService = new PipelineModificationServiceImpl();

    @Before
    public void initialize() throws IOException, JDOMException, URISyntaxException {
        //Use @Before instead of @BeforeClass because @Autowired doesn't work with static,
        //so check if the modification holder is already set.
        if (modificationCombinationSolver == null) {
            //add the pipeline modifications
            ModificationHolder modificationHolder = new ModificationHolder();
            File modificationsResource = new File(ModCombSolverEqualMassesTest.class.getClassLoader().getResource("modifications_equal_mass.xml").toURI());
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
