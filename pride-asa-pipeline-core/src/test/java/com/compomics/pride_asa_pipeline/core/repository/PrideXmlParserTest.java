package com.compomics.pride_asa_pipeline.core.repository;

import com.compomics.pride_asa_pipeline.core.repository.impl.PrideXmlParser;
import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.util.io.compression.ZipUtils;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;

/**
 *
 * @author Niels Hulstaert
 */
public class PrideXmlParserTest {

    private static FileParser prideXmlParser;

    private static File getFileFromResources(String fileName) throws IOException {
        File testResource = new ClassPathResource(fileName).getFile();
        if (testResource.getName().endsWith(".zip")) {
            ZipUtils.unzip(testResource, testResource.getParentFile(), null);
            testResource = new File(testResource.getAbsolutePath().replace(".zip", ""));
            testResource.deleteOnExit();
        }
        return testResource;
    }

    @BeforeClass
    public static void initParser() throws IOException, ClassNotFoundException, MzXMLParsingException, JMzReaderException {
        prideXmlParser = (FileParser) new PrideXmlParser();
        File testingFile = getFileFromResources("peptideshaker_example.xml.zip");
        prideXmlParser.init(testingFile);
    }

    @Test
    public void testGetIdentifications() {
        List<Identification> identifications = prideXmlParser.getExperimentIdentifications();
        Assert.assertEquals(7277, identifications.size());
    }

    @Test
    public void testGetNumberOfPeptides() {
        long numberOfPeptides = prideXmlParser.getNumberOfPeptides();
        Assert.assertEquals(7277, numberOfPeptides);
    }

    @Test
    public void testLoadModifications() {
        List<Modification> modifications = prideXmlParser.getModifications();

        Set<String> modificationNames = new HashSet<>();
        //check if there are 6 unique modifications
        for (Modification modification : modifications) {
            modificationNames.add(modification.getAccession());
        }

        Assert.assertEquals(6, modificationNames.size());
    }

    @Test
    public void testGetAnalyzerSources() {
        Map<String, String> analyzerSources = prideXmlParser.getAnalyzerSources();
        //ToDo is this still relevant?
        Assert.assertTrue(analyzerSources != null);
    }

    @Test
    public void testGetAnalyzerData() {
        List<AnalyzerData> analyzerData = prideXmlParser.getAnalyzerData();

        Assert.assertEquals(1, analyzerData.size());
        //ToDO is this still relevant?
        //Assert.assertEquals(AnalyzerData.ANALYZER_FAMILY.FT, analyzerData.get(0).getAnalyzerFamily());
    }

    @Test
    public void testGetProteinAccessions() {
        List<String> proteinAccessions = prideXmlParser.getProteinAccessions();

        Assert.assertEquals(2357, proteinAccessions.size());
    }

}
