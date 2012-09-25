/*
 *

 */
package com.compomics.pride_asa_pipeline.service.impl;

import com.compomics.pride_asa_pipeline.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Identifications;
import com.compomics.pride_asa_pipeline.model.MascotGenericFile;
import com.compomics.pride_asa_pipeline.model.Peak;
import com.compomics.pride_asa_pipeline.repository.ExperimentRepository;
import com.compomics.pride_asa_pipeline.service.ExperimentService;
import com.compomics.pride_asa_pipeline.service.ResultHandler;
import com.compomics.pride_asa_pipeline.service.SpectrumService;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.*;
import java.util.*;
import javax.annotation.Nullable;
import org.apache.log4j.Logger;

/**
 * @author Niels Hulstaert
 */
public class ExperimentServiceImpl implements ExperimentService {

    private static final Logger LOGGER = Logger.getLogger(ExperimentServiceImpl.class);
    private static final String MALDI_SOURCE_ACCESSION = "PSI:1000075";
    private ExperimentRepository experimentRepository;
    private SpectrumService spectrumService;
    private ResultHandler resultHandler;
    private boolean iFirstMfgFile;

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

    public ResultHandler getResultHandler() {
        return resultHandler;
    }

    public void setResultHandler(ResultHandler resultHandler) {
        this.resultHandler = resultHandler;
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
    public File getSpectrumCacheAsMgfFile(String experimentAccession, boolean rebuildCache) {

        String lPath_tmp = PropertiesConfigurationHolder.getInstance().getString("results_path_tmp");
        File tempDir = new File(lPath_tmp);
        if (!tempDir.exists()) {
            tempDir.mkdir();
        }

        File file = new File(tempDir, experimentAccession + ".mgf");
        LOGGER.debug(String.format("writing spectra from experiment %s to %s", experimentAccession, file.getAbsolutePath()));

        if (rebuildCache) {
            LOGGER.debug(String.format("rebuilding spectrum cache for experiment %s", experimentAccession));
            buildSpectrumCacheForExperiment(experimentAccession);
        }

        BufferedOutputStream outputStream = null;
        try {
            //create new mgf file from scratch
            MascotGenericFile mascotGenericFile = new MascotGenericFile();
            mascotGenericFile.setFilename(experimentAccession);

            outputStream = new BufferedOutputStream(new FileOutputStream(file));
            writeCachedSpectra(outputStream, experimentAccession);
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

    @Override
    public void buildSpectrumCacheForExperiment(String experimentAccession) {

        // First clear the existing cache.
        LOGGER.debug(String.format("clearing spectrum cache before starting to cache all spectra for experiment %s", experimentAccession));
        spectrumService.clearCache();

        //get spectra metadata
        List<Map<String, Object>> spectraMetadata = experimentRepository.getSpectraMetadata(experimentAccession);


        HashMap<Long, Map> spectrumIdMap = new HashMap<Long, Map>();
        for (Map<String, Object> spectrumMetadata : spectraMetadata) {
            spectrumIdMap.put((Long) spectrumMetadata.get("spectrum_id"), spectrumMetadata);
        }

        Iterator<Long> lSpectrumIdIterator = spectrumIdMap.keySet().iterator();
        ArrayList<Long> lSpectrumidCacheList = Lists.newArrayList();

        while (lSpectrumIdIterator.hasNext()) {
            Long spectrumId = lSpectrumIdIterator.next();
            lSpectrumidCacheList.add(spectrumId);
        }

        if (lSpectrumidCacheList.size() > 0) {
            spectrumService.cacheSpectra(lSpectrumidCacheList);
            LOGGER.debug(String.format("added %s entries to the spectrum Cache", lSpectrumidCacheList.size()));
        }

    }

    private void writeCachedSpectra(BufferedOutputStream aOutputStream, String experimentAccession) throws IOException {
        MascotGenericFile mascotGenericFile = null;
        PeakToMapFunction peakToMap = new PeakToMapFunction();

        //get spectra metadata
        List<Map<String, Object>> spectraMetadata = experimentRepository.getSpectraMetadata(experimentAccession);


        HashMap<Long, Map> spectrumIdMap = new HashMap<Long, Map>();
        for (Map<String, Object> spectrumMetadata : spectraMetadata) {
            spectrumIdMap.put((Long) spectrumMetadata.get("spectrum_id"), spectrumMetadata);
        }

        for (Long cachedSpectrumId : spectrumIdMap.keySet()) {
            Map spectrumMetadata = spectrumIdMap.get(cachedSpectrumId);


            //write identification data to stream
            mascotGenericFile = new MascotGenericFile();

            mascotGenericFile.setTitle(cachedSpectrumId.toString());

            Object lPrecursor_mz = spectrumMetadata.get("precursor_mz");
            if (lPrecursor_mz != null) {
                mascotGenericFile.setPrecursorMZ(Double.parseDouble(lPrecursor_mz.toString()));
            }

            Object lPrecursor_charge_state = spectrumMetadata.get("precursor_charge_state");
            if (lPrecursor_charge_state != null) {
                mascotGenericFile.setCharge(Integer.parseInt(lPrecursor_charge_state.toString()));
            }

            mascotGenericFile.setPeaks(peakToMap.apply(spectrumService.getSpectrumPeaksBySpectrumId(cachedSpectrumId)));

            //first mascot generic file has to start at the first line
            //else insert empty line
            if (iFirstMfgFile) {
                mascotGenericFile.setComments("");
                iFirstMfgFile = Boolean.FALSE;
            } else {
                mascotGenericFile.setComments("\n\n");
            }
            mascotGenericFile.writeToStream(aOutputStream);
        }
        LOGGER.debug(String.format("finished writing %d spectra to %s", spectraMetadata.size(), mascotGenericFile.getFilename()));

    }

    /**
     * Helper function class to Convert the pride-asap List<Peak> instances in
     * the HashMaps for the MascotGenericFile instance.
     */
    private class PeakToMapFunction implements Function<List<Peak>, HashMap> {

        @Override
        public HashMap apply(@Nullable List<Peak> aPeaks) {
            HashMap lResult = Maps.newHashMap();
            for (Peak lPeak : aPeaks) {
                lResult.put(lPeak.getMzRatio(), lPeak.getIntensity());
            }
            return lResult;
        }
    }
}
