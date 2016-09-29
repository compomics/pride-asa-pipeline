/*
 *

 */
package com.compomics.pride_asa_pipeline.core.logic.recalibration;

import com.compomics.pride_asa_pipeline.model.AASequenceMassUnknownException;
import com.compomics.pride_asa_pipeline.model.AminoAcidSequence;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.model.UnknownAAException;
import com.compomics.pride_asa_pipeline.model.Constants;
import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
    private static final double[] massDeltas = new double[]{-50, 10, 25, 19, -70, 2, 6, -3, 8, 11};
    
    private Collection<Peptide> peptides;
    @Autowired
    private MassRecalibrator massRecalibrator;
    private AnalyzerData analyzerData;

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
            peptide = new Peptide(1, aminoAcidSequence.getSequenceMass() + Constants.MASS_H2O + Constants.MASS_H + massDelta, aminoAcidSequence);
            peptides.add(peptide);
            counter++;
        }
        
        analyzerData = new AnalyzerData(0.4, 0.4, AnalyzerData.ANALYZER_FAMILY.UNKNOWN);
    }

    /**
     * There are 10 peptides in the list, but only 7 have a mass delta smaller
     * than 20 (see property mass_delta_threshold in properties file). So only 7
     * peptides are taken into account for the recalibration.
     *
     * @throws AASequenceMassUnknownException
     */
    @Test
    public void testRecalibarate() throws AASequenceMassUnknownException {
        MassRecalibrationResult massRecalibrationResult = massRecalibrator.recalibrate(analyzerData, peptides);
        
        double massErrorChargeState1 = massRecalibrationResult.getError(1);
        assertEquals(8.0, massErrorChargeState1, 0.01);
        double errorWindowChargeState1 = massRecalibrationResult.getErrorWindow(1);
        assertEquals(11.1, errorWindowChargeState1, 0.01);

        //check default values
        double massErrorChargeState2 = massRecalibrationResult.getError(2);
        assertEquals(0.0, massErrorChargeState2, 0.01);
        double errorWindowChargeState2 = massRecalibrationResult.getErrorWindow(2);
        assertEquals(0.2, errorWindowChargeState2, 0.01);

        assertNull(massRecalibrationResult.getErrorWindow(3));
    }
}
