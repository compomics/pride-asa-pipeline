/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.model;

import com.compomics.pride_asa_pipeline.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.service.ModificationService;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;
import static junit.framework.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Niels Hulstaert
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:springXMLConfig.xml")
public class ModifiedPeptideTest {

    @Autowired
    private ModificationService modificationService;

    /**
     * Test the ion mass ladder with one NT modification
     */
    @Test
    public void testIonMassLadder_1() throws UnknownAAException, AASequenceMassUnknownException {

        long peptideId = 1L;         //random ID for this hypothetical peptide
        int charge = 1;             //charge state set to 1 (to keep it easy)

        AminoAcidSequence aminoAcidSequence = new AminoAcidSequence("ACDLLYNTTTEY");

        //first get the ion ladders before adding the modification
        ModifiedPeptide modifiedPeptide = new ModifiedPeptide(charge, aminoAcidSequence.getSequenceMass(), aminoAcidSequence, peptideId);

        double[] unmodifiedBIonLadder = modifiedPeptide.getBIonLadderMasses(charge);
        double[] unmodifiedYIonLadder = modifiedPeptide.getYIonLadderMasses(charge);

        //create modification
        Set<AminoAcid> modifiedAAs = new HashSet<AminoAcid>();
        modifiedAAs.add(AminoAcid.getAA('N'));
        double modMassShift = 12.34;
        Modification modification = new Modification("testModification", modMassShift, modMassShift, Modification.Location.NON_TERMINAL, modifiedAAs, "mod", "mod");

        //add modification
        modifiedPeptide.setNTModification(6, modification);

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

        //first get the ion ladders before adding the modification
        ModifiedPeptide modifiedPeptide = new ModifiedPeptide(charge, aminoAcidSequence.getSequenceMass(), aminoAcidSequence, peptideId);

        double[] unmodifiedBIonLadder = modifiedPeptide.getBIonLadderMasses(charge);
        double[] unmodifiedYIonLadder = modifiedPeptide.getYIonLadderMasses(charge);

        //create modification
        Set<AminoAcid> modifiedAAs = new HashSet<AminoAcid>();
        modifiedAAs.add(AminoAcid.getAA('A'));
        double modMassShift = 12.34;
        Modification modification = new Modification("testModification", modMassShift, modMassShift, Modification.Location.N_TERMINAL, modifiedAAs, "mod", "mod");

        //add modification
        modifiedPeptide.setNTermMod(modification);

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

        //first get the ion ladders before adding the modification
        ModifiedPeptide modifiedPeptide = new ModifiedPeptide(charge, aminoAcidSequence.getSequenceMass(), aminoAcidSequence, peptideId);

        double[] unmodifiedBIonLadder = modifiedPeptide.getBIonLadderMasses(charge);
        double[] unmodifiedYIonLadder = modifiedPeptide.getYIonLadderMasses(charge);

        //create modification
        Set<AminoAcid> modifiedAAs = new HashSet<AminoAcid>();
        modifiedAAs.add(AminoAcid.getAA('Y'));
        double modMassShift = 12.34;
        Modification modification = new Modification("testModification", modMassShift, modMassShift, Modification.Location.C_TERMINAL, modifiedAAs, "mod", "mod");

        //add modification
        modifiedPeptide.setCTermMod(modification);

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

        //first get the ion ladders before adding the modification
        ModifiedPeptide modifiedPeptide = new ModifiedPeptide(charge, aminoAcidSequence.getSequenceMass(), aminoAcidSequence, peptideId);

        double[] unmodifiedBIonLadder = modifiedPeptide.getBIonLadderMasses(charge);
        double[] unmodifiedYIonLadder = modifiedPeptide.getYIonLadderMasses(charge);

        //create modification_1
        Set<AminoAcid> modifiedAAs = new HashSet<AminoAcid>();
        modifiedAAs.add(AminoAcid.getAA('A'));
        double mod1MassShift = 12.34;
        Modification modification_1 = new Modification("testModification_1", mod1MassShift, mod1MassShift, Modification.Location.NON_TERMINAL, modifiedAAs, "mod1", "mod1");

        //create modification_2
        double mod2MassShift = 19.88;
        Modification modification_2 = new Modification("testModification_2", mod2MassShift, mod2MassShift, Modification.Location.N_TERMINAL, modifiedAAs, "mod2", "mod2");

        //add modifications
        modifiedPeptide.setNTModification(0, modification_1);
        modifiedPeptide.setNTermMod(modification_2);

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

        //first get the ion ladders before adding the modification
        ModifiedPeptide modifiedPeptide = new ModifiedPeptide(charge, aminoAcidSequence.getSequenceMass(), aminoAcidSequence, peptideId);

        double[] unmodifiedBIonLadder = modifiedPeptide.getBIonLadderMasses(charge);
        double[] unmodifiedYIonLadder = modifiedPeptide.getYIonLadderMasses(charge);

        //create modification_1
        Set<AminoAcid> modifiedAAs = new HashSet<AminoAcid>();
        modifiedAAs.add(AminoAcid.getAA('Y'));
        double mod1MassShift = 12.34;
        Modification modification_1 = new Modification("testModification_1", mod1MassShift, mod1MassShift, Modification.Location.NON_TERMINAL, modifiedAAs, "mod1", "mod1");

        //create modification_2
        double mod2MassShift = 19.88;
        Modification modification_2 = new Modification("testModification_2", mod2MassShift, mod2MassShift, Modification.Location.C_TERMINAL, modifiedAAs, "mod2", "mod2");

        //add modifications
        modifiedPeptide.setNTModification(modifiedPeptide.length() - 1, modification_1);
        modifiedPeptide.setCTermMod(modification_2);

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
     * Tests the equals method of ModifiedPeptide.
     *
     */
    @Test
    public void testEquals() throws UnknownAAException {        
        Set<Modification> modifications = modificationService.loadPipelineModifications(PropertiesConfigurationHolder.getInstance().getString("modification.pipeline_modifications_file_name"));

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
