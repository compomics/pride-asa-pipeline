package com.compomics.pride_asa_pipeline.logic.spectrum.score;

import com.compomics.pride_asa_pipeline.logic.spectrum.score.IdentificationScorer;
import com.compomics.pride_asa_pipeline.model.*;
import java.util.ArrayList;
import java.util.List;
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
    
    private static final Logger LOGGER = Logger.getLogger(IdentificationScorerTest.class);
    
    @Autowired    
    private IdentificationScorer identificationScorer; 
    
    @Test
    public void testScore() {
        Peptide peptide = null;
        try {
            peptide = new Peptide(3, 579.7805745110767, new AminoAcidSequence("MSHSYKKAISDEALR"));
        } catch (UnknownAAException e) {
            LOGGER.error(e.getMessage(), e);
        }

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
}
