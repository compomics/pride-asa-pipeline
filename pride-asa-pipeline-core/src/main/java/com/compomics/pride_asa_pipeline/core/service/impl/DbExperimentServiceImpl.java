/*
 *

 */
package com.compomics.pride_asa_pipeline.core.service.impl;

import com.compomics.pride_asa_pipeline.core.model.MascotGenericFile;
import com.compomics.pride_asa_pipeline.core.service.DbExperimentService;
import com.compomics.pride_asa_pipeline.core.service.DbSpectrumService;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Identifications;
import com.compomics.pride_asa_pipeline.model.Peak;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.apache.log4j.Logger;

/**
 * @author Niels Hulstaert
 */
public class DbExperimentServiceImpl extends ExperimentServiceImpl implements DbExperimentService {

    private static final Logger LOGGER = Logger.getLogger(DbExperimentServiceImpl.class);
    private DbSpectrumService spectrumService;
    private boolean iFirstMfgFile;

    public DbSpectrumService getSpectrumService() {
        return spectrumService;
    }

    public void setSpectrumService(DbSpectrumService spectrumService) {
        this.spectrumService = spectrumService;
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
        //go over the identifications and check their charges
        List<Identification> loadExperimentIdentifications = experimentRepository.loadExperimentIdentifications(experimentAccession);
        for (Identification ident : loadExperimentIdentifications) {
            chargeStates.add(ident.getPeptide().getCharge());
        }
    }

    @Override
    public long getNumberOfSpectra(String experimentAccession) {
        return experimentRepository.getNumberOfSpectra(experimentAccession);
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
    public void getSpectraAsMgfFile(String experimentAccession, File mgfFile, boolean rebuildCache) {
        LOGGER.debug(String.format("writing spectra from experiment %s to %s", experimentAccession, mgfFile.getAbsolutePath()));

        if (rebuildCache) {
            LOGGER.debug(String.format("rebuilding spectrum cache for experiment %s", experimentAccession));
            buildSpectrumCacheForExperiment(experimentAccession);
        }

        BufferedOutputStream outputStream = null;
        try {
            //create new mgf file from scratch
            MascotGenericFile mascotGenericFile = new MascotGenericFile();
            mascotGenericFile.setFilename(experimentAccession);

            outputStream = new BufferedOutputStream(new FileOutputStream(mgfFile));
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
    }

    /**
     * Builds up the spectrum cache for an entire experiment.
     *
     * @param experimentAccession
     */
    private void buildSpectrumCacheForExperiment(String experimentAccession) {

        //clear the existing cache
        LOGGER.debug(String.format("clearing spectrum cache before starting to cache all spectra for experiment %s", experimentAccession));
        spectrumService.clearCache();

        //get spectra metadata
        List<Map<String, Object>> spectraMetadata = experimentRepository.getSpectraMetadata(experimentAccession);

        HashMap<String, Map> spectrumIdMap = new HashMap<String, Map>();
        for (Map<String, Object> spectrumMetadata : spectraMetadata) {
            spectrumIdMap.put((String) spectrumMetadata.get("spectrum_id"), spectrumMetadata);
        }

        Iterator<String> lSpectrumIdIterator = spectrumIdMap.keySet().iterator();
        ArrayList<String> lSpectrumidCacheList = Lists.newArrayList();

        while (lSpectrumIdIterator.hasNext()) {
            String spectrumId = lSpectrumIdIterator.next();
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

        HashMap<String, Map> spectrumIdMap = new HashMap<String, Map>();
        for (Map<String, Object> spectrumMetadata : spectraMetadata) {
            spectrumIdMap.put((String) spectrumMetadata.get("spectrum_id"), spectrumMetadata);
        }

        for (String cachedSpectrumId : spectrumIdMap.keySet()) {
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
