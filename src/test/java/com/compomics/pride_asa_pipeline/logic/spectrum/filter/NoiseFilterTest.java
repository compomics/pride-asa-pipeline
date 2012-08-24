package com.compomics.pride_asa_pipeline.logic.spectrum.filter;

import com.compomics.pride_asa_pipeline.model.Peak;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by IntelliJ IDEA.
 * User: niels
 * Date: 9/12/11
 * Time: 14:33
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:springXMLConfig.xml")
public class NoiseFilterTest {

    private static final Logger LOGGER = Logger.getLogger(NoiseFilterTest.class);
    private static List<Peak> peakList;
    
    @Autowired
    private NoiseFilter noiseFilter;            

    @BeforeClass
    public static void setUponce() throws Exception {
        Resource resource = new ClassPathResource("Filter_TestData.txt");
        File file = resource.getFile();

        BufferedReader br = new BufferedReader(new FileReader(file));
        peakList = new ArrayList<Peak>();

        String line = null;
        while( (line = br.readLine()) != null ){
            double intensity = (Math.rint(Double.parseDouble(line))%2==0) ? 400 : 200;
            Peak peak = new Peak(Double.parseDouble(line), intensity);
            peakList.add(peak);
        }

        br.close();
    }

    @Test
    public void testFilterNoise(){        
        double threshold = 300D;
        double precursorMass = 1510D;

        List<Peak> filteredPeaks = noiseFilter.filterNoise(peakList, threshold , precursorMass);

        for(Peak peak : filteredPeaks){
            //check if values are filtered on intensity
            assertTrue(peak.getIntensity() > threshold);
            //check if precursor mass is omitted
            assertFalse((precursorMass - 18) < peak.getMzRatio() && peak.getMzRatio() < (precursorMass + 18));
        }
    }

}
