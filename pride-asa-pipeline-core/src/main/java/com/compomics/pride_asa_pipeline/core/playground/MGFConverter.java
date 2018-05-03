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
package com.compomics.pride_asa_pipeline.core.playground;

import com.compomics.pride_asa_pipeline.core.data.extractor.MGFExtractor;
import com.compomics.pride_asa_pipeline.core.model.MGFExtractionException;
import java.io.File;

/**
 *
 * @author Kenneth
 */
public class MGFConverter {

    public static void main(String[] args) {
        try {
            //    File input = new File("C:\\Users\\Kenneth\\Desktop\\mzML-test\\A0214_WHIM16-P6-3_bhplc_20120605_r_klc_X_A3_t1_fr01.mzML");
            File input = new File("C:\\\\Users\\\\Kenneth\\\\Desktop\\\\mzML-test\\\\A0214_WHIM16-P6-3_bhplc_20120605_r_klc_X_A3_t1_fr06.mzML");
            File output = new File("C:\\Users\\Kenneth\\Desktop\\mzML-test\\output.mgf");
            MGFExtractor extractor = new MGFExtractor(input);
            extractor.extractMGF(output,1000);
        } catch (MGFExtractionException ex) {
            ex.printStackTrace();
        } finally {
            System.out.println("Done !");
        }
    }
}
