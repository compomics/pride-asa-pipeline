/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.service.impl;

import com.compomics.mslims.util.fileio.MascotGenericFile;
import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Identifications;
import com.compomics.pride_asa_pipeline.model.SpectrumAnnotatorResult;
import com.compomics.pride_asa_pipeline.repository.ExperimentRepository;
import com.compomics.pride_asa_pipeline.service.ExperimentService;
import com.compomics.pride_asa_pipeline.service.SpectrumService;
import com.google.common.io.Files;
import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 *
 * @author niels
 */
public class ExperimentServiceImpl implements ExperimentService {

    private static final Logger LOGGER = Logger.getLogger(ExperimentServiceImpl.class);
    private static final String MALDI_SOURCE_ACCESSION = "PSI:1000075";
    private ExperimentRepository experimentRepository;
    private SpectrumService spectrumService;

    public ExperimentRepository getExperimentRepository() {
        return experimentRepository;
    }

    public void setExperimentRepository(ExperimentRepository experimentRepository) {
        this.experimentRepository = experimentRepository;
    }

    public SpectrumService getSpectrumService() {
        return spectrumService;
    }

    public void setSpectrumService(SpectrumService spectrumService) {
        this.spectrumService = spectrumService;
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
    public Identifications loadExperimentIdentifications(String experimentAccession) {
        Identifications identifications = new Identifications();
        List<Identification> identificationList = experimentRepository.loadExperimentIdentifications(experimentAccession);
        for (Identification identification : identificationList) {
            identifications.addIdentification(identification);
        }
        return identifications;
    }

    @Override
    public void updateChargeStates(String experimentAccession, Set<Integer> chargeStates) {
        Map<String, String> analyzerSources = experimentRepository.getAnalyzerSources(experimentAccession);
        if (analyzerSources.containsKey(MALDI_SOURCE_ACCESSION)) {
            LOGGER.debug("Found MALDI source, setting charge state to 1.");
            chargeStates = new HashSet<Integer>();
            chargeStates.add(1);
        } else {
            //iterate over values to check if a maldi source is found
            for (String value : analyzerSources.values()) {
                if (value.indexOf("maldi") > -1 || value.indexOf("matrix") > -1) {
                    LOGGER.debug("Found MALDI source, setting charge state to 1.");
                    chargeStates = new HashSet<Integer>();
                    chargeStates.add(1);
                }
            }
        }
    }

    @Override
    public AnalyzerData getAnalyzerData(String experimentAccession) {
        //ToDo: for the moment, only take the first result into account, check the other results
        List<AnalyzerData> analyzerDataList = experimentRepository.getAnalyzerData(experimentAccession);
        return analyzerDataList.get(0);
    }

    @Override
    public long getNumberOfSpectra(String experimentAccession) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<String> getProteinAccessions(String experimentAccession) {
        Set<String> proteinAccessions = new HashSet<String>();
        List<String> proteinAccessionList = experimentRepository.getProteinAccessions(experimentAccession);
        for (String proteinAccession : proteinAccessionList) {
            proteinAccessions.add(proteinAccession);
        }
        return proteinAccessions;
    }

    @Override
    public long getNumberOfPeptides(String experimentAccession) {
        return experimentRepository.getNumberOfPeptides(experimentAccession);
    }

    @Override
    public File getSpectraAsMgfFile(String experimentAccession) {
        File tempDir = Files.createTempDir();
        File file = new File(tempDir, experimentAccession + ".mgf");

        //get spectra metadata
        List<Map<String, Object>> spectraMetadata = experimentRepository.getSpectraMetadata(experimentAccession);

        //create new mgf file from scratch
        MascotGenericFile mascotGenericFile = new MascotGenericFile();
        mascotGenericFile.setFilename(experimentAccession);

        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);

            boolean isFirstMfgFile = Boolean.TRUE;
            for (Map<String, Object> spectrumMetadata : spectraMetadata) {
                //write identification data to stream
                mascotGenericFile = new MascotGenericFile();
                Long spectrumId = (Long) spectrumMetadata.get("spectrum_id");
                mascotGenericFile.setTitle(spectrumId.toString());
                mascotGenericFile.setPrecursorMZ(Double.parseDouble(spectrumMetadata.get("precursor_mz").toString()));
                mascotGenericFile.setCharge(Integer.parseInt(spectrumMetadata.get("precursor_charge_state").toString()));
                mascotGenericFile.setPeaks(spectrumService.getSpectrumPeakMapBySpectrumId(spectrumId));
                //first mascot generic file has to start at the first line
                //else insert empty line
                if (isFirstMfgFile) {
                    mascotGenericFile.setComments("");
                    isFirstMfgFile = Boolean.FALSE;
                } else {
                    mascotGenericFile.setComments("\n\n");
                }
                mascotGenericFile.writeToStream(outputStream);
            }
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }

        return file;
    }
}
