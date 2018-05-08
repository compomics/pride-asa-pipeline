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

import com.compomics.pride_asa_pipeline.core.logic.impl.PeptideVariationsGeneratorImpl;
import com.compomics.pride_asa_pipeline.core.logic.modification.InputType;
import com.compomics.pride_asa_pipeline.core.service.impl.PipelineModificationServiceImpl;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.ModificationFacade;
import com.compomics.pride_asa_pipeline.core.model.ModificationCombination;
import com.compomics.pride_asa_pipeline.model.AminoAcidSequence;
import com.compomics.pride_asa_pipeline.model.UnknownAAException;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.model.ModifiedPeptide;
import com.compomics.pride_asa_pipeline.core.service.PipelineModificationService;
import com.compomics.pride_asa_pipeline.core.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;
import org.jdom2.JDOMException;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Niels Hulstaert Hulstaert
 */
public class PeptideVariationsGeneratorTest {

    private PeptideVariationsGenerator peptideVariationsGenerator = new PeptideVariationsGeneratorImpl();

    private PipelineModificationService modificationService = new PipelineModificationServiceImpl();

    private Set<Modification> modifications;

    @Before
    public void loadModifications() throws IOException, JDOMException, URISyntaxException {
        if (modifications == null) {
            File modificationsResource = ResourceUtils.getInternalResource("resources/pride_asap_modifications.xml");
            modifications = modificationService.loadPipelineModifications(modificationsResource, InputType.PRIDE_ASAP);
        }
    }

    /**
     * Test a variation for a modification combination with one N-terminal
     * modification
     *
     * @throws UnknownAAException
     */
    @Test
    public void testGenerateVariations_1() throws UnknownAAException {
        Peptide peptide = new Peptide(1, 1356, new AminoAcidSequence("AKEYNNY"));

        Set<ModificationCombination> modificationCombinations = new HashSet<ModificationCombination>();
        ModificationCombination modificationCombination = new ModificationCombination();
        //add N-terminal modification to modification combination        
        for (Modification modification : modifications) {
            if (modification.getName().equals("Terminal Acetylation")) {
                modificationCombination.addModification(modification);
            }
        }
        modificationCombinations.add(modificationCombination);

        Set<ModifiedPeptide> modifiedPeptides = peptideVariationsGenerator.generateVariations(peptide, modificationCombinations);
        //the set should contain only one modified peptide, with the N-terminal modification
        assertEquals(1, modifiedPeptides.size());
        ModifiedPeptide modifiedPeptide = modifiedPeptides.iterator().next();
        assertNotNull(modifiedPeptide.getNTermMod());
        for (ModificationFacade modificationIface : modifiedPeptide.getNTModifications()) {
            assertNull(modificationIface);
        }
        assertNull(modifiedPeptide.getCTermMod());
    }

    /**
     * Test a variation for a modification combination with non-terminal
     * modifications; the number of modifications is equal to the number of AA
     * that can be affected by the modifications.
     *
     * @throws UnknownAAException
     */
    @Test
    public void testGenerateVariations_2() throws UnknownAAException {
        Peptide peptide = new Peptide(1, 1356, new AminoAcidSequence("AKEYNNYKYEKNAK"));

        Set<ModificationCombination> modificationCombinations = new HashSet<ModificationCombination>();
        ModificationCombination modificationCombination = new ModificationCombination();
        //add modifications to modification combination        
        for (Modification modification : modifications) {
            if (modification.getName().equals("Acetylation")) {
                modificationCombination.addModification(modification);
                modificationCombination.addModification(modification);
                modificationCombination.addModification(modification);
                modificationCombination.addModification(modification);
            }
        }
        modificationCombinations.add(modificationCombination);

        Set<ModifiedPeptide> modifiedPeptides = peptideVariationsGenerator.generateVariations(peptide, modificationCombinations);
        //the set should contain only one modified peptide, with 4 non-terminal modifications on the right AA
        assertEquals(1, modifiedPeptides.size());
        ModifiedPeptide modifiedPeptide = modifiedPeptides.iterator().next();
        assertNull(modifiedPeptide.getNTermMod());
        for (int i = 0; i < modifiedPeptide.getNumberNTModifications(); i++) {
            if (i == 1 || i == 7 || i == 10 || i == 13) {
                assertNotNull(modifiedPeptide.getNTModification(i));
            } else {
                assertNull(modifiedPeptide.getNTModification(i));
            }
        }
        assertNull(modifiedPeptide.getCTermMod());
    }

    /**
     * Test a variation for a modification combination with non-terminal
     * modifications; the number of modifications is less than the number of AA
     * that can be affected by the modifications.
     *
     * @throws UnknownAAException
     */
    @Test
    public void testGenerateVariations_3() throws UnknownAAException {
        Peptide peptide = new Peptide(1, 1356, new AminoAcidSequence("AKEYNNYKYEKNAK"));

        Set<ModificationCombination> modificationCombinations = new HashSet<ModificationCombination>();
        ModificationCombination modificationCombination = new ModificationCombination();
        //add modifications to modification combination        
        for (Modification modification : modifications) {
            if (modification.getName().equals("Acetylation")) {
                modificationCombination.addModification(modification);
                modificationCombination.addModification(modification);
            }
        }
        modificationCombinations.add(modificationCombination);

        Set<ModifiedPeptide> modifiedPeptides = peptideVariationsGenerator.generateVariations(peptide, modificationCombinations);
        //the set should contain 6 modified peptides, with 2 non-terminal modifications on the right AA
        assertEquals(6, modifiedPeptides.size());
        for (ModifiedPeptide modifiedPeptide : modifiedPeptides) {
            assertNull(modifiedPeptide.getNTermMod());
            int counter = 0;
            for (int i = 0; i < modifiedPeptide.length(); i++) {
                if (i == 1 || i == 7 || i == 10 || i == 13) {
                    if (modifiedPeptide.getNTModification(i) != null) {
                        counter++;
                    }
                } else {
                    assertNull(modifiedPeptide.getNTModification(i));
                }
            }
            //there should be 2 non-terminal modifications per modified peptide
            assertEquals(2, counter);
            assertNull(modifiedPeptide.getCTermMod());
        }
    }

