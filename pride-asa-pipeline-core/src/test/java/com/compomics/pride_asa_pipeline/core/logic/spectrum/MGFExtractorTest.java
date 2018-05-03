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
package com.compomics.pride_asa_pipeline.core.logic.spectrum;

import com.compomics.pride_asa_pipeline.core.data.extractor.MGFExtractor;
import com.compomics.pride_asa_pipeline.core.model.MGFExtractionException;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import com.compomics.util.io.compression.ZipUtils;
import java.io.File;
import java.io.IOException;
import junit.framework.TestCase;
import org.springframework.core.io.ClassPathResource;

/**
 *
 * @author Kenneth
 */
public class MGFExtractorTest extends TestCase {

    private static MGFExtractor instance3 = null;

    private File getFileFromResources(String fileName) throws IOException {
        File testResource = new ClassPathResource(fileName).getFile();
        if (testResource.getName().endsWith(".zip")) {
            ZipUtils.unzip(testResource, testResource.getParentFile(), null);
            testResource = new File(testResource.getAbsolutePath().replace(".zip", ""));
            testResource.deleteOnExit();
        }
        return testResource;
    }

    public MGFExtractorTest(String testName) throws MGFExtractionException, IOException {
        super(testName);
        if (MGFExtractorTest.instance3 == null) {
            File testingFile3 = getFileFromResources("PRIDE_Exp_Complete_Ac_3.xml.zip");
            MGFExtractorTest.instance3 = new MGFExtractor(testingFile3);
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of extractMGF method, of class DefaultMGFExtractor2.
     */
    public void testExtractMGF() throws MGFExtractionException, IOException  {
        System.out.println("extractMGF");

        File outputFile = new File("pride_project_3.mgf");
        outputFile.deleteOnExit();
        File result = MGFExtractorTest.instance3.extractMGF(outputFile);
        SpectrumFactory factory = SpectrumFactory.getInstance();
        factory.clearFactory();
        factory.addSpectra(outputFile, null);
        //TODO verify that charge is really not required, as suggested by Harald ...
        //  Integer maxCharge = factory.getMaxCharge(result.getName());
        Double maxIntensity = factory.getMaxIntensity(result.getName());
        Double maxMz = factory.getMaxMz(result.getName());
        assertEquals(58177.0547, maxIntensity);
        assertEquals(1198.1465, maxMz);
        assertEquals(1958, factory.getSpectrumTitles(result.getName()).size());
    }

}
