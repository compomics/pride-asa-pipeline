package com.compomics.pride_asa_pipeline.logic.spectrum.score;

import com.compomics.pride_asa_pipeline.logic.spectrum.score.IdentificationScorer;
import com.compomics.pride_asa_pipeline.model.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static junit.framework.Assert.*;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by IntelliJ IDEA.
 * User: niels
 * Date: 13/12/11
 * Time: 16:59
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:springXMLConfig.xml")
public class IdentificationScorerTest {
    
    @Autowired    
    private IdentificationScorer identificationScorer; 
    
    /**
     * Test the scoring of an unmodified peptide
     */
    @Test
    public void testScore_1() throws UnknownAAException {
        Peptide peptide = new Peptide(3, 579.7805745110767, new AminoAcidSequence("MSHSYKKAISDEALR"));        

        //make peakList, peak with m/z 690 has 3 matching ion ladder masses
        //the scorer should only score this peak once
        List<Peak> peakList = new ArrayList<Peak>();
        Peak peak = new Peak(488, 100);
        peakList.add(peak);
        peak = new Peak(690, 100);
        peakList.add(peak);
        peak = new Peak(302, 100);
        peakList.add(peak);
        peak = new Peak(501, 100);
        peakList.add(peak);
        peak = new Peak(734, 100);
        peakList.add(peak);
        peak = new Peak(933, 100);
        peakList.add(peak);
        peak = new Peak(222, 100);
        peakList.add(peak);
        peak = new Peak(567, 100);
        peakList.add(peak);
        peak = new Peak(50, 100);
        peakList.add(peak);
        peak = new Peak(60, 100);
        peakList.add(peak);
        
        identificationScorer.setFragmentMassError(1.0);
        
        //score the unmodified peptide
        AnnotationData annotationData = identificationScorer.score(peptide, peakList);

        //10 peaks in peak list, 8 match so matching intensity score should be 0.8
        assertNotNull(annotationData);
        assertNotNull(annotationData.getFragmentIonAnnotations());
        assertEquals(8, annotationData.getIdentificationScore().getMatchingPeaks());
        assertEquals(10, annotationData.getIdentificationScore().getTotalPeaks());
        assertEquals(800, annotationData.getIdentificationScore().getMatchingIntensity());
        assertEquals(1000, annotationData.getIdentificationScore().getTotalIntensity());
        assertEquals(0.05, annotationData.getIdentificationScore().getAverageAminoAcidScore(), 0.01);
        assertEquals(0.1, annotationData.getIdentificationScore().getAverageFragmentIonScore(), 0.01);
    }
    
    /**
     * Test the scoring of an modified peptide
     */
    @Test
    public void testScore_2() throws UnknownAAException, AASequenceMassUnknownException {        
        long peptideId = 1L;         //random ID for this hypothetical peptide
        int charge = 1;             //charge state set to 1 (to keep it easy)

        AminoAcidSequence aminoAcidSequence = new AminoAcidSequence("ACDLLYNTTTEY");

        //create modified peptide
        ModifiedPeptide modifiedPeptide = new ModifiedPeptide(charge, aminoAcidSequence.getSequenceMass(), aminoAcidSequence, peptideId);        

        //create modification
        Set<AminoAcid> modifiedAAs = new HashSet<AminoAcid>();
        modifiedAAs.add(AminoAcid.getAA('N'));
        double modMassShift = 20.0;
        Modification modification = new Modification("testModification", modMassShift, modMassShift, Modification.Location.NON_TERMINAL, modifiedAAs, "mod", "mod");

        //add modification
        modifiedPeptide.setNTModification(6, modification);
        
        //make peakList
        List<Peak> peakList = new ArrayList<Peak>();
        Peak peak = new Peak(175.05, 100);
        peakList.add(peak);
        peak = new Peak(914.40, 100);
        peakList.add(peak);
        peak = new Peak(1245.54, 100);
        peakList.add(peak);
        peak = new Peak(1355.57, 100);
        peakList.add(peak);
        peak = new Peak(513.219, 100);
        peakList.add(peak);
        peak = new Peak(457.70, 100);
        peakList.add(peak);
        peak = new Peak(623.27, 100);
        peakList.add(peak);
        peak = new Peak(569.27, 100);
        peakList.add(peak);
        peak = new Peak(206.58, 100);
        peakList.add(peak);
        peak = new Peak(91.54, 100);
        peakList.add(peak);
        
        identificationScorer.setFragmentMassError(1.0);
        
        //score the modified peptide
        AnnotationData annotationData = identificationScorer.score(modifiedPeptide, peakList);
        
        //all the peaks match
        assertEquals(10, annotationData.getFragmentIonAnnotations().size());
        assertEquals(10, annotationData.getIdentificationScore().getMatchingPeaks());
        assertEquals(10, annotationData.getIdentificationScore().getTotalPeaks());
        assertEquals(1000, annotationData.getIdentificationScore().getMatchingIntensity(), 0.01);
        assertEquals(1000, annotationData.getIdentificationScore().getTotalIntensity(), 0.01);
        assertEquals(0.1, annotationData.getIdentificationScore().getAverageFragmentIonScore(), 0.01);                                 
    }
}