    /**
     * Test a variation for a modification combination with 2 non-terminal
     * modifications that don't have a common affected AA.
     *
     * @throws UnknownAAException
     */
    @Test
    public void testGenerateVariations_4() throws UnknownAAException {
        Peptide peptide = new Peptide(1, 1356, new AminoAcidSequence("AKEYNNYKYEKNAK"));

        Set<ModificationCombination> modificationCombinations = new HashSet<ModificationCombination>();
        ModificationCombination modificationCombination = new ModificationCombination();
        //add modifications to modification combination        
        for (Modification modification : modifications) {
            if (modification.getName().equals("Acetylation")) {
                modificationCombination.addModification(modification);
            }
            if (modification.getName().equals("Carboxyamidomethylation")) {
                modificationCombination.addModification(modification);
            }
        }
        modificationCombinations.add(modificationCombination);

        Set<ModifiedPeptide> modifiedPeptides = peptideVariationsGenerator.generateVariations(peptide, modificationCombinations);
        //the set should contain 12 modified peptides;
        //the Acetylation modification has 4 possible sites,
        //the Carboxyamidomethylation 3 => 4*3 = 12
        assertEquals(12, modifiedPeptides.size());
        for (ModifiedPeptide modifiedPeptide : modifiedPeptides) {
            assertNull(modifiedPeptide.getNTermMod());
            int counter = 0;
            for (int i = 0; i < modifiedPeptide.length(); i++) {
                if (i == 1 || i == 7 || i == 10 || i == 13 || i == 3 || i == 6 || i == 8) {
                    if (modifiedPeptide.getNTModification(i) != null) {
                        counter++;
                    }
                } else {
                    assertNull(modifiedPeptide.getNTModification(i));
                }
            }
            //there should be 2 non-terminal modifications per modified peptide
            assertEquals(2, counter);
            assertNull(modifiedPeptide.getCTermMod());
        }
    }

    /**
     * Test a variation for a modification combination with 2 non-terminal
     * modifications that do have a common affected AA.
     *
     * @throws UnknownAAException
     */
    @Test
    public void testGenerateVariations_5() throws UnknownAAException {
        Peptide peptide = new Peptide(1, 1356, new AminoAcidSequence("AKEYKNAK"));

        Set<ModificationCombination> modificationCombinations = new HashSet<ModificationCombination>();
        ModificationCombination modificationCombination = new ModificationCombination();
        //add modifications to modification combination        
        for (Modification modification : modifications) {
            if (modification.getName().equals("Acetylation")) {
                modificationCombination.addModification(modification);
            }
            if (modification.getName().equals("Oxidation")) {
                modificationCombination.addModification(modification);
            }
        }
        modificationCombinations.add(modificationCombination);

        Set<ModifiedPeptide> modifiedPeptides = peptideVariationsGenerator.generateVariations(peptide, modificationCombinations);
        //the set should contain 6 modified peptides;
        //the Acetylation modification has 3 possible sites,
        //the Oxidation the same 3 => variation 2 out of 3 = 6 modified peptides 
        assertEquals(6, modifiedPeptides.size());
        for (ModifiedPeptide modifiedPeptide : modifiedPeptides) {
            assertNull(modifiedPeptide.getNTermMod());
            int counter = 0;
            for (int i = 0; i < modifiedPeptide.length(); i++) {
                if (i == 1 || i == 4 || i == 7) {
                    if (modifiedPeptide.getNTModification(i) != null) {
                        counter++;
                    }
                } else {
                    assertNull(modifiedPeptide.getNTModification(i));
                }
            }
            //there should be 2 non-terminal modifications per modified peptide
            assertEquals(2, counter);
            assertNull(modifiedPeptide.getCTermMod());
        }
    }

    /**
     * Test a variation for a modification combination with one N-terminal and
     * one C-terminal modification.
     *
     * @throws UnknownAAException
     */
    @Test
    public void testGenerateVariations_6() throws UnknownAAException {
        Peptide peptide = new Peptide(1, 1356, new AminoAcidSequence("AKEYKNAK"));

        Set<ModificationCombination> modificationCombinations = new HashSet<ModificationCombination>();
        ModificationCombination modificationCombination = new ModificationCombination();
        //add modifications to modification combination        
        for (Modification modification : modifications) {
            if (modification.getName().equals("Terminal Acetylation")) {
                modificationCombination.addModification(modification);
            }
            if (modification.getName().equals("Terminal Oxidation")) {
                modificationCombination.addModification(modification);
            }
        }
        modificationCombinations.add(modificationCombination);

        Set<ModifiedPeptide> modifiedPeptides = peptideVariationsGenerator.generateVariations(peptide, modificationCombinations);
        //the set should contain 1 modified peptide with one N-terminal and one C-terminal modification                 
        assertEquals(1, modifiedPeptides.size());
        for (ModifiedPeptide modifiedPeptide : modifiedPeptides) {
            assertNotNull(modifiedPeptide.getNTermMod());
            for (int i = 0; i < modifiedPeptide.length(); i++) {
                assertNull(modifiedPeptide.getNTModification(i));
            }
            assertNotNull(modifiedPeptide.getCTermMod());
        }
    }
}
