/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.repository;

import com.compomics.pride_asa_pipeline.model.AASequenceMassUnknownException;
import com.compomics.pride_asa_pipeline.model.AminoAcid;
import com.compomics.pride_asa_pipeline.model.AminoAcidSequence;
import com.compomics.pride_asa_pipeline.model.AnnotationData;
import com.compomics.pride_asa_pipeline.model.FragmentIonAnnotation;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.IdentificationScore;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.ModifiedPeptide;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.model.PipelineExplanationType;
import com.compomics.pride_asa_pipeline.model.SpectrumAnnotatorResult;
import com.compomics.pride_asa_pipeline.model.UnknownAAException;
import com.compomics.pride_asa_pipeline.util.MathUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static junit.framework.Assert.*;
import org.jdom2.JDOMException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Niels Hulstaert
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:springXMLConfig.xml")
public class FileResultHandlerTest {

    @Autowired
    private FileResultHandler fileResultHandler;
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testWriteResult() throws IOException, UnknownAAException, JDOMException, AASequenceMassUnknownException {
        File tempResultFile = temporaryFolder.newFile("tempResultFile.txt");

        //add identifications
        List<Identification> identifications = new ArrayList<Identification>();

        //create identification with annotation data
        Peptide peptide = new Peptide(1, 1649.8D, new AminoAcidSequence("AAKENNYLENNART"));
        Identification identification_1 = new Identification(peptide, "mzAccession_1", 1L, 1L);
        identification_1.setPipelineExplanationType(PipelineExplanationType.UNMODIFIED);
        identifications.add(identification_1);

        List<FragmentIonAnnotation> fragmentIonAnnotations = new ArrayList<FragmentIonAnnotation>();
        FragmentIonAnnotation fragmentIonAnnotation_1 = new FragmentIonAnnotation(1L, FragmentIonAnnotation.IonType.Y_ION, 2, 100.5, 100, 1);
        fragmentIonAnnotations.add(fragmentIonAnnotation_1);

        FragmentIonAnnotation fragmentIonAnnotation_2 = new FragmentIonAnnotation(1L, FragmentIonAnnotation.IonType.Y_ION, 5, 200.3, 50, 2);
        fragmentIonAnnotations.add(fragmentIonAnnotation_2);

        FragmentIonAnnotation fragmentIonAnnotation_3 = new FragmentIonAnnotation(1L, FragmentIonAnnotation.IonType.B_ION, 4, 500, 300, 1);
        fragmentIonAnnotations.add(fragmentIonAnnotation_3);

        FragmentIonAnnotation fragmentIonAnnotation_4 = new FragmentIonAnnotation(1L, FragmentIonAnnotation.IonType.B_ION, 2, 400.8, 80, 1);
        fragmentIonAnnotations.add(fragmentIonAnnotation_4);

        IdentificationScore identificationScore = new IdentificationScore(4, 20, 1023L, 7502L, peptide.length());

        AnnotationData annotationData = new AnnotationData();
        annotationData.setFragmentIonAnnotations(fragmentIonAnnotations);
        annotationData.setIdentificationScore(identificationScore);

        //add annotation data to identification
        identification_1.setAnnotationData(annotationData);

        //create identification with modified peptide
        Set<AminoAcid> modifiedAAs = new HashSet<AminoAcid>();
        ModifiedPeptide modifiedPeptide = new ModifiedPeptide(1, new AminoAcidSequence("AAKENNYLENNART").getSequenceMass(), new AminoAcidSequence("AAKENNYLENNART"), 2L);
        modifiedPeptide.setNTModification(3, new Modification("testModification_1", 0.0, 0.0, Modification.Location.NON_TERMINAL, modifiedAAs, "mod_1", "mod_1"));
        modifiedPeptide.setNTModification(6, new Modification("testModification_2", 0.0, 0.0, Modification.Location.NON_TERMINAL, modifiedAAs, "mod_2", "mod_2"));
        modifiedPeptide.setNTModification(0, new Modification("testModification_3", 0.0, 0.0, Modification.Location.NON_TERMINAL, modifiedAAs, "mod_3", "mod_3"));
        modifiedPeptide.setNTermMod(new Modification("testModification_4", 0.0, 0.0, Modification.Location.N_TERMINAL, modifiedAAs, "mod_4", "mod_4"));

        Identification identification_2 = new Identification(modifiedPeptide, "mzAccession_1", 2L, 2L);
        identification_2.setPipelineExplanationType(PipelineExplanationType.MODIFIED);
        identifications.add(identification_2);

        //write identifications to file
        fileResultHandler.writeResult(tempResultFile, identifications);

        //read file
        BufferedReader bufferedReader = new BufferedReader(new FileReader(tempResultFile));
        String line = null;

        while ((line = bufferedReader.readLine()) != null) {
            String[] splitArray = line.split("\t");

            if (line.startsWith("1")) {
                assertEquals(Long.toString(identification_1.getSpectrumId()), splitArray[0]);
                assertEquals(identification_1.getMzAccession(), splitArray[1]);
                assertEquals(identification_1.getPeptide().getSequenceString(), splitArray[2]);
                assertEquals(peptide.getMzRatio(), Double.parseDouble(splitArray[3]), 0.01);
                assertEquals(peptide.getCharge(), Integer.parseInt(splitArray[4]));
                assertEquals(identification_1.getPipelineExplanationType(), PipelineExplanationType.valueOf(splitArray[5]));
                String identificationScoreString_1 = "" + MathUtils.roundDouble(identification_1.getAnnotationData().getIdentificationScore().getAverageFragmentIonScore()) + "("
                        + identification_1.getAnnotationData().getIdentificationScore().getMatchingPeaks() + ";"
                        + identification_1.getAnnotationData().getIdentificationScore().getTotalPeaks() + ";"
                        + identification_1.getAnnotationData().getIdentificationScore().getMatchingIntensity() + ";"
                        + identification_1.getAnnotationData().getIdentificationScore().getTotalIntensity() + ")";
                assertEquals(identificationScoreString_1, splitArray[6]);
                assertEquals("ions[b ion_1+(22{400.8:80.0}|44{500.0:300.0});y ion_1+(22{100.5:100.0});y ion_2+(55{200.3:50.0})]", splitArray[7]);
                assertEquals("N/A", splitArray[8]);
            } else if (line.startsWith("2")) {
                assertEquals(Long.toString(identification_2.getSpectrumId()), splitArray[0]);
                assertEquals(identification_1.getMzAccession(), splitArray[1]);
                assertEquals(identification_2.getPeptide().getSequenceString(), splitArray[2]);
                assertEquals(modifiedPeptide.getMzRatio(), Double.parseDouble(splitArray[3]), 0.01);
                assertEquals(modifiedPeptide.getCharge(), Integer.parseInt(splitArray[4]));
                assertEquals(identification_2.getPipelineExplanationType(), PipelineExplanationType.valueOf(splitArray[5]));
                assertEquals("N/A", splitArray[6]);
                assertEquals("N/A", splitArray[7]);
                assertEquals("mods[NT_testModification_4;0_testModification_3;3_testModification_1;6_testModification_2]", splitArray[8]);
            }
        }
    }

    @Test
    public void testReadResult() throws IOException {
        Resource testDataResource = new ClassPathResource("FileResultHandler_TestData.txt");

        SpectrumAnnotatorResult spectrumAnnotatorResult = fileResultHandler.readResult(testDataResource.getFile());

        assertNotNull(spectrumAnnotatorResult);
        assertEquals("FileResultHandler_TestData", spectrumAnnotatorResult.getExperimentAccession());

        //1 unmodified identification, 3 modified, 2 unexplained
        assertEquals(1, spectrumAnnotatorResult.getUnmodifiedPrecursors().size());
        assertEquals(3, spectrumAnnotatorResult.getModifiedPrecursors().size());
        assertEquals(2, spectrumAnnotatorResult.getUnexplainedIdentifications().size());

        for (Identification identification : spectrumAnnotatorResult.getIdentifications()) {
            assertNotNull(identification.getPeptide());            
        }
    }
}
