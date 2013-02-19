
package com.compomics.pride_asa_pipeline.repository;

import com.compomics.pride_asa_pipeline.repository.impl.PrideXmlParserImpl;
import java.io.IOException;
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
    
    @Autowired
    private PrideXmlParserImpl prideXmlParser;
    private Resource prideXmlResource = new ClassPathResource("PRIDE_Experiment_11954.xml");
    
    @Before
    public void initParser() throws IOException{
        prideXmlParser.init(prideXmlResource.getFile());
    }
    
    @Test
    public void testLoadIdentifications(){
        //prideXmlParser.loadExperimentIdentifications();
        prideXmlParser.getSpectraMetadata();
        System.out.println("test");
    }

}
