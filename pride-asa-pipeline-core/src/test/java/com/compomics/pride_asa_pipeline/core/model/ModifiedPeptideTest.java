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
package com.compomics.pride_asa_pipeline.core.model;

import com.compomics.pride_asa_pipeline.core.logic.modification.InputType;
import com.compomics.pride_asa_pipeline.core.service.impl.PipelineModificationServiceImpl;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.AminoAcid;
import com.compomics.pride_asa_pipeline.model.AASequenceMassUnknownException;
import com.compomics.pride_asa_pipeline.model.AminoAcidSequence;
import com.compomics.pride_asa_pipeline.model.UnknownAAException;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.model.ModifiedPeptide;
import com.compomics.pride_asa_pipeline.core.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.core.service.PipelineModificationService;
import com.compomics.pride_asa_pipeline.core.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;
import org.jdom2.JDOMException;
import org.junit.Test;


/**
 *
 * @author Niels Hulstaert Hulstaert
 */

public class ModifiedPeptideTest {

    private PipelineModificationService modificationService = new PipelineModificationServiceImpl();

    /**
     * Test the ion mass ladder with one NT modification
     */
    @Test
    public void testIonMassLadder_1() throws UnknownAAException, AASequenceMassUnknownException {

        long peptideId = 1L;         //random ID for this hypothetical peptide
        int charge = 1;             //charge state set to 1 (to keep it easy)

        AminoAcidSequence aminoAcidSequence = new AminoAcidSequence("ACDLLYNTTTEY");

        //create modified peptide
        ModifiedPeptide modifiedPeptide = new ModifiedPeptide(charge, aminoAcidSequence.getSequenceMass(), aminoAcidSequence, peptideId);

        //create modification
        Set<AminoAcid> modifiedAAs = new HashSet<>();
        modifiedAAs.add(AminoAcid.getAA('N'));
        double modMassShift = 12.34;
        Modification modification = new Modification("testModification", modMassShift, modMassShift, Modification.Location.NON_TERMINAL, modifiedAAs, "mod", "mod");

        //add modification
        modifiedPeptide.setNTModification(6, modification);

        double[] unmodifiedBIonLadder = modifiedPeptide.getUnmodifiedPeptide().getBIonLadderMasses(charge);
        double[] unmodifiedYIonLadder = modifiedPeptide.getUnmodifiedPeptide().getYIonLadderMasses(charge);

        double[] modifiedBIonLadder = modifiedPeptide.getBIonLadderMasses(1);
        for (int i = 0; i < modifiedBIonLadder.length; i++) {
            double unmodifiedBIonLadderMass = unmodifiedBIonLadder[i];      //unmodified mass
            double modifiedBIonLadderMass = modifiedBIonLadder[i];   //modified mass
            if (i >= 6) {
                unmodifiedBIonLadderMass += modification.getMassShift();
            }    //from the mod position on we have to take the mod mass into account
            assertEquals(unmodifiedBIonLadderMass, modifiedBIonLadderMass, 0.001);
        }

        double[] modifiedYIonLadder = modifiedPeptide.getYIonLadderMasses(1);
        for (int i = 0; i < modifiedYIonLadder.length; i++) {
            double unmodifiedYIonLadderMass = unmodifiedYIonLadder[i];      //unmodified mass
            double modifiedYIonLadderMass = modifiedYIonLadder[i];   //modified mass                      
            if (i >= 5) {
                unmodifiedYIonLadderMass += modification.getMassShift();
            }    //from the mod position on we have to take the mod mass into account
            assertEquals(unmodifiedYIonLadderMass, modifiedYIonLadderMass, 0.001);
        }

    }

