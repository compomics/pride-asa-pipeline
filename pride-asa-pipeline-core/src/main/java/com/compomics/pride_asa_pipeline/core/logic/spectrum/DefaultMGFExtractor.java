/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.logic.spectrum;

import com.compomics.pride_asa_pipeline.core.exceptions.MGFExtractionException;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.experiment.massspectrometry.Precursor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
            spectrum = jMzReader.getSpectrumById(spectrumId);
        } catch (JMzReaderException ex) {
            LOGGER.error(ex);
        }
        return spectrum;
    }

    public File extractMGF(File outputFile) throws MGFExtractionException, IOException {
        if (jMzReader.getMsNIndexes(2).isEmpty() || jMzReader.getSpectraIds().isEmpty()) {
            throw new MGFExtractionException("There are no MS2 spectra in this project...");
        } else {
            LOGGER.info(jMzReader.getClass() + " found spectra...Extracting can commence");
            if (!outputFile.exists()) {
                outputFile.createNewFile();
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
                double precursorMZ;
                double precursorCharge = 0;
                double precursorIntensity = 0;
                HashMap<Double, Double> peakList;

                Iterator<Spectrum> spectrumIterator = jMzReader.getSpectrumIterator();

                if (!spectrumIterator.hasNext()) {
                    throw new MGFExtractionException(jMzReader.getClass() + " could not read spectra from the inputfile");
                } else {
                    LOGGER.info(jMzReader.getClass() + " filled spectrum iterator");
                }

                while (spectrumIterator.hasNext()) {
                    try {
                        try {
                            Spectrum spectrum;
                            spectrum = spectrumIterator.next();
                            if (spectrum.getMsLevel() == 2) {
                                //FIGURE OUT PEAKLIST           
                                try {
                                    peakList = (HashMap<Double, Double>) spectrum.getPeakList();
                                } catch (NullPointerException e) {
                                    throw new NullPointerException("No peaklist found for spectrum " + spectrum.getId());
                                }
//FIGURE OUT MZ
                                try {
                                    precursorMZ = spectrum.getPrecursorMZ();
                                } catch (NullPointerException e) {
                                    //try to find it in the spectrum description?
                                    precursorMZ = scavengePrecursorMZ(spectrum);
                                    if (precursorMZ == 0.0) {
                                        throw new NullPointerException("No precursor m/z for spectrum " + spectrum.getId());
                                    }
                                }
// FIGURE OUT INTENSITY
                                try {
                                    precursorIntensity = spectrum.getPrecursorIntensity();
                                } catch (NullPointerException e) {
                                    LOGGER.warn("No precursor intensity for spectrum " + spectrum.getId());
                                }
// FIGURE OUT CHARGES
                                try {
                                    precursorCharge = spectrum.getPrecursorCharge();
                                } catch (NullPointerException e) {
                                    precursorCharge = scavengeCharge(spectrum);
                                    if (precursorCharge == 0.0) {
                                        LOGGER.warn("No precursor charge for spectrum " + spectrum.getId());
                                    }
                                }
                                ArrayList<Charge> possibleCharges = new ArrayList<>();

// FIGURE OUT RT
                                //            Precursor precursor = new Precursor(0.0, precursorMZ, precursorIntensity, possibleCharges);
                                Precursor precursor = new Precursor(precursorMZ, precursorIntensity, possibleCharges, 0.0, 0.0);
                                MSnSpectrum msNspectrum = new MSnSpectrum(2, precursor, spectrum.getId(), outputFile.getAbsolutePath());

                                for (double anMz : peakList.keySet()) {
                                    msNspectrum.addPeak(new Peak(anMz, peakList.get(anMz)));
                                }
                                msNspectrum.writeMgf(writer);
                                //  mgfEntries.add(new MGFentry(spectrumId, precursorMZ, precursorIntensity, (int) precursorCharge, peakList));
                            }
                        } catch (NullPointerException | IndexOutOfBoundsException e) {
                            throw new MGFExtractionException(jMzReader.getClass() + " could not read precursors from the inputfile");
                        }
                    } catch (IOException e) {
                        LOGGER.error(e);
                    }
                }
            } catch (IOException e) {
                LOGGER.error(e);
            }
            return outputFile;
        }
    }

    public double scavengePrecursorMZ(Spectrum spectrum) {
        double scavengedPrecursorMz = 0.0;
        try {
            scavengedPrecursorMz = spectrum.getPrecursorMZ();
        } catch (Exception e) {
            for (CvParam aParameter : spectrum.getAdditional().getCvParams()) {
                if (aParameter.getAccession().equalsIgnoreCase("MS:1000744")
                        || aParameter.getAccession().equalsIgnoreCase("PSI:1000040")) {
                    scavengedPrecursorMz = Double.parseDouble(aParameter.getValue());
                }
            }
        }
        return scavengedPrecursorMz;
    }

    public double scavengeCharge(Spectrum spectrum) {
        double scavengedCharge = 0.0;
        try {
            scavengedCharge = spectrum.getPrecursorCharge();
        } catch (Exception e) {
            for (CvParam aParameter : spectrum.getAdditional().getCvParams()) {
                if (aParameter.getAccession().equalsIgnoreCase("MS:1000041")) {
                    scavengedCharge = Double.parseDouble(aParameter.getValue());
                }
            }
        }
        return scavengedCharge;
    }   
    
}
