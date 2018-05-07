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



public class NoiseThresholdFinderTest {

    private static double[] values_1;
    private static double[] values_2;
    private static double[] values_3;
    private NoiseThresholdFinder noiseTresholdFinder;

    @BeforeClass
    public static void setUponce() throws Exception {
        File resource = new File(NoiseThresholdFinderTest.class.getClassLoader().getResource("Filter_TestData_1.txt").toURI());
        BufferedReader br = new BufferedReader(new FileReader(resource));

        String line = null;
        List valuesList = new ArrayList<Double>();
        while ((line = br.readLine()) != null) {
            valuesList.add(Double.parseDouble(line));
        }
        values_1 = ArrayUtils.toPrimitive((Double[]) valuesList.toArray(new Double[valuesList.size()]));

        br.close();

        resource = new File(NoiseThresholdFinderTest.class.getClassLoader().getResource("Filter_TestData_2.txt").toURI());
        br = new BufferedReader(new FileReader(resource));

        line = null;
        valuesList = new ArrayList<Double>();
        while ((line = br.readLine()) != null) {
            valuesList.add(Double.parseDouble(line));
        }
        values_2 = ArrayUtils.toPrimitive((Double[]) valuesList.toArray(new Double[valuesList.size()]));

        br.close();

        resource =  new File(NoiseThresholdFinderTest.class.getClassLoader().getResource("Filter_TestData_3.txt").toURI());

        br = new BufferedReader(new FileReader(resource));

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
