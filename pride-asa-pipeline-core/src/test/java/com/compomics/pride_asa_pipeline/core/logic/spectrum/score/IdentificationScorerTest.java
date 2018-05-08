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
package com.compomics.pride_asa_pipeline.core.logic.spectrum.score;

import com.compomics.pride_asa_pipeline.core.logic.spectrum.score.impl.IdentificationScorerImpl;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.AminoAcid;
import com.compomics.pride_asa_pipeline.model.AnnotationData;
import com.compomics.pride_asa_pipeline.model.AASequenceMassUnknownException;
import com.compomics.pride_asa_pipeline.model.AminoAcidSequence;
import com.compomics.pride_asa_pipeline.model.UnknownAAException;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.model.Peak;
import com.compomics.pride_asa_pipeline.model.ModifiedPeptide;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.junit.Assert.*;
import org.junit.Test;


public class IdentificationScorerTest {

    private IdentificationScorer identificationScorer = new IdentificationScorerImpl();

    /**
     * Test the scoring of an unmodified peptide
     */
    @Test
    public void testScore_1() throws UnknownAAException {
        Peptide peptide = new Peptide(3, 579.7805745110767, new AminoAcidSequence("MSHSYKKAISDEALR"));

        //make peakList, peak with m/z 690 has 3 matching ion ladder masses
        //the scorer should only score this peak once
        List<Peak> peakList = new ArrayList<>();
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

        //score the unmodified peptide
        AnnotationData annotationData = identificationScorer.score(peptide, peakList, 1.0);

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
        Set<AminoAcid> modifiedAAs = new HashSet<>();
        modifiedAAs.add(AminoAcid.getAA('N'));
        double modMassShift = 20.0;
        Modification modification = new Modification("testModification", modMassShift, modMassShift, Modification.Location.NON_TERMINAL, modifiedAAs, "mod", "mod");

        //add modification
        modifiedPeptide.setNTModification(6, modification);

        //make peakList
        List<Peak> peakList = new ArrayList<>();
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

        //score the modified peptide
        AnnotationData annotationData = identificationScorer.score(modifiedPeptide, peakList, 1.0);

        //all the peaks match
        assertEquals(10, annotationData.getFragmentIonAnnotations().size());
        assertEquals(10, annotationData.getIdentificationScore().getMatchingPeaks());
        assertEquals(10, annotationData.getIdentificationScore().getTotalPeaks());
        assertEquals(1000, annotationData.getIdentificationScore().getMatchingIntensity(), 0.01);
        assertEquals(1000, annotationData.getIdentificationScore().getTotalIntensity(), 0.01);
        assertEquals(0.1, annotationData.getIdentificationScore().getAverageFragmentIonScore(), 0.01);
    }
}
