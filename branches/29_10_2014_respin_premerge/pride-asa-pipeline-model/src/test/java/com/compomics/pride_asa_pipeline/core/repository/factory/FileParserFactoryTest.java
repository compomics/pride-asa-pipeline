/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.repository.factory;

import com.compomics.pride_asa_pipeline.core.repository.FileParser;
import com.compomics.pride_asa_pipeline.core.repository.impl.MzIdentMlParser;
import com.compomics.pride_asa_pipeline.core.repository.impl.PrideXmlParser;
import java.io.File;
import static junit.framework.Assert.assertTrue;
import junit.framework.TestCase;


/**
 *
 * @author Kenneth Verheggen
 */
public class FileParserFactoryTest extends TestCase {

    public FileParserFactoryTest(String testName) {
        super(testName);
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
     * Test of getFileParser method, of class FileParserFactory.
     *
     * @throws com.compomics.respin_utilities.exception.ExtractionException
     */
    public void testGetFileParser() {
        System.out.println("getFileParser");
        try {
            FileParser result = FileParserFactory.getFileParser(new File("Test_pride.xml"));
            assertTrue(result instanceof PrideXmlParser);
            result = FileParserFactory.getFileParser(new File("Test_mzXMl.mzid"));
            assertTrue(result instanceof MzIdentMlParser);
            FileParserFactory.getFileParser(new File("Test_fake.fake"));
        } catch (Exception e) {
            System.out.println(e);
            assertTrue(true);
        }

    }

}
