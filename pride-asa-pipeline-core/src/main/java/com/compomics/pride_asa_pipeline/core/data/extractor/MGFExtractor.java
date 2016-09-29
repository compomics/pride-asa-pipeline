package com.compomics.pride_asa_pipeline.core.data.extractor;

import com.compomics.pride_asa_pipeline.model.MGFExtractionException;
import com.compomics.pride_asa_pipeline.core.util.report.impl.TotalReportGenerator;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.tools.dta_parser.DtaFile;
import uk.ac.ebi.pride.tools.jmzreader.JMzReader;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.jmzreader.model.impl.CvParam;
import uk.ac.ebi.pride.tools.mgf_parser.MgfFile;
import uk.ac.ebi.pride.tools.ms2_parser.Ms2File;
import uk.ac.ebi.pride.tools.mzdata_parser.MzDataFile;
import uk.ac.ebi.pride.tools.mzml_wrapper.MzMlWrapper;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;
import uk.ac.ebi.pride.tools.pkl_parser.PklFile;
import uk.ac.ebi.pride.tools.pride_wrapper.PRIDEXmlWrapper;

/**
 *
 * @author Kenneth Verheggen
 */
public class MGFExtractor {

    JMzReader jMzReader;
    private final static Logger LOGGER = Logger.getLogger(MGFExtractor.class);
    public File inputFile;
    private Integer maxPrecursorCharge = 5;
    private Integer minPrecursorCharge = 1;

    public MGFExtractor() {

    }

    public MGFExtractor(File inputFile) throws MGFExtractionException {
        init(inputFile);
    }

    public JMzReader getJMzReader() {
        return jMzReader;
    }

//the prideFTP should be checked before this...
    private void init(File inputFile) throws MGFExtractionException {
        this.inputFile = inputFile;
        LOGGER.info("Getting parser for " + inputFile.getName());
        try {
            jMzReader = getJMzReader(inputFile);
        } catch (Exception ex) {
            LOGGER.error("Could not initiate spectrum extraction !");
            throw new MGFExtractionException(ex.getMessage());
        }
    }

    /**
     * Convert the peakfile to mgf without reporting
     *
     * @param outputFile the output MGF file
     * @return the extracted MGF file
     * @throws MGFExtractionException
     */
    public File extractMGF(File outputFile) throws MGFExtractionException {
        return extractMGF(outputFile, 30000);
    }

    /**
     * Convert the peakfile to mgf without reporting
     *
     * @param outputFile the output MGF file
     * @param timeout the timeout for spectrum retrieval (it sometimes blocks)
     * @return the extracted MGF file
     * @throws MGFExtractionException
     */
    public File extractMGF(File outputFile, long timeout) throws MGFExtractionException {
        OutputStream reportStream = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                //ignore the message
            }
        };
        return extractMGF(outputFile, reportStream, timeout);
    }

    /**
     * Convert the peakfile to mgf without reporting
     *
     * @param outputFile the output MGF file
     * @param reportStream the stream to report to (for example System.out)
     * @param timeout the timeout for spectrum retrieval (it sometimes blocks)
     * @return the extracted MGF file
     * @throws MGFExtractionException
     * @throws JMzReaderException
     */
    public File extractMGF(File outputFile, OutputStream reportStream, long timeout) throws MGFExtractionException {
        LOGGER.debug("Extraction of mgf started...");
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
                        TotalReportGenerator.addSpectrum(spectrum);
                    } else {
                        TotalReportGenerator.addFailedSpectrum(aSpectrumId, "Reported spectrum by reader not found in file");
                        rw.append("\tNOT FOUND" + System.lineSeparator()).flush();
                    }
                } catch (MGFExtractionException | TimeoutException e) {
                    LOGGER.error(e);
                    TotalReportGenerator.addFailedSpectrum(aSpectrumId, e.getMessage());
                    rw.append("\t" + e.getMessage() + System.lineSeparator()).flush();
                }
            }
            rw.append("Total usable \t" + validSpectrumCount + System.lineSeparator()).flush();
        } catch (Exception ex) {
            ex.printStackTrace();
            LOGGER.error(ex);
        }
        LOGGER.info("MGF extraction completed");
        return outputFile;
    }

    /**
     * Writes the given spectrum to the buffered writer and adds it to a total
     * report.
     *
     * @param spectrum the input spectrum object
     * @param bw the writer to export the mgf to
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
            if (cvParam.getAccession().equalsIgnoreCase("MS:1000894") | cvParam.getAccession().equalsIgnoreCase("PSI:1000039")) {
                precursorRt = Double.parseDouble(cvParam.getValue());
            }
        }
// ignore ms levels other than 2 ?
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
            futureResult.cancel(true);
            retriever.interrupt();
            //for safety kill the service pool?
            service.shutdownNow();
            //notify the program that this particular spectrum timed out...
            //can you completely destroy a future?
            if (ex instanceof TimeoutException | ex instanceof InterruptedException) {
                throw new TimeoutException("Timed out after " + timeout + " ms (" + System.lineSeparator() + ex + System.lineSeparator() + ")");
            }
        }
        service.shutdown();
        return result;
    }

    public void clear() {
        jMzReader = null;
    }

    private JMzReader getJMzReader(File inputFile) throws ClassNotFoundException, MzXMLParsingException, JMzReaderException {
        JMzReader parser = null;
        String extension = FilenameUtils.getExtension(inputFile.getAbsolutePath());
        switch (extension.toLowerCase()) {
            case "mzxml":
                LOGGER.debug("Detected mzXml file extension.");
                parser = new MzXMLFile(inputFile);
                break;
            case "mzml":
                LOGGER.debug("Detected mzml file extension.");
                parser = new MzMlWrapper(inputFile);
                break;
            case "dta":
                LOGGER.debug("Detected dta file extension.");
                parser = new DtaFile(inputFile);
                break;
            case "mgf":
                LOGGER.debug("Detected mgf file extension.");
                parser = new MgfFile(inputFile);
                break;
            case "ms2":
                LOGGER.debug("Detected ms2 file extension.");
                parser = new Ms2File(inputFile);
                break;
            case "mzData":
                LOGGER.debug("Detected mzData file extension.");
                parser = new MzDataFile(inputFile);
                break;
            case "xml":
                LOGGER.debug("Detected xml file extension.");
                parser = new PRIDEXmlWrapper(inputFile);
                break;
            case "pkl":
                LOGGER.debug("Detected pkl file extension.");
                parser = new PklFile(inputFile);
                break;
            default:
                throw new ClassNotFoundException("No suitable parser was found for the inputfile.");
        }
        return parser;
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
