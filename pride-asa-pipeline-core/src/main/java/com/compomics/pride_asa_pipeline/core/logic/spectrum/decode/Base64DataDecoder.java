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
package com.compomics.pride_asa_pipeline.core.logic.spectrum.decode;

/**
 * Created by IntelliJ IDEA.
 * User: niels
 * Date: 4/01/12
 * Time: 11:38
 * To change this template use File | Settings | File Templates.
 */
public interface Base64DataDecoder {
    
    /**
     * Gets the binary data as a double array
     * 
     * @param dataPrecision precision for encoded doubles
     * @param dataEndian data type, big and little endian
     * @param base64DataString the base 64 encoded string 
     * @return the data array
     */
    double[] getDataAsArray(String dataPrecision, String dataEndian, String base64DataString);

}
