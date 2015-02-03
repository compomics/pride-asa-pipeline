/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.logic.spectrum;

import com.compomics.pride_asa_pipeline.core.exceptions.MGFExtractionException;
import com.compomics.util.experiment.massspectrometry.Peak;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.tools.jmzreader.JMzReader;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.jmzreader.model.Param;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.jmzreader.model.impl.CvParam;
import uk.ac.ebi.pride.tools.jmzreader.model.impl.UserParam;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;

/**
 *
 * @author Kenneth Verheggen
 */
public class DefaultMGFExtractor {

    JMzReader jMzReader;
    private final static Logger LOGGER = Logger.getLogger(DefaultMGFExtractor.class);
    public File inputFile;
    private Integer maxPrecursorCharge = 5;
    private Integer minPrecursorCharge = 1;

    public DefaultMGFExtractor() {

    }

    public DefaultMGFExtractor(File inputFile) throws ClassNotFoundException, MzXMLParsingException, JMzReaderException {
        init(inputFile);
    }

    public JMzReader getJMzReader() {
        return jMzReader;
    }

//the prideFTP should be checked before this...
    private void init(File inputFile) throws ClassNotFoundException, MzXMLParsingException, JMzReaderException {
        this.inputFile = inputFile;
        LOGGER.info("Getting parser for " + inputFile.getName());
        jMzReader = SpectrumParserFactory.getJMzReader(inputFile);
    }

    public List<Map<String, Object>> getSpectraMetadata() {
        List<Map<String, Object>> spectraMetaDataMap = new ArrayList<>();
        for (String spectrumId : jMzReader.getSpectraIds()) {
            try {
                Map<String, Object> spectrumMetaDataList = new HashMap<>();
                Spectrum spectrum = jMzReader.getSpectrumById(spectrumId);
                spectrumMetaDataList.put("msLv", spectrum.getMsLevel());
                for (CvParam aParameter : spectrum.getAdditional().getCvParams()) {
                    spectrumMetaDataList.put(aParameter.getName(), aParameter.getValue());
                }
                for (Param aParameter : spectrum.getAdditional().getParams()) {
                    spectrumMetaDataList.put(aParameter.getName(), aParameter.getValue());
                }
                for (UserParam aParameter : spectrum.getAdditional().getUserParams()) {
                    spectrumMetaDataList.put(aParameter.getName(), aParameter.getValue());
                }
            } catch (JMzReaderException ex) {
                LOGGER.error(ex);
            }

        }
        return spectraMetaDataMap;
    }

    public List<String> getSpectrumIds() {
        return jMzReader.getSpectraIds();
    }

    public List<Peak> getSpectrumPeaksBySpectrumId(String spectrumId) {
        List<Peak> myPeakList = new ArrayList<>();
        try {
            Spectrum spectrum = jMzReader.getSpectrumById(spectrumId);
            Map<Double, Double> peakList = spectrum.getPeakList();
            for (Double anMZ : peakList.keySet()) {
                myPeakList.add(new Peak(anMZ, peakList.get(anMZ)));
            }
        } catch (JMzReaderException ex) {
            LOGGER.error(ex);
        }
        return myPeakList;
    }

    public HashMap<Double, Double> getSpectrumPeakMapBySpectrumId(String spectrumId) {
        HashMap<Double, Double> peakMap = null;
        try {
            peakMap = (HashMap<Double, Double>) jMzReader.getSpectrumById(spectrumId).getPeakList();
        } catch (JMzReaderException ex) {
            LOGGER.error(ex);
        }
        return peakMap;
    }

    public Spectrum getSpectrumBySpectrumId(String spectrumId) {
        Spectrum spectrum = null;
        try {
            spectrum = jMzReader.getSpectrumById(spectrumId.substring(spectrumId.lastIndexOf("=") + 1));
        } catch (JMzReaderException ex) {
            LOGGER.error(ex);
        }
        return spectrum;
    }

    /**
     * Convert the peakfile to mgf.
     *
     * @param outputFile
     * @return
     * @throws
     * com.compomics.pride_asa_pipeline.core.exceptions.MGFExtractionException
     * @throws uk.ac.ebi.pride.tools.jmzreader.JMzReaderException
     */
    public File extractMGF(File outputFile) throws MGFExtractionException, JMzReaderException {
        try {
            try (FileWriter w = new FileWriter(outputFile); BufferedWriter bw = new BufferedWriter(w)) {
                List<String> spectra = jMzReader.getSpectraIds();
                int spectraCount = spectra.size();
                if (spectraCount == 0) {
                    bw.close();
                    w.close();
                    throw new MGFExtractionException("No spectra present");
                }
                int validSpectrumCount = 0;
                for (String spectrumId : spectra) {
                    Spectrum spectrum = jMzReader.getSpectrumById(spectrumId);
                    boolean valid = asMgf(spectrum, bw);
                    if (valid) {
                        validSpectrumCount++;
                    }
                }
                if (validSpectrumCount == 0) {
                    throw new MGFExtractionException("The file (" + inputFile.getName() + ") contains no valid spectra!");
                }
            }
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
        return outputFile;
    }

    /**
     * Writes the given spectrum to the buffered writer.
     *
     * @param spectrum
     * @param bw
     * @return true of the spectrum could be converted to mgf
     * @throws IOException
     */
    public boolean asMgf(Spectrum spectrum, BufferedWriter bw) throws IOException {
        boolean valid = true;
        int msLevel = spectrum.getMsLevel();
        // ignore ms levels other than 2
        if (msLevel == 2) {
            // add precursor details
            if (spectrum.getPrecursorMZ() > 0) {
                bw.write("BEGIN IONS" + System.getProperty("line.separator"));
                bw.write("TITLE=" + spectrum.getId() + System.getProperty("line.separator"));

                Double precursorMz = spectrum.getPrecursorMZ();
                Double precursorIntensity = spectrum.getPrecursorIntensity();
                Integer precursorCharge = spectrum.getPrecursorCharge();

                //TODO SEE WHERE TO GET RT
                Double precursorRt = null;

                if (precursorMz != null) {
                    bw.write("PEPMASS=" + precursorMz);
                } else {
                    valid = false; // @TODO: cancel conversion??
                }

                if (precursorIntensity != null) {
                    bw.write("\t" + precursorIntensity);
                }

                bw.write(System.getProperty("line.separator"));

                if (precursorRt != null) {
                    bw.write("RTINSECONDS=" + precursorRt + System.getProperty("line.separator")); // @TODO: improve the retention time mapping, e.g., support rt windows
                }

                if (precursorCharge != null) {
                    bw.write("CHARGE=" + precursorCharge + System.getProperty("line.separator"));

                    if (maxPrecursorCharge == null || precursorCharge > maxPrecursorCharge) {
                        maxPrecursorCharge = precursorCharge;
                    }
                    if (minPrecursorCharge == null || precursorCharge < minPrecursorCharge) {
                        minPrecursorCharge = precursorCharge;
                    }
                } else {
                    //valid = false; // @TODO: can we use spectra without precursor charge??
                }

                // process all peaks by iterating over the m/z values
                for (Map.Entry<Double, Double> mzEntry : spectrum.getPeakList().entrySet()) {
                    bw.write(mzEntry.getKey().toString());
                    bw.write(" ");
                    bw.write(mzEntry.getValue().toString());
                    bw.write(System.getProperty("line.separator"));
                }

                bw.write("END IONS" + System.getProperty("line.separator") + System.getProperty("line.separator"));

            } else {
                valid = false;
            }
        } else {
            valid = false;
        }

        return valid;
    }

}
