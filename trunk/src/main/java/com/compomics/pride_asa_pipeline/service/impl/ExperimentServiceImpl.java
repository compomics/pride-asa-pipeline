/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.service.impl;

import com.compomics.mslims.util.fileio.MascotGenericFile;
import com.compomics.pride_asa_pipeline.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Identifications;
import com.compomics.pride_asa_pipeline.repository.ExperimentRepository;
import com.compomics.pride_asa_pipeline.service.ExperimentService;
import com.compomics.pride_asa_pipeline.service.SpectrumService;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

/**
 * @author niels
 */
public class ExperimentServiceImpl implements ExperimentService {

    private static final Logger LOGGER = Logger.getLogger(ExperimentServiceImpl.class);
    private static final String MALDI_SOURCE_ACCESSION = "PSI:1000075";
    private ExperimentRepository experimentRepository;
    private SpectrumService spectrumService;
    protected boolean iFirstMfgFile;

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

        BufferedOutputStream outputStream = null;


        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(file));

            HashMap<Long, Map> spectrumIdMap = new HashMap<Long, Map>();
            for (Map<String, Object> spectrumMetadata : spectraMetadata) {
                spectrumIdMap.put((Long) spectrumMetadata.get("spectrum_id"), spectrumMetadata);
            }
            spectraMetadata = null;

            boolean spectrumLimit = PropertiesConfigurationHolder.getInstance().getBoolean("spectrum.limit");
            int spectrumLimitSize = PropertiesConfigurationHolder.getInstance().getInt("spectrum.limit.size");

            int spectrumCacheSize = PropertiesConfigurationHolder.getInstance().getInt("spectrum.cache");
            int spectrumCountRunner = 0;
            iFirstMfgFile = Boolean.TRUE;

            Iterator<Long> lSpectrumIdIterator = spectrumIdMap.keySet().iterator();
            ArrayList<Long> lSpectrumidCacheList = Lists.newArrayList();

            while (lSpectrumIdIterator.hasNext()) {
                Long spectrumId = lSpectrumIdIterator.next();
                lSpectrumidCacheList.add(spectrumId);
                spectrumCountRunner++;

                if(spectrumLimit && spectrumLimitSize > spectrumCountRunner){
                    LOGGER.debug(String.format("Spectrum limit enabled! Only writing %d MSMS spectra to %s", spectrumLimitSize, file.getName()));
                    break;
                }

                if (spectrumCountRunner % spectrumCacheSize == 0) {
                    // Make the spectrumService cache the current list of spectrumIds
                    spectrumService.cacheSpectra(lSpectrumidCacheList);

                    // Flush the cache to the file!
                    flushCachedSpectra(outputStream, spectrumIdMap, lSpectrumidCacheList);
                    lSpectrumidCacheList = Lists.newArrayList();

                } else {
                    // continue!
                }
            }

            // fence post!
            if (lSpectrumidCacheList.size() > 0) {
                spectrumService.cacheSpectra(lSpectrumidCacheList);
                flushCachedSpectra(outputStream, spectrumIdMap, lSpectrumidCacheList);
            }

            LOGGER.debug(String.format("finished writing %d spectra to %s", spectrumCountRunner, file.getAbsolutePath()));

        } catch (
                FileNotFoundException e
                )

        {
            LOGGER.error(e.getMessage(), e);
        } catch (
                IOException e
                )

        {
            LOGGER.error(e.getMessage(), e);
        } finally

        {
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

    private void flushCachedSpectra(BufferedOutputStream aOutputStream, HashMap<Long, Map> aSpectrumIdMap, ArrayList<Long> aSpectrumidCache) throws IOException {
        MascotGenericFile mascotGenericFile;
        for (Long cachedSpectrumId : aSpectrumidCache) {
            Map spectrumMetadata = aSpectrumIdMap.get(cachedSpectrumId);


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

            mascotGenericFile.setPeaks((HashMap) spectrumService.getCachedSpectrum(cachedSpectrumId));

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
    }
}
