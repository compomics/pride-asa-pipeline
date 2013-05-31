/*
 *

 */
package com.compomics.pride_asa_pipeline.model;

import com.compomics.pride_asa_pipeline.repository.FileResultHandler;
import com.compomics.pride_asa_pipeline.service.impl.DbModificationServiceImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Map;

import static junit.framework.Assert.*;

/**
 *
 * @author Niels Hulstaert Hulstaert
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:springXMLConfig.xml")
public class UsedMoficationsTest {

    @Autowired
    private DbModificationServiceImpl modificationService = new DbModificationServiceImpl();

    @Autowired
    private FileResultHandler fileResultHandler;
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * Test the ion mass ladder with one NT modification
     */
    @Test
    public void testIonMassLadder_1() throws UnknownAAException, AASequenceMassUnknownException, IOException {

        Resource testDataResource = new ClassPathResource("FileResultHandler_TestData_2.txt");

        SpectrumAnnotatorResult spectrumAnnotatorResult = fileResultHandler.readResult(testDataResource.getFile());

        assertNotNull(spectrumAnnotatorResult);
        assertEquals("FileResultHandler_TestData_2", spectrumAnnotatorResult.getExperimentAccession());

        //2 unexplained
        assertEquals(15, spectrumAnnotatorResult.getIdentifications().size());
        assertEquals(5, spectrumAnnotatorResult.getUnexplainedIdentifications().size());

        Map<Modification,Integer> lUsedModifications = modificationService.getUsedModifications(spectrumAnnotatorResult);
        assertEquals(2, lUsedModifications.size());

        Map<Modification, Double> lModificationRates = modificationService.estimateModificationRate(lUsedModifications, spectrumAnnotatorResult, 0.8);
        for (Modification lModification : lModificationRates.keySet()) {

            // In the testing data, the oxidation occurs on 'T', and 9/10 T's are oxidized. Fixed mod!
            if (lModification.getName().contains("Oxidation")) {
                assertEquals(0.9, lModificationRates.get(lModification));
            }

            // In the testing data, the 'Carboxyamidomethylation' occurs on 'Y', and 1/6 Y's are modified. Variable mod!
            if(lModification.getName().contains("methyl")){
                assertEquals(0.2, lModificationRates.get(lModification));
            }
        }


    }
}