package com.compomics.pride_asa_pipeline.repository;

import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.repository.impl.PrideXmlParserImpl;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
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
public class PrideXmlParserTest {

    private static boolean fileLoaded = false;
    @Autowired
    private PrideXmlParserImpl prideXmlParser;
    private Resource prideXmlResource = new ClassPathResource("PRIDE_Experiment_11954.xml");

    @Before
    public void initParser() throws IOException {
        if (!fileLoaded) {
            prideXmlParser.init(prideXmlResource.getFile());
            fileLoaded = true;
        }
    }
    
    @Test
    public void testGetIdentifications() {
        List<Identification> identifications = prideXmlParser.getExperimentIdentifications();                

        //experiment 11954 contains 517 peptide identifications
        Assert.assertEquals(517, identifications.size());
    }
    
    @Test
    public void testGetNumberOfPeptides() {
        long numberOfPeptides = prideXmlParser.getNumberOfPeptides();

        //experiment 11954 contains 517 peptide identifications
        Assert.assertEquals(517, numberOfPeptides);
    }

    @Test
    public void testLoadModifications(){
        List<Modification> modifications = prideXmlParser.getModifications();
        
        Set<String> modificationNames = new HashSet<String>();
        //check if there are 4 unique modifications
        for(Modification modification : modifications){
            modificationNames.add(modification.getAccession());
        }
        
        Assert.assertEquals(4, modificationNames.size());
    }
    
    @Test
    public void testGetAnalyzerSources(){
        Map<String, String> analyzerSources = prideXmlParser.getAnalyzerSources();
        Assert.assertTrue(analyzerSources.isEmpty());
    }
    
    @Test
    public void testGetAnalyzerData(){
        List<AnalyzerData> analyzerData = prideXmlParser.getAnalyzerData();
        
        Assert.assertEquals(1, analyzerData.size());
        Assert.assertEquals(AnalyzerData.ANALYZER_FAMILY.FT, analyzerData.get(0).getAnalyzerFamily());
    }
    
    @Test
    public void testGetProteinAccessions(){
        List<String> proteinAccessions = prideXmlParser.getProteinAccessions();
        
        Assert.assertEquals(43, proteinAccessions.size());
    }
}
