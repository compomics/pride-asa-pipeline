package com.compomics.pride_asa_pipeline.spectrum.filter;

import com.compomics.pride_asa_pipeline.spectrum.filter.impl.WinsorNoiseThresholdFinder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.ArrayUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import static junit.framework.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by IntelliJ IDEA.
 * User: niels
 * Date: 28/10/11
 * Time: 16:42
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:springXMLConfig.xml")
public class NoiseThresholdFinderTest {

    private static double [] values;
    
    @Autowired
    private NoiseThresholdFinder noiseTresholdFinder;

    @BeforeClass
    public static void setUponce() throws Exception {
        File file = new File(NoiseThresholdFinderTest.class.getClassLoader().getResource("Filter_TestData.txt").getPath());

        BufferedReader br = new BufferedReader(new FileReader(file));

        String line = null;
        List valuesList = new ArrayList<Double>();
        while( (line = br.readLine()) != null ){
            valuesList.add(Double.parseDouble(line));
        }
        values = ArrayUtils.toPrimitive((Double[])valuesList.toArray(new Double[valuesList.size()]));

        br.close();
    }

    @Test
    public void testWinsorNoiseTresholdFinder(){        
        double threshold = noiseTresholdFinder.findNoiseThreshold(values);
        assertEquals(104.47, threshold, 0.01);
    }

}
