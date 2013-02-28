package com.compomics.pride_asa_pipeline.service.impl;

import com.compomics.pride_asa_pipeline.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Identifications;
import com.compomics.pride_asa_pipeline.model.Peak;
import com.compomics.pride_asa_pipeline.repository.ExperimentRepository;
import com.compomics.pride_asa_pipeline.repository.PrideXmlParser;
import com.compomics.pride_asa_pipeline.service.ExperimentService;
import com.compomics.pride_asa_pipeline.service.PrideXmlExperimentService;
import com.compomics.pride_asa_pipeline.service.ResultHandler;
import com.compomics.pride_asa_pipeline.service.SpectrumService;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.apache.log4j.Logger;

/**
 *
 * @author Niels Hulstaert
 */
public class PrideXmlExperimentServiceImpl extends ExperimentServiceImpl implements PrideXmlExperimentService {

    private static final Logger LOGGER = Logger.getLogger(PrideXmlExperimentServiceImpl.class);
    private SpectrumService spectrumService;
    private PrideXmlParser prideXmlParser;
  
    public SpectrumService getSpectrumService() {
        return spectrumService;
    }

    public void setSpectrumService(SpectrumService spectrumService) {
        this.spectrumService = spectrumService;
    }
  
    public PrideXmlParser getPrideXmlParser() {
        return prideXmlParser;
    }

    public void setPrideXmlParser(PrideXmlParser prideXmlParser) {
        this.prideXmlParser = prideXmlParser;
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
    public Identifications loadExperimentIdentifications(File experimentPrideXmlFile) {
        Identifications identifications = new Identifications();
        //@todo check if the parser is initialized
        List<Identification> identificationList = prideXmlParser.getExperimentIdentifications();
        for (Identification identification : identificationList) {
            identifications.addIdentification(identification);
        }
        return identifications;
    }

    @Override
    public void updateChargeStates(Set<Integer> chargeStates) {
        Map<String, String> analyzerSources = prideXmlParser.getAnalyzerSources();
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
        List<AnalyzerData> analyzerDataList = prideXmlParser.getAnalyzerData();
        AnalyzerData analyzerData = null;
        if(analyzerDataList.isEmpty()){            
            analyzerData = analyzerDataList.get(0);            
            if(analyzerDataList.size() != 1){
                for(AnalyzerData ad : analyzerDataList){
                    if(!analyzerData.getAnalyzerFamily().equals(AnalyzerData.ANALYZER_FAMILY.UNKNOWN)){
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
        return prideXmlParser.getNumberOfSpectra();
    }

    @Override
    public Set<String> getProteinAccessions() {
        Set<String> proteinAccessions = new HashSet<String>();
        //List<String> proteinAccessionList = experimentRepository.getProteinAccessions(experimentAccession);
//        for (String proteinAccession : proteinAccessionList) {
//            proteinAccessions.add(proteinAccession);
//        }
        return proteinAccessions;
    }

    @Override
    public long getNumberOfPeptides() {
        return prideXmlParser.getNumberOfPeptides();
    }

    @Override
    public File getSpectraAsMgfFile() {

        String lPath_tmp = PropertiesConfigurationHolder.getInstance().getString("results_path_tmp");
        File tempDir = new File(lPath_tmp);
        if (!tempDir.exists()) {
            tempDir.mkdir();
        }

        //File file = new File(tempDir, experimentAccession + ".mgf");
        //LOGGER.debug(String.format("writing spectra from experiment %s to %s", experimentAccession, file.getAbsolutePath()));

//        if (rebuildCache) {
//            LOGGER.debug(String.format("rebuilding spectrum cache for experiment %s", experimentAccession));
//            //buildSpectrumCacheForExperiment(experimentAccession);
//        }
//
//        BufferedOutputStream outputStream = null;
//        try {
//            //create new mgf file from scratch
//            MascotGenericFile mascotGenericFile = new MascotGenericFile();
//            mascotGenericFile.setFilename(experimentAccession);
//
//            outputStream = new BufferedOutputStream(new FileOutputStream(file));
//            writeCachedSpectra(outputStream, experimentAccession);
//        } catch (FileNotFoundException e) {
//            LOGGER.error(e.getMessage(), e);
//        } catch (IOException e) {
//            LOGGER.error(e.getMessage(), e);
//        } finally {
//            if (outputStream != null) {
//                try {
//                    outputStream.close();
//                } catch (IOException e) {
//                    LOGGER.error(e.getMessage(), e);
//                }
//            }
//        }

        return null;
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
