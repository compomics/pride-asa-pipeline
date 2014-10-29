/*
 *

 */
package com.compomics.pride_asa_pipeline.model;

import static junit.framework.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Niels Hulstaert Hulstaert
 */
public class PeptideTest {

    /**
     * Test the ion mass ladder.
     *
     * @throws com.compomics.pride_asa_pipeline.model.UnknownAAException
     * @throws
     * com.compomics.pride_asa_pipeline.model.AASequenceMassUnknownException
     */
    @Test
    public void testIonMassLadder_1() throws UnknownAAException, AASequenceMassUnknownException {

        long peptideId = 1L;         //random ID for this hypothetical peptide
        int charge = 1;             //charge state set to 1 (to keep it easy)

        AminoAcidSequence aminoAcidSequence = new AminoAcidSequence("ACDLLYNTTTEY");

        //create peptide with charge 1
        Peptide peptide = new Peptide(charge, aminoAcidSequence.getSequenceMass(), aminoAcidSequence, peptideId);

        double[] bIonLadder = peptide.getBIonLadderMasses(charge);
        double[] yIonLadder = peptide.getYIonLadderMasses(charge);

        //B ion ladder
        assertEquals(72.04498, bIonLadder[0], 0.001);
        assertEquals(175.05417, bIonLadder[1], 0.001);
        assertEquals(290.08111, bIonLadder[2], 0.001);
        assertEquals(403.16518, bIonLadder[3], 0.001);
        assertEquals(516.24924, bIonLadder[4], 0.001);
        assertEquals(679.31257, bIonLadder[5], 0.001);
        assertEquals(793.35549, bIonLadder[6], 0.001);
        assertEquals(894.40317, bIonLadder[7], 0.001);
        assertEquals(995.45085, bIonLadder[8], 0.001);
        assertEquals(1096.49853, bIonLadder[9], 0.001);
        assertEquals(1225.54112, bIonLadder[10], 0.001);

        //Y ion ladder
        assertEquals(182.08176, yIonLadder[0], 0.001);
        assertEquals(311.12436, yIonLadder[1], 0.001);
        assertEquals(412.17203, yIonLadder[2], 0.001);
        assertEquals(513.21971, yIonLadder[3], 0.001);
        assertEquals(614.26739, yIonLadder[4], 0.001);
        assertEquals(728.31032, yIonLadder[5], 0.001);
        assertEquals(891.37365, yIonLadder[6], 0.001);
        assertEquals(1004.45771, yIonLadder[7], 0.001);
        assertEquals(1117.54177, yIonLadder[8], 0.001);
        assertEquals(1232.56872, yIonLadder[9], 0.001);
        assertEquals(1335.57790, yIonLadder[10], 0.001);

        //create peptide with charge 2
        charge = 2;
        peptide = new Peptide(charge, aminoAcidSequence.getSequenceMass(), aminoAcidSequence, peptideId);

        bIonLadder = peptide.getBIonLadderMasses(charge);
        yIonLadder = peptide.getYIonLadderMasses(charge);

        //B ion ladder
        assertEquals(36.52643, bIonLadder[0], 0.001);
        assertEquals(88.03102, bIonLadder[1], 0.001);
        assertEquals(145.54449, bIonLadder[2], 0.001);
        assertEquals(202.08652, bIonLadder[3], 0.001);
        assertEquals(258.62855, bIonLadder[4], 0.001);
        assertEquals(340.16022, bIonLadder[5], 0.001);
        assertEquals(397.18168, bIonLadder[6], 0.001);
        assertEquals(447.70552, bIonLadder[7], 0.001);
        assertEquals(498.22936, bIonLadder[8], 0.001);
        assertEquals(548.75320, bIonLadder[9], 0.001);
        assertEquals(613.27450, bIonLadder[10], 0.001);

        //Y ion ladder
        assertEquals(91.54482, yIonLadder[0], 0.001);
        assertEquals(156.06611, yIonLadder[1], 0.001);
        assertEquals(206.58995, yIonLadder[2], 0.001);
        assertEquals(257.11379, yIonLadder[3], 0.001);
        assertEquals(307.63763, yIonLadder[4], 0.001);
        assertEquals(364.65909, yIonLadder[5], 0.001);
        assertEquals(446.19076, yIonLadder[6], 0.001);
        assertEquals(502.73279, yIonLadder[7], 0.001);
        assertEquals(559.27482, yIonLadder[8], 0.001);
        assertEquals(616.78829, yIonLadder[9], 0.001);
        assertEquals(668.29289, yIonLadder[10], 0.001);

    }

}
