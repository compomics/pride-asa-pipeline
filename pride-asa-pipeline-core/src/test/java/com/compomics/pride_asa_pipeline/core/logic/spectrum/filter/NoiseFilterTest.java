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

import com.compomics.pride_asa_pipeline.model.Peak;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Niels Hulstaert Hulstaert
 */
public class NoiseFilterTest {

    private static List<Peak> peakList;
    private NoiseFilter noiseFilter;

    @BeforeClass
    public static void setUponce() throws Exception {
        File resource = new File(NoiseFilter.class.getClassLoader().getResource("Filter_TestData_1.txt").toURI());

        BufferedReader br = new BufferedReader(new FileReader(resource));
        peakList = new ArrayList<Peak>();

        String line = null;
        while ((line = br.readLine()) != null) {
            double intensity = (Math.rint(Double.parseDouble(line)) % 2 == 0) ? 400 : 200;
            Peak peak = new Peak(Double.parseDouble(line), intensity);
            peakList.add(peak);
        }

        br.close();
    }

    @Test
    public void testFilterNoise() {
        double threshold = 300D;
        double precursorMass = 1510D;

        List<Peak> filteredPeaks = noiseFilter.filterNoise(peakList, threshold, precursorMass);

        for (Peak peak : filteredPeaks) {
            //check if values are filtered on intensity
            assertTrue(peak.getIntensity() > threshold);
            //check if precursor mass is omitted
            assertFalse((precursorMass - 18) < peak.getMzRatio() && peak.getMzRatio() < (precursorMass + 18));
        }
    }
}