    /**
     * Test the ion mass ladder with one N-terminal modification
     */
    @Test
    public void testIonMassLadder_2() throws UnknownAAException, AASequenceMassUnknownException {

        long peptideId = 1L;         //random ID for this hypothetical peptide
        int charge = 1;             //charge state set to 1 (to keep it easy)

        AminoAcidSequence aminoAcidSequence = new AminoAcidSequence("ACDLLYNTTTEY");

        //create modified peptide
        ModifiedPeptide modifiedPeptide = new ModifiedPeptide(charge, aminoAcidSequence.getSequenceMass(), aminoAcidSequence, peptideId);

        //create modification
        Set<AminoAcid> modifiedAAs = new HashSet<>();
        modifiedAAs.add(AminoAcid.getAA('A'));
        double modMassShift = 12.34;
        Modification modification = new Modification("testModification", modMassShift, modMassShift, Modification.Location.N_TERMINAL, modifiedAAs, "mod", "mod");

        //add modification
        modifiedPeptide.setNTermMod(modification);

        double[] unmodifiedBIonLadder = modifiedPeptide.getUnmodifiedPeptide().getBIonLadderMasses(charge);
        double[] unmodifiedYIonLadder = modifiedPeptide.getUnmodifiedPeptide().getYIonLadderMasses(charge);

        double[] modifiedBIonLadder = modifiedPeptide.getBIonLadderMasses(1);
        for (int i = 0; i < modifiedBIonLadder.length; i++) {
            double unmodifiedBIonLadderMass = unmodifiedBIonLadder[i];      //unmodified mass
            double modifiedBIonLadderMass = modifiedBIonLadder[i];   //modified mass
            //from the mod position on we have to take the mod mass into account
            unmodifiedBIonLadderMass += modification.getMassShift();
            assertEquals(unmodifiedBIonLadderMass, modifiedBIonLadderMass, 0.001);
        }

        double[] modifiedYIonLadder = modifiedPeptide.getYIonLadderMasses(1);
        for (int i = 0; i < modifiedYIonLadder.length; i++) {
            double unmodifiedYIonLadderMass = unmodifiedYIonLadder[i];      //unmodified mass
            double modifiedYIonLadderMass = modifiedYIonLadder[i];   //modified mass          
            assertEquals(unmodifiedYIonLadderMass, modifiedYIonLadderMass, 0.001);
        }

    }

    /**
     * Test the ion mass ladder with one C-terminal modification
     */
    @Test
    public void testIonMassLadder_3() throws UnknownAAException, AASequenceMassUnknownException {

        long peptideId = 1L;         //random ID for this hypothetical peptide
        int charge = 1;             //charge state set to 1 (to keep it easy)

        AminoAcidSequence aminoAcidSequence = new AminoAcidSequence("ACDLLYNTTTEY");

        //create modified peptide
        ModifiedPeptide modifiedPeptide = new ModifiedPeptide(charge, aminoAcidSequence.getSequenceMass(), aminoAcidSequence, peptideId);

        //create modification
        Set<AminoAcid> modifiedAAs = new HashSet<>();
        modifiedAAs.add(AminoAcid.getAA('Y'));
        double modMassShift = 12.34;
        Modification modification = new Modification("testModification", modMassShift, modMassShift, Modification.Location.C_TERMINAL, modifiedAAs, "mod", "mod");

        //add modification
        modifiedPeptide.setCTermMod(modification);

        double[] unmodifiedBIonLadder = modifiedPeptide.getUnmodifiedPeptide().getBIonLadderMasses(charge);
        double[] unmodifiedYIonLadder = modifiedPeptide.getUnmodifiedPeptide().getYIonLadderMasses(charge);

        double[] modifiedBIonLadder = modifiedPeptide.getBIonLadderMasses(1);
        for (int i = 0; i < modifiedBIonLadder.length; i++) {
            double unmodifiedBIonLadderMass = unmodifiedBIonLadder[i];      //unmodified mass
            double modifiedBIonLadderMass = modifiedBIonLadder[i];   //modified mass            
            assertEquals(unmodifiedBIonLadderMass, modifiedBIonLadderMass, 0.001);
        }

        double[] modifiedYIonLadder = modifiedPeptide.getYIonLadderMasses(1);
        for (int i = 0; i < modifiedYIonLadder.length; i++) {
            double unmodifiedYIonLadderMass = unmodifiedYIonLadder[i];      //unmodified mass
            double modifiedYIonLadderMass = modifiedYIonLadder[i];   //modified mass          
            //from the mod position on we have to take the mod mass into account
            unmodifiedYIonLadderMass += modification.getMassShift();
            assertEquals(unmodifiedYIonLadderMass, modifiedYIonLadderMass, 0.001);
        }

    }

