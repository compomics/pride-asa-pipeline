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
package com.compomics.pride_asa_pipeline.core.parameters;

import com.compomics.pride_asa_pipeline.core.logic.inference.ParameterExtractor;
import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileExperimentRepository;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.io.compression.ZipUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import junit.framework.TestCase;
import org.apache.log4j.Level;
import com.compomics.pride_asa_pipeline.core.gui.PipelineProgressMonitor;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;

/**
 *
 * @author Kenneth
 */
public class ParameterExtractorTest extends TestCase {

    public ParameterExtractorTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Logger.getRootLogger().setLevel(Level.DEBUG);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private File getFileFromResources(String fileName) throws IOException {
        File testResource = new ClassPathResource(fileName).getFile();
        if (testResource.getName().endsWith(".zip")) {
            ZipUtils.unzip(testResource, testResource.getParentFile(), null);
            testResource = new File(testResource.getAbsolutePath().replace(".zip", ""));
            testResource.deleteOnExit();
        }
        return testResource;
    }

    /**
     * Test of getSearchParametersFileForProject method, of class
     * PrideAsapSearchParamExtractor.
     */
    public void testGetSearchParameters() throws Exception {
        System.out.println("testGetSearchParameters");
        // File inputFile = getFileFromResources("PRIDE_Exp_Complete_Ac_3.xml.zip");
        File inputFile = getFileFromResources("PeptideShaker_Example_Dataset.xml.zip");

        FileExperimentRepository experimentRepository = new FileExperimentRepository();
        experimentRepository.addPrideXMLFile(inputFile.getName(),inputFile);
        ParameterExtractor extractor = new ParameterExtractor(inputFile.getName());
     
        
        SearchParameters identificationParameters = extractor.getParameters();

        assertEquals(10.0, identificationParameters.getPrecursorAccuracy());
        assertEquals(0.02, identificationParameters.getFragmentIonAccuracy());

        assertEquals(identificationParameters.getPrecursorAccuracyType(), SearchParameters.MassAccuracyType.PPM);

        assertTrue(identificationParameters.getEnzyme().getName().toLowerCase().contains("tryp"));
        assertEquals(2, identificationParameters.getMinChargeSearched().value);
        assertEquals(4, identificationParameters.getMaxChargeSearched().value);

        ArrayList<String> modifications = identificationParameters.getPtmSettings().getAllModifications();

        assertTrue(modifications.contains("Oxidation of M"));
        assertTrue(modifications.contains("Carbamidomethylation of C"));
      /*  System.out.println();
        System.out.println("# ------------------------------------------------------------------");
        System.out.println("FINAL VERDICT");
        System.out.println("# ------------------------------------------------------------------");
        System.out.println(identificationParameters.toString());*/

    }

}
