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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
            //check that the spectrum is MS2...
            if (spectrum.getMsLevel() == 2) {
                Map<Double, Double> peakList = spectrum.getPeakList();
                for (Map.Entry<Double, Double> aPeakPair : peakList.entrySet()) {
                    myPeakList.add(new Peak(aPeakPair.getKey(), aPeakPair.getValue()));
                }
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
        } catch (NumberFormatException | JMzReaderException ex) {
            LOGGER.error(ex);
        }
        return spectrum;
    }

    public File extractMGF(File outputFile, long timeout) throws MGFExtractionException, JMzReaderException {
        OutputStream reportStream = System.out;
        return extractMGF(outputFile, reportStream, timeout);
    }

    public File extractMGF(File outputFile) throws MGFExtractionException, JMzReaderException {
        return extractMGF(outputFile, 30000);
    }

    /**
     * Convert the peakfile to mgf.
     *
     * @param outputFile
     * @param reportStream
     * @param timeout
     * @return
     * @throws
     * com.compomics.pride_asa_pipeline.core.exceptions.MGFExtractionException
     * @throws uk.ac.ebi.pride.tools.jmzreader.JMzReaderException
     */
    public File extractMGF(File outputFile, OutputStream reportStream, long timeout) throws MGFExtractionException, JMzReaderException {
        try (FileWriter w = new FileWriter(outputFile);
                BufferedWriter bw = new BufferedWriter(w);
                OutputStreamWriter rw = new OutputStreamWriter(reportStream);) {
            int spectraCount = jMzReader.getSpectraCount();
            if (spectraCount == 0) {
                bw.close();
                w.close();
                rw.append("No spectra discovered").flush();
            } else {
                rw.append("Total amount of spectra \t " + spectraCount + System.lineSeparator()).flush();
            }
            int validSpectrumCount = 0;

            System.out.println("Processing Spectra");
            List<String> spectrumIds = jMzReader.getSpectraIds();
            for (String aSpectrumId : spectrumIds) {
                //this part gets stuck sometimes, no idea why...
                rw.append(aSpectrumId + "\t\t").flush();
                try {
                    Spectrum spectrum = controlledReadNextSpectrum(aSpectrumId, jMzReader, timeout);
                    if (spectrum != null) {
                        asMgf(spectrum, bw);
                        rw.append("VALIDATED" + System.lineSeparator());
                        validSpectrumCount++;

                    } else {
                        rw.append("\tNOT FOUND" + System.lineSeparator()).flush();
                    }
                } catch (MGFExtractionException | TimeoutException e) {
                    LOGGER.error(e);
                    rw.append("\t" + e.getMessage() + System.lineSeparator()).flush();
                }
            }
            rw.append("Total usable \t" + validSpectrumCount + System.lineSeparator()).flush();
        } catch (Exception ex) {
            LOGGER.error(ex);
            ex.printStackTrace();
        }
        System.out.println("Done");
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
    public void asMgf(Spectrum spectrum, BufferedWriter bw) throws MGFExtractionException, IOException {

        int msLevel = spectrum.getMsLevel();
        Double precursorMz = spectrum.getPrecursorMZ();
        Double precursorIntensity = spectrum.getPrecursorIntensity();
        Integer precursorCharge = spectrum.getPrecursorCharge();
        Double precursorRt = 0.0;
        for (CvParam cvParam : spectrum.getAdditional().getCvParams()) {
            if (cvParam.getAccession().equalsIgnoreCase("MS:1000894")|cvParam.getAccession().equalsIgnoreCase("PSI:1000039")) {
                precursorRt = Double.parseDouble(cvParam.getValue());
            }
        }

// ignore ms levels other than 2
        if (msLevel == 2) {
            // add precursor details
            if (spectrum.getPrecursorMZ() > 0) {
                bw.write("BEGIN IONS" + System.getProperty("line.separator"));
                bw.write("TITLE=" + spectrum.getId() + System.getProperty("line.separator"));
                bw.write("PEPMASS=" + precursorMz);
  
                if (precursorIntensity != null) {
                    bw.write("\t" + precursorIntensity);
                }

                bw.write(System.getProperty("line.separator"));

                if (precursorRt > 0.0) {
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
                throw new MGFExtractionException(spectrum.getId() + "\t" + "No precursor mz");
            }
        } else {
            //TODO not being MS2 is not really an exception...
            throw new MGFExtractionException(spectrum.getId() + "\t" + "Not ms2 :" + msLevel);
        }
    }

    private Spectrum controlledReadNextSpectrum(String spectrumID, JMzReader jmzReader, long timeout) throws TimeoutException {
        ExecutorService service = Executors.newFixedThreadPool(1, new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                return thread;
            }
        });
        SpectrumRetriever retriever = new SpectrumRetriever(spectrumID, jmzReader);
        Future<Spectrum> futureResult = service.submit(retriever);
        Spectrum result = null;
        try {
            result = futureResult.get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException ex) {
            boolean cancel = futureResult.cancel(true);
            retriever.interrupt();
            if (!cancel) {
                LOGGER.error("Could not clear the thread for " + spectrumID);
                retriever = null;
                futureResult = null;
            }
            //for safety kill the service pool?
            service.shutdownNow();
            //notify the program that this particular spectrum timed out...
            //can you completely destroy a future?

            throw new TimeoutException("Timed out after " + timeout + " ms (" + System.lineSeparator() + ex + System.lineSeparator() + ")");
        }
        service.shutdown();
        return result;
    }

    public void clear() {
        jMzReader = null;
    }

    private static final class SpectrumRetriever implements Callable<Spectrum> {

        private final JMzReader jmzReader;
        private final String spectrumId;

        private SpectrumRetriever(String spectrumId, JMzReader jmzReader) {
            this.jmzReader = jmzReader;
            this.spectrumId = spectrumId;
        }

        @Override
        public Spectrum call() throws Exception {
            Thread.currentThread().setName(spectrumId);
            Spectrum spectrum = jmzReader.getSpectrumById(spectrumId);
            return spectrum;
        }

        public void interrupt() {
            Thread[] a = new Thread[Thread.activeCount()];
            int n = Thread.enumerate(a);
            for (int i = 0; i < n; i++) {
                if (a[i].getName().equals(spectrumId)) {
                    a[i].interrupt();
                    break;
                }
            }
        }

    }

}
