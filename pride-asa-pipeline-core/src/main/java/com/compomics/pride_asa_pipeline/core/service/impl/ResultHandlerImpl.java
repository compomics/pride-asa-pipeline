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
package com.compomics.pride_asa_pipeline.core.service.impl;

import com.compomics.pride_asa_pipeline.core.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.core.logic.modification.ModificationMarshaller;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.core.model.SpectrumAnnotatorResult;
import com.compomics.pride_asa_pipeline.model.comparator.IdentificationSpectrumIdComparator;
import com.compomics.pride_asa_pipeline.core.repository.FileResultHandler;
import com.compomics.pride_asa_pipeline.core.service.ResultHandler;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 *
 * @author Niels Hulstaert
 */
public class ResultHandlerImpl implements ResultHandler {

    private FileResultHandler fileResultHandler;
    private ModificationMarshaller modificationMarshaller;

    public FileResultHandler getFileResultHandler() {
        return fileResultHandler;
    }

    public void setFileResultHandler(FileResultHandler fileResultHandler) {
        this.fileResultHandler = fileResultHandler;
    }

    public ModificationMarshaller getModificationMarshaller() {
        return modificationMarshaller;
    }

    public void setModificationMarshaller(ModificationMarshaller modificationMarshaller) {
        this.modificationMarshaller = modificationMarshaller;
    }

    @Override
    public File writeResultToFile(SpectrumAnnotatorResult spectrumAnnotatorResult) {
        File resultFile = new File(PropertiesConfigurationHolder.getInstance().getString("results_path"), spectrumAnnotatorResult.getExperimentAccession() + ".txt");

        List<Identification> identifications = spectrumAnnotatorResult.getIdentifications();
        //sort list by spectrum ID
        Collections.sort(identifications, new IdentificationSpectrumIdComparator());

        fileResultHandler.writeResult(resultFile, spectrumAnnotatorResult.getIdentifications());
        return resultFile;
    }

    @Override
    public SpectrumAnnotatorResult readResultFromFile(File resultFile) {
        return fileResultHandler.readResult(resultFile);
    }

    @Override
    public void writeUsedModificationsToFile(String experimentAccession, Set<Modification> usedModifications) {
        File usedModificationsResource = new File((PropertiesConfigurationHolder.getInstance().getString("results_path") + File.separator + experimentAccession + "_mods.xml"));
        modificationMarshaller.marshall(usedModificationsResource, usedModifications);
    }

}
