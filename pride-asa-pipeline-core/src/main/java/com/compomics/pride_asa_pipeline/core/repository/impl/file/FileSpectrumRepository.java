package com.compomics.pride_asa_pipeline.core.repository.impl.file;

import com.compomics.pride_asa_pipeline.core.data.extractor.MGFExtractor;
import com.compomics.pride_asa_pipeline.core.repository.ParserCacheConnector;
import com.compomics.pride_asa_pipeline.model.MGFExtractionException;
import com.compomics.pride_asa_pipeline.core.repository.SpectrumRepository;
import com.compomics.pride_asa_pipeline.model.Peak;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.apache.log4j.Logger;
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
    /**
     * Logging instance
     */
    private Logger LOGGER = Logger.getLogger(FileSpectrumRepository.class);

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
        try {
            Spectrum spectrumById = parserCache.getParser(experimentIdentifier, false).getSpectrumById(spectrumId);
            return spectrumById.getMassIntensityMap()[0];
        } catch (TimeoutException | InterruptedException | ExecutionException ex) {
            LOGGER.error("The parser timed out before it could deliver the spectrum");
        }
        return new double[0];
    }

    public double[][] getMzValuesMapBySpectrumId(String spectrumId) {
        try {
            Spectrum spectrumById = parserCache.getParser(experimentIdentifier, false).getSpectrumById(spectrumId);
            return spectrumById.getMassIntensityMap();
        } catch (TimeoutException | InterruptedException | ExecutionException ex) {
            LOGGER.error("The parser timed out before it could deliver the spectrum");
        }
        return new double[0][0];
    }

    @Override
    public double[] getIntensitiesBySpectrumId(String spectrumId) {
        try {
            Spectrum spectrumById = parserCache.getParser(experimentIdentifier, false).getSpectrumById(spectrumId);
            return spectrumById.getMassIntensityMap()[1];
        } catch (TimeoutException | InterruptedException | ExecutionException ex) {
            LOGGER.error("The parser timed out before it could deliver the spectrum");
        }
        return new double[0];
    }

    @Override
    public Map<String, List<Peak>> getPeakMapsBySpectrumIdList(List<String> spectrumIds) {
        Map<String, List<Peak>> peakMap = new HashMap<>();
        for (String aSpectrumID : spectrumIds) {
            try {
                Spectrum spectrumById = parserCache.getParser(experimentIdentifier, false).getSpectrumById(aSpectrumID);
                List<Peak> peakList = new ArrayList<>();
                double[][] massIntensityMap = spectrumById.getMassIntensityMap();
                for (int i = 0; i < massIntensityMap.length; i++) {
                    peakList.add(new Peak(massIntensityMap[0][i], massIntensityMap[1][i]));
                }
                peakMap.put(aSpectrumID, peakList);
            } catch (TimeoutException | InterruptedException | ExecutionException ex) {
                LOGGER.error("The parser timed out before it could deliver the peaks");
            }
        }
        return peakMap;
    }

      /**
     * Writes an mgf file with the contents of all cached peak files
     *
     * @param experimentID the assay identifier
     * @param merge boolean indicating if the MGF should be merged into a single mgf
     * @param outputFolder the output folder for mgf files
     * @throws IOException when something goes wrong in the reading or writing
     */
    public File writeToMGF(File outputFolder, boolean merge) throws IOException, MGFExtractionException {
        File mgf;
        if (merge) {
            mgf = writeToMergedMGF(outputFolder);
        } else {
            mgf = writeToMGFFolder(outputFolder);
        }
        return mgf;
    }

    /**
     * Writes an mgf file with the contents of all cached peak files
     *
     * @param experimentID the assay identifier
     * @param outputFile the output folder for mgf files
     * @throws IOException when something goes wrong in the reading or writing
     */
    public File writeToMergedMGF(File outputFolder) throws IOException, MGFExtractionException {
        final long timeout = 30000;
        File mgf = new File(outputFolder, experimentIdentifier + ".mgf");
        if (!mgf.exists()) {
            mgf.getParentFile().mkdirs();
            mgf.createNewFile();
        }
        try (FileWriter writer = new FileWriter(mgf, true)) {
            for (File aPeakFile : parserCache.getPeakFiles(experimentIdentifier)) {
                File tempOut = new File(aPeakFile.getParentFile(), aPeakFile.getName() + ".asap.temp.mgf");
                tempOut.deleteOnExit();
                try {
                    new MGFExtractor(aPeakFile).extractMGF(tempOut, timeout);
                    //merge everything in one big file
                    BufferedReader reader = new BufferedReader(new FileReader(tempOut));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        writer.append(line).append(System.lineSeparator()).flush();
                    }
                } finally {
                    tempOut.delete();
                }
            }
        }
        return mgf;
    }

    /**
     * Writes an mgf file with the contents of all cached peak files
     *
     * @param outputFolder the output folder for mgf files
     * @return the folder containing mgf formatted spectrum files
     * @throws IOException when something goes wrong in the reading or writing
     * @throws com.compomics.pride_asa_pipeline.model.MGFExtractionException
     */
    public File writeToMGFFolder(File outputFolder) throws IOException, MGFExtractionException {
        final long timeout = 30000;
        File target = new File(outputFolder, "spectra");
        for (File aPeakFile : parserCache.getPeakFiles(experimentIdentifier)) {
            File mgf = new File(target, aPeakFile.getName() + ".mgf");
            if (!mgf.exists()) {
                mgf.getParentFile().mkdirs();
                mgf.createNewFile();
            }
            new MGFExtractor(aPeakFile).extractMGF(mgf, timeout);
        }
        return target;
    }

}