    /**
     * Test the ion mass ladder with one NT modification and one N-terminal
     * modification on the same AA
     */
    @Test
    public void testIonMassLadder_4() throws UnknownAAException, AASequenceMassUnknownException {

        long peptideId = 1L;         //random ID for this hypothetical peptide
        int charge = 1;             //charge state set to 1 (to keep it easy)

        AminoAcidSequence aminoAcidSequence = new AminoAcidSequence("ACDLLNTTTEY");

        //create modified peptide
        ModifiedPeptide modifiedPeptide = new ModifiedPeptide(charge, aminoAcidSequence.getSequenceMass(), aminoAcidSequence, peptideId);

        //create modification_1
        Set<AminoAcid> modifiedAAs = new HashSet<>();
        modifiedAAs.add(AminoAcid.getAA('A'));
        double mod1MassShift = 12.34;
        Modification modification_1 = new Modification("testModification_1", mod1MassShift, mod1MassShift, Modification.Location.NON_TERMINAL, modifiedAAs, "mod1", "mod1");

        //create modification_2
        double mod2MassShift = 19.88;
        Modification modification_2 = new Modification("testModification_2", mod2MassShift, mod2MassShift, Modification.Location.N_TERMINAL, modifiedAAs, "mod2", "mod2");

        //add modifications
        modifiedPeptide.setNTModification(0, modification_1);
        modifiedPeptide.setNTermMod(modification_2);

        double[] unmodifiedBIonLadder = modifiedPeptide.getUnmodifiedPeptide().getBIonLadderMasses(charge);
        double[] unmodifiedYIonLadder = modifiedPeptide.getUnmodifiedPeptide().getYIonLadderMasses(charge);

        double[] modifiedBIonLadder = modifiedPeptide.getBIonLadderMasses(1);
        for (int i = 0; i < modifiedBIonLadder.length; i++) {
            double unmodifiedBIonLadderMass = unmodifiedBIonLadder[i];      //unmodified mass
            double modifiedBIonLadderMass = modifiedBIonLadder[i];   //modified mass          
            //from the mod positions on we have to take the mod masses into account
            unmodifiedBIonLadderMass += modification_1.getMassShift() + modification_2.getMassShift();
            assertEquals(unmodifiedBIonLadderMass, modifiedBIonLadderMass, 0.001);
        }

        double[] modifiedYIonLadder = modifiedPeptide.getYIonLadderMasses(1);
        for (int i = 0; i < modifiedYIonLadder.length; i++) {
            double unmodifiedYIonLadderMass = unmodifiedYIonLadder[i];      //unmodified mass
            double modifiedYIonLadderMass = modifiedYIonLadder[i];   //modified mass                      
            assertEquals(unmodifiedYIonLadderMass, modifiedYIonLadderMass, 0.001);
        }

    }

    /**
     * Test the ion mass ladder with one NT modification and one C-terminal
     * modification on the same AA
     */
    @Test
    public void testIonMassLadder_5() throws UnknownAAException, AASequenceMassUnknownException {

        long peptideId = 1L;         //random ID for this hypothetical peptide
        int charge = 1;             //charge state set to 1 (to keep it easy)

        AminoAcidSequence aminoAcidSequence = new AminoAcidSequence("ACDLLNTTTEY");

        //create modified peptide
        ModifiedPeptide modifiedPeptide = new ModifiedPeptide(charge, aminoAcidSequence.getSequenceMass(), aminoAcidSequence, peptideId);

        //create modification_1
        Set<AminoAcid> modifiedAAs = new HashSet<>();
        modifiedAAs.add(AminoAcid.getAA('Y'));
        double mod1MassShift = 12.34;
        Modification modification_1 = new Modification("testModification_1", mod1MassShift, mod1MassShift, Modification.Location.NON_TERMINAL, modifiedAAs, "mod1", "mod1");

        //create modification_2
        double mod2MassShift = 19.88;
        Modification modification_2 = new Modification("testModification_2", mod2MassShift, mod2MassShift, Modification.Location.C_TERMINAL, modifiedAAs, "mod2", "mod2");

        //add modifications
        modifiedPeptide.setNTModification(modifiedPeptide.length() - 1, modification_1);
        modifiedPeptide.setCTermMod(modification_2);

        double[] unmodifiedBIonLadder = modifiedPeptide.getUnmodifiedPeptide().getBIonLadderMasses(charge);
        double[] unmodifiedYIonLadder = modifiedPeptide.getUnmodifiedPeptide().getYIonLadderMasses(charge);

        double[] modifiedBIonLadder = modifiedPeptide.getBIonLadderMasses(1);
        for (int i = 0; i < modifiedBIonLadder.length; i++) {
            double unmodifiedBIonLadderMass = unmodifiedBIonLadder[i];      //unmodified mass
            double modifiedBIonLadderMass = modifiedBIonLadder[i];   //modified mass            
            assertEquals(unmodifiedBIonLadderMass, modifiedBIonLadderMass, 0.001);
        }

        double[] modifiedYIonLadder = modifiedPeptide.getYIonLadderMasses(1);
        for (int i = 0; i < modifiedYIonLadder.length; i++) {
            double unmodifiedYIonLadderMass = unmodifiedYIonLadder[i];      //unmodified mass
            double modifiedYIonLadderMass = modifiedYIonLadder[i];   //modified mass          
            //from the mod positions on we have to take the mod masses into account
            unmodifiedYIonLadderMass += modification_1.getMassShift() + modification_2.getMassShift();
            assertEquals(unmodifiedYIonLadderMass, modifiedYIonLadderMass, 0.001);
        }

    }

