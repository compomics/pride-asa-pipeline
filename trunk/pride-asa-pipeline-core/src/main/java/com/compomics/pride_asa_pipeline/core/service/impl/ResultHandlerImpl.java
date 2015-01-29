/*
 *

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
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

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
        Resource usedModificationsResource = new FileSystemResource(PropertiesConfigurationHolder.getInstance().getString("results_path") + File.separator + experimentAccession + "_mods.xml");
        modificationMarshaller.marshall(usedModificationsResource, usedModifications);
    }

}
