package com.compomics.pride_asa_pipeline.core.repository.impl.file;

import com.compomics.pride_asa_pipeline.core.data.extractor.MGFExtractor;
import com.compomics.pride_asa_pipeline.core.model.MGFExtractionException;
import com.compomics.pride_asa_pipeline.core.model.ParserCacheConnector;
import com.compomics.pride_asa_pipeline.core.repository.SpectrumRepository;
import com.compomics.pride_asa_pipeline.model.Peak;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import uk.ac.ebi.pride.utilities.data.core.Spectrum;

/**
 *
 * @author Kenneth Verheggen
 */
public class FileSpectrumRepository extends ParserCacheConnector implements SpectrumRepository {

    /**
     * The identifier for the current repository (filename or assay accession)
     */
    private String experimentIdentifier;

    public FileSpectrumRepository() {

    }

    public FileSpectrumRepository(String experimentIdentifier) {
        this.experimentIdentifier = experimentIdentifier;
    }

    public FileSpectrumRepository(File identificationsFile) {
        this.experimentIdentifier = identificationsFile.getName();
    }

    public String getExperimentIdentifier() {
        return experimentIdentifier;
    }

    public void setExperimentIdentifier(String experimentIdentifier) {
        this.experimentIdentifier = experimentIdentifier;
    }

    @Override
    public double[] getMzValuesBySpectrumId(String spectrumId) {
        Spectrum spectrumById = parserCache.getParser(experimentIdentifier, true).getSpectrumById(spectrumId);
        return spectrumById.getMassIntensityMap()[0];
    }

    @Override
    public double[] getIntensitiesBySpectrumId(String spectrumId) {
        Spectrum spectrumById = parserCache.getParser(experimentIdentifier, true).getSpectrumById(spectrumId);
        return spectrumById.getMassIntensityMap()[1];
    }

    @Override
    public Map<String, List<Peak>> getPeakMapsBySpectrumIdList(List<String> spectrumIds) {
        Map<String, List<Peak>> peakMap = new HashMap<>();
        for (String aSpectrumID : spectrumIds) {
            Spectrum spectrumById = parserCache.getParser(experimentIdentifier, true).getSpectrumById(aSpectrumID);
            List<Peak> peakList = new ArrayList<>();
            double[][] massIntensityMap = spectrumById.getMassIntensityMap();
            for (int i = 0; i < massIntensityMap.length; i++) {
                peakList.add(new Peak(massIntensityMap[0][i], massIntensityMap[1][i]));
            }
            peakMap.put(aSpectrumID, peakList);
        }
        return peakMap;
    }

    /**
     * Writes an mgf file with the contents of all cached peak files
     *
     * @param experimentID the assay identifier
     * @param outputFile the output folder for mgf files
     * @throws IOException when something goes wrong in the reading or writing
     */
    public File writeToMGF(File outputFolder) throws IOException, MGFExtractionException {
        final long timeout = 30000;
        File mgf = new File(outputFolder, experimentIdentifier + ".mgf");
        if(!mgf.exists()){
            mgf.getParentFile().mkdirs();
            mgf.createNewFile();
        }
        try (FileWriter writer = new FileWriter(mgf, true)) {
            for (File aPeakFile : parserCache.getPeakFiles(experimentIdentifier)) {
                File tempOut = new File(aPeakFile.getParentFile(), aPeakFile.getName() + ".asap.temp.mgf");
                tempOut.deleteOnExit();
                try {
                    new MGFExtractor(aPeakFile).extractMGF(tempOut, timeout);
                    String mgfAsString = FileUtils.readFileToString(tempOut);
                    writer.append(mgfAsString).append(System.lineSeparator());
                } finally {
                    tempOut.delete();
                }
            }
        }
        return mgf;
    }
}
