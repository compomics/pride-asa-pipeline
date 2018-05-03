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

import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.AASequenceMassUnknownException;
import com.compomics.pride_asa_pipeline.model.UnknownAAException;
import com.compomics.pride_asa_pipeline.core.repository.FileResultHandler;
import com.compomics.pride_asa_pipeline.core.service.DbModificationService;
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

import static org.junit.Assert.*;

/**
 *
 * @author Niels Hulstaert Hulstaert
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:springXMLConfig.xml")
public class UsedMoficationsTest {

    @Autowired
    private DbModificationService modificationService;
    
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

        assertEquals(19, spectrumAnnotatorResult.getIdentifications().size());
        assertEquals(2, spectrumAnnotatorResult.getUnmodifiedPrecursors().size());
        assertEquals(12, spectrumAnnotatorResult.getModifiedPrecursors().size());        
        assertEquals(5, spectrumAnnotatorResult.getUnexplainedIdentifications().size());        

        Map<Modification, Integer> usedModifications = modificationService.getUsedModifications(spectrumAnnotatorResult);
        assertEquals(3, usedModifications.size());

        Map<Modification, Double> modificationRates = modificationService.estimateModificationRate(usedModifications, spectrumAnnotatorResult, 0.8);
        for (Modification modification : modificationRates.keySet()) {

            //In the testing data, the oxidation occurs on 'K', and 9/10 K's are oxidized. Fixed mod!
            if (modification.getName().equals("Oxidation")) {
                assertEquals(0.9, modificationRates.get(modification), 0.01);
            }

            //In the testing data, the 'Carboxyamidomethylation' occurs on 'Y', and 1/6 Y's are modified. Variable mod!
            if (modification.getName().contains("methyl")) {
                assertEquals(0.2, modificationRates.get(modification), 0.01);
            }
            
            //In the testing data, the 'Terminal Oxidation' occurs on 'A', and 1/2 A's are modified. Variable mod!
            if (modification.getName().equals("Terminal Oxidation")) {
                assertEquals(0.5, modificationRates.get(modification), 0.01);
            }
        }
    }
}