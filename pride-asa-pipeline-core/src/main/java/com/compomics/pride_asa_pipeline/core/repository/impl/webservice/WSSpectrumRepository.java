package com.compomics.pride_asa_pipeline.core.repository.impl.webservice;

import com.compomics.pride_asa_pipeline.core.model.MGFExtractionException;
import com.compomics.pride_asa_pipeline.core.data.extractor.MGFExtractor;
import com.compomics.pride_asa_pipeline.core.repository.SpectrumRepository;
import com.compomics.pride_asa_pipeline.core.model.ParserCacheConnector;
import com.compomics.pride_asa_pipeline.model.Peak;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;
import uk.ac.ebi.pride.utilities.data.core.Spectrum;

/**
 *
 * @author Kenneth Verheggen
 */
public class WSSpectrumRepository extends ParserCacheConnector implements SpectrumRepository {

    /**
     * The identifier for the current repository (filename or assay accession)
     */
    private final String experimentIdentifier;

    public WSSpectrumRepository(String experimentIdentifier) {
        this.experimentIdentifier = experimentIdentifier;
    }

    public WSSpectrumRepository(File identificationsFile) {
        this.experimentIdentifier = identificationsFile.getName();
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
     * @param outputFile the output mgf file
     * @throws IOException when something goes wrong in the reading or writing
     */
    public void writeToMGF(File outputFile) throws IOException, MGFExtractionException {
        final long timeout = 30000;
        try (FileWriter writer = new FileWriter(outputFile, true)) {
            for (File aPeakFile : parserCache.getPeakFiles(experimentIdentifier)) {
                File tempOut = new File(aPeakFile.getParentFile(), aPeakFile.getName() + "asap.temp.mgf");
                tempOut.deleteOnExit();
                try {
                    new MGFExtractor(aPeakFile).extractMGF(tempOut, timeout);
                    String mgfAsString = FileUtils.readFileToString(tempOut);
                    writer.append(mgfAsString).append(System.lineSeparator());
                } catch (ClassNotFoundException | MzXMLParsingException | JMzReaderException ex) {
                    throw new MGFExtractionException(ex.getMessage());
                } finally {
                    tempOut.delete();
                }
            }
        }
    }

}
