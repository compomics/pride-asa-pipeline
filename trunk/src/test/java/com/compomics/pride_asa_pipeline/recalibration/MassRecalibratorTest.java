/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.recalibration;

import com.compomics.pride_asa_pipeline.model.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Niels Hulstaert
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:springXMLConfig.xml")
public class MassRecalibratorTest {

    private static final String[] aminoAcidSequenceStrings = new String[]{"AAAAAAWWWWWWWW", "KRRKKRKDKKKKKKKKKKKKKKK",
        "AAAKKRRR", "KENNNYY", "KNENNYYY", "KENRRWWWAAA", "LLDDDDKEAAAA",
        "AAADDDKKKRRRLLL", "LLLLNNNNEEEEEEE", "KKKKNNNDDDWWWW"};
    private static final double[] massDeltas = new double[]{-50, +10, 25, 19, -70};
    private Collection<Peptide> peptides;
    @Autowired
    private MassRecalibrator massRecalibrator;

    @Before
    public void initialize() throws UnknownAAException, AASequenceMassUnknownException {
        peptides = new ArrayList<Peptide>();

        //set considered charge states
        Set<Integer> consideredChargeStates = new HashSet<Integer>();
        consideredChargeStates.add(1);
        consideredChargeStates.add(2);
        massRecalibrator.setConsideredChargeStates(consideredChargeStates);

        Peptide peptide = null;
        AminoAcidSequence aminoAcidSequence = null;
        int counter = 0;
        for (String aaSequence : aminoAcidSequenceStrings) {
            aminoAcidSequence = new AminoAcidSequence(aaSequence);
            double massDelta = massDeltas[counter % (massDeltas.length)];
            peptide = new Peptide(1, aminoAcidSequence.getSequenceMass() + massDelta, aminoAcidSequence);
            peptides.add(peptide);
            counter++;
        }
    }

    /**
     * There are 10 peptides in the list, but only 6 have a mass delta smaller
     * than 20 (see property massrecalibrator.usable_peptides_threshold in
     * properties file). So only 6 peptides are taken into account for the
     * recalibration.
     *
     * @throws AASequenceMassUnknownException
     */
    @Test
    public void testRecalibarate() throws AASequenceMassUnknownException {
        MassRecalibrationResult massRecalibrationResult = massRecalibrator.recalibrate(peptides);

        double errorWindowChargeState1 = massRecalibrationResult.getErrorWindow(1);
        assertEquals(9.1, errorWindowChargeState1, 0.1);

        double errorWindowChargeState2 = massRecalibrationResult.getErrorWindow(2);
        assertEquals(0.2, errorWindowChargeState2, 0.01);

        assertNull(massRecalibrationResult.getErrorWindow(3));
    }
}
