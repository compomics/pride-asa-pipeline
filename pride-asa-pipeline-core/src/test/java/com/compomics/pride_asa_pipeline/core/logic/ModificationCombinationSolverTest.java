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
import com.compomics.pride_asa_pipeline.model.AminoAcidSequence;
import com.compomics.pride_asa_pipeline.model.UnknownAAException;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.core.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.core.logic.impl.ModificationCombinationSolverImpl;
import com.compomics.pride_asa_pipeline.core.model.ModificationCombination;
import com.compomics.pride_asa_pipeline.core.service.PipelineModificationService;
import com.compomics.pride_asa_pipeline.core.util.ResourceUtils;

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

public class ModificationCombinationSolverTest {

    private static final String peptideSequenceString = "AAKENNYLENNART";
    private ModificationCombinationSolver modificationCombinationSolver;
    private PipelineModificationService modificationService = new PipelineModificationServiceImpl();

    @Before
    public void setUp() throws IOException, JDOMException, URISyntaxException {
        //Use @Before instead of @BeforeClass because @Autowired doesn't work with static,
        //so check if the modification holder is already set.
        if (modificationCombinationSolver == null) {
            //add the pipeline modifications
            ModificationHolder modificationHolder = new ModificationHolder();
            File modificationsResource = ResourceUtils.getInternalResource("resources/pride_asap_modifications.xml");
            modificationHolder.addModifications(modificationService.loadPipelineModifications(modificationsResource, InputType.PRIDE_ASAP));

            modificationCombinationSolver = new ModificationCombinationSolverImpl(modificationHolder);
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
