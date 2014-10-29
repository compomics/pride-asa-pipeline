package com.compomics.pride_asa_pipeline.core.service.impl;

import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Identifications;
import com.compomics.respindataextractor.dataextraction.extractors.parameters.FileParser;
import com.compomics.pride_asa_pipeline.core.service.FileExperimentService;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 *
 * @author Niels Hulstaert
 */
public class FileExperimentServiceImpl extends ExperimentServiceImpl implements FileExperimentService {

    private static final Logger LOGGER = Logger.getLogger(FileExperimentServiceImpl.class);
    private FileParser fileParser;

    public FileExperimentServiceImpl() {
    }

    @Override
    public void setFileParser(FileParser fileParser) {
        this.fileParser = fileParser;
    }

    @Override
    public Map<String, String> findAllExperimentAccessions() {
        return experimentRepository.findAllExperimentAccessions();
    }

    @Override
    public Map<String, String> findExperimentAccessionsByTaxonomy(int taxonomyId) {
        return experimentRepository.findExperimentAccessionsByTaxonomy(taxonomyId);
    }

    @Override
    public Identifications loadExperimentIdentifications() {
        Identifications identifications = new Identifications();
        //@todo check if the parser is initialized
        List<Identification> identificationList = fileParser.getExperimentIdentifications();
        for (Identification identification : identificationList) {
            identifications.addIdentification(identification);
        }
        return identifications;
    }

    @Override
    public void updateChargeStates(Set<Integer> chargeStates) {
        Map<String, String> analyzerSources = fileParser.getAnalyzerSources();
        if (analyzerSources.containsKey(MALDI_SOURCE_ACCESSION)) {
            LOGGER.debug("Found MALDI source, setting charge state to 1.");
            chargeStates.clear();
            chargeStates.add(1);
        } else {
            //iterate over values to check if a maldi source is found
            for (String value : analyzerSources.values()) {
                if (value.indexOf("maldi") > -1 || value.indexOf("matrix") > -1) {
                    LOGGER.debug("Found MALDI source, setting charge state to 1.");
                    chargeStates.clear();
                    chargeStates.add(1);
                }
            }
        }
    }

    @Override
    public AnalyzerData getAnalyzerData() {
        //@ToDo: for the moment, only take the first result into account, check the other results
        List<AnalyzerData> analyzerDataList = fileParser.getAnalyzerData();
        AnalyzerData analyzerData = null;
        if (!analyzerDataList.isEmpty()) {
            analyzerData = analyzerDataList.get(0);
            if (analyzerDataList.size() != 1) {
                for (AnalyzerData ad : analyzerDataList) {
                    if (!analyzerData.getAnalyzerFamily().equals(AnalyzerData.ANALYZER_FAMILY.UNKNOWN)) {
                        analyzerData = ad;
                        break;
                    }
                }
            }
        }
        return analyzerData;
    }

    @Override
    public long getNumberOfSpectra() {
        return fileParser.getNumberOfSpectra();
    }

    @Override
    public Set<String> getProteinAccessions() {
        Set<String> proteinAccessions = new HashSet<>();
        List<String> proteinAccessionList = fileParser.getProteinAccessions();
        for (String proteinAccession : proteinAccessionList) {
            proteinAccessions.add(proteinAccession);
        }
        return proteinAccessions;
    }

    @Override
    public long getNumberOfPeptides() {
        return fileParser.getNumberOfPeptides();
    }

    @Override
    public void init(File identificationsFile) {
        fileParser.init(identificationsFile);
    }

    @Override
    public void clear() {
        fileParser.clear();
    }
}
