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
package com.compomics.pride_asa_pipeline.core.repository;

import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.core.model.SpectrumAnnotatorResult;
import java.io.File;
import java.util.List;

/**
 *
 * @author Niels Hulstaert
 */
public interface FileResultHandler {

    /**
     * Writes the identifications to file
     *
     * @param resultFile the spectrum annotation pipeline result
     * @param identifications the experiment identifications
     */
    void writeResult(File resultFile, List<Identification> identifications);

    /**
     * Parses the result file and returns the SpectrumAnnotatorResult
     *
     * @param resultFile the spectrum annotation pipeline result
     * @return the spectrum annotator result
     */
    SpectrumAnnotatorResult readResult(File resultFile);
}