    /**
     * Test the ion mass ladder with one MS1 modification. The modification
     * should not affect the fragment ion ladders.
     */
    @Test
    public void testIonMassLadder_6() throws UnknownAAException, AASequenceMassUnknownException {

        long peptideId = 1L;         //random ID for this hypothetical peptide
        int charge = 1;             //charge state set to 1 (to keep it easy)

        AminoAcidSequence aminoAcidSequence = new AminoAcidSequence("ACDLLNTTTEY");

        //create modified peptide
        ModifiedPeptide modifiedPeptide = new ModifiedPeptide(charge, aminoAcidSequence.getSequenceMass(), aminoAcidSequence, peptideId);

        //create a MS1 modification
        Set<AminoAcid> modifiedAAs = new HashSet<>();
        modifiedAAs.add(AminoAcid.getAA('Y'));
        double modMassShift = 12.34;
        Modification modification = new Modification("testModification", modMassShift, modMassShift, Modification.Location.NON_TERMINAL, modifiedAAs, "mod", "mod");
        modification.setType(Modification.Type.MS1);

        //add modification
        modifiedPeptide.setNTModification(5, modification);

        double[] unmodifiedBIonLadder = modifiedPeptide.getUnmodifiedPeptide().getBIonLadderMasses(charge);
        double[] unmodifiedYIonLadder = modifiedPeptide.getUnmodifiedPeptide().getYIonLadderMasses(charge);

        double[] modifiedBIonLadder = modifiedPeptide.getBIonLadderMasses(1);
        for (int i = 0; i < modifiedBIonLadder.length; i++) {
            double unmodifiedBIonLadderMass = unmodifiedBIonLadder[i];      //unmodified mass
            double modifiedBIonLadderMass = modifiedBIonLadder[i];   //modified mass            
            assertEquals(unmodifiedBIonLadderMass, modifiedBIonLadderMass, 0.001);
        }

        double[] modifiedYIonLadder = modifiedPeptide.getYIonLadderMasses(1);
        for (int i = 0; i < modifiedYIonLadder.length; i++) {
            double unmodifiedYIonLadderMass = unmodifiedYIonLadder[i];      //unmodified mass
            double modifiedYIonLadderMass = modifiedYIonLadder[i];   //modified mass                      
            assertEquals(unmodifiedYIonLadderMass, modifiedYIonLadderMass, 0.001);
        }

    }

    /**
     * Tests the equals method of ModifiedPeptide.
     *
     */
    @Test
    public void testEquals() throws UnknownAAException, IOException, JDOMException, URISyntaxException {
        File modificationsResource = ResourceUtils.getInternalResource("resources/pride_asap_modifications.xml");
        Set<Modification> modifications = modificationService.loadPipelineModifications(modificationsResource, InputType.PRIDE_ASAP);

        Peptide peptide = new Peptide(1, 1256, new AminoAcidSequence("AAAKENKKNYYY"));

        ModifiedPeptide modifiedPeptide_1 = new ModifiedPeptide(peptide);
        ModifiedPeptide modifiedPeptide_2 = new ModifiedPeptide(peptide);
        ModifiedPeptide modifiedPeptide_3 = new ModifiedPeptide(peptide);
        ModifiedPeptide modifiedPeptide_4 = new ModifiedPeptide(peptide);

        //add modifications to the modified peptides
        for (Modification modification : modifications) {
            if (modification.getName().equals("Terminal Acetylation")) {
                modifiedPeptide_2.setNTermMod(modification);
                modifiedPeptide_3.setNTermMod(modification);
                modifiedPeptide_4.setNTermMod(modification);
            }
            if (modification.getName().equals("Acetylation")) {
                modifiedPeptide_2.setNTModification(4, modification);
                modifiedPeptide_2.setNTModification(7, modification);
            }
            if (modification.getName().equals("Carboxyamidomethylation")) {
                modifiedPeptide_3.setNTModification(10, modification);
                modifiedPeptide_4.setNTModification(10, modification);
            }
        }

        //Reflexivity
        assertTrue(modifiedPeptide_1.equals(modifiedPeptide_1));
        assertTrue(modifiedPeptide_2.equals(modifiedPeptide_2));
        assertTrue(modifiedPeptide_3.equals(modifiedPeptide_3));
        assertTrue(modifiedPeptide_4.equals(modifiedPeptide_4));

        //Symmetry 
        assertTrue(modifiedPeptide_3.equals(modifiedPeptide_4));
        assertTrue(modifiedPeptide_4.equals(modifiedPeptide_3));

        //Comparison to null
        assertFalse(modifiedPeptide_1.equals(null));

        //No equality
        assertFalse(modifiedPeptide_1.equals(modifiedPeptide_2));
        assertFalse(modifiedPeptide_1.equals(modifiedPeptide_3));
        assertFalse(modifiedPeptide_2.equals(modifiedPeptide_3));
    }
}
