package com.compomics.pride_asa_pipeline.core.logic.spectrum.filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;
import org.apache.commons.lang.ArrayUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by IntelliJ IDEA. User: niels Date: 28/10/11 Time: 16:42 To change
 * this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:springXMLConfig.xml")
public class NoiseThresholdFinderTest {

    private static double[] values_1;
    private static double[] values_2;
    private static double[] values_3;
    @Autowired
    private NoiseThresholdFinder noiseTresholdFinder;

    @BeforeClass
    public static void setUponce() throws Exception {
        Resource resource = new ClassPathResource("Filter_TestData_1.txt");
        File file = resource.getFile();

        BufferedReader br = new BufferedReader(new FileReader(file));

        String line = null;
        List valuesList = new ArrayList<Double>();
        while ((line = br.readLine()) != null) {
            valuesList.add(Double.parseDouble(line));
        }
        values_1 = ArrayUtils.toPrimitive((Double[]) valuesList.toArray(new Double[valuesList.size()]));

        br.close();

        resource = new ClassPathResource("Filter_TestData_2.txt");
        file = resource.getFile();

        br = new BufferedReader(new FileReader(file));

        line = null;
        valuesList = new ArrayList<Double>();
        while ((line = br.readLine()) != null) {
            valuesList.add(Double.parseDouble(line));
        }
        values_2 = ArrayUtils.toPrimitive((Double[]) valuesList.toArray(new Double[valuesList.size()]));

        br.close();

        resource = new ClassPathResource("Filter_TestData_3.txt");
        file = resource.getFile();

        br = new BufferedReader(new FileReader(file));

        line = null;
        valuesList = new ArrayList<Double>();
        while ((line = br.readLine()) != null) {
            valuesList.add(Double.parseDouble(line));
        }
        values_3 = ArrayUtils.toPrimitive((Double[]) valuesList.toArray(new Double[valuesList.size()]));

        br.close();
    }

    /**
     * Test the noise threshold finder for a spectrum with few signal peaks
     * compared to the noise peaks.
     */
    @Test
    public void testWinsorNoiseTresholdFinder_1() {
        double threshold = noiseTresholdFinder.findNoiseThreshold(values_1);
        assertEquals(104.47, threshold, 0.01);
    }

    /**
     * Test the noise threshold finder for a spectrum with only signal peaks.
     */
    @Test
    public void testWinsorNoiseTresholdFinder_2() {
        double threshold = noiseTresholdFinder.findNoiseThreshold(values_2);
        assertEquals(0.0, threshold, 0.01);
    }

    /**
     * Test the noise threshold finder for a spectrum with only noise peaks.
     */
    @Test
    public void testWinsorNoiseTresholdFinder_3() {
        double threshold = noiseTresholdFinder.findNoiseThreshold(values_3);
        assertEquals(102.93, threshold, 0.01);
    }
}
