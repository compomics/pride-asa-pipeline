package com.compomics.pride_asa_pipeline.core.repository.impl;

import com.compomics.pride_asa_pipeline.core.logic.spectrum.DefaultMGFExtractor;
import com.compomics.pride_asa_pipeline.model.Peak;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.io.FTPDownloader;
import com.compomics.util.io.compression.ZipUtils;
import com.compomics.util.pride.PrideWebService;
import com.compomics.util.pride.prideobjects.webservice.file.FileDetail;
import com.compomics.util.pride.prideobjects.webservice.file.FileDetailList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;

/**
 *
 * @author Kenneth Verheggen
 */
public class WSSpectrumRepository extends JdbcSpectrumRepository {

    private static final Logger LOGGER = Logger.getLogger(WSSpectrumRepository.class);
    /**
     * The timeout value for the extraction of a spectrum
     */
    private static final long timeout = 30000;
    /**
     * The output folder for the peak extraction (required)
     */
    private File peakFileOutputFolder = new File(System.getProperty("user.home" + "/compomics/.compomics/pride-asap/peakfiles"));
    /**
     * The merged peak file (in case there are multiple assay peak files)
     */
    private static File mergedPeakFile;
    /**
     * The mgfExtractor for the merged peak file (to query)
     */
    private DefaultMGFExtractor mgfExtractor;

    /**
     * Constructs a new WSSpectrumRepository
     *
     * @param experimentAccession the assay identifier
     * @throws IOException in case the mgf can not be read or written
     * @throws ClassNotFoundException in case no suitable parser was available
     * @throws MzXMLParsingException in case the parsing of an mzXML did not
     * occur correctly
     * @throws JMzReaderException in case the Jmzreader threw an exception
     */
    public WSSpectrumRepository(String experimentAccession) throws IOException, ClassNotFoundException, MzXMLParsingException, JMzReaderException {
        this.peakFileOutputFolder = new File(peakFileOutputFolder, experimentAccession);
        initPeakFile(experimentAccession);
    }

    /**
     * Constructs a new WSSpectrumRepository
     *
     * @param experimentAccession the assay identifier
     * @param peakFileOutputFolder the output folder for the extracted peak
     * files
     * @throws IOException in case the mgf can not be read or written
     * @throws ClassNotFoundException in case no suitable parser was available
     * @throws MzXMLParsingException in case the parsing of an mzXML did not
     * occur correctly
     * @throws JMzReaderException in case the Jmzreader threw an exception
     */
    public WSSpectrumRepository(File peakFileOutputFolder, String experimentAccession) throws IOException, ClassNotFoundException, MzXMLParsingException, JMzReaderException {
        this.peakFileOutputFolder = new File(peakFileOutputFolder, experimentAccession);
        initPeakFile(experimentAccession);
    }

    private void initPeakFile(String experimentAccession) throws IOException, ClassNotFoundException, MzXMLParsingException, JMzReaderException {
        FileDetailList assayFileDetails = PrideWebService.getAssayFileDetails(experimentAccession);
        mergedPeakFile = new File(peakFileOutputFolder, experimentAccession + ".asap.mgf");
        for (FileDetail aFileDetail : assayFileDetails.getList()) {
            if (aFileDetail.getFileType().equalsIgnoreCase("PEAK")) {
                try {
                    //download the file
                    File downloadFile = downloadFile(aFileDetail);
                    //convert the file to an MGF
                    DefaultMGFExtractor extractor = new DefaultMGFExtractor(downloadFile);
                    File tempOutput = extractor.extractMGF(new File(downloadFile.getAbsolutePath() + ".temp.mgf"), timeout);
                    //merge the tempOutput into the mergedPeakFile
                    mergeMGFFiles(mergedPeakFile, tempOutput);
                    tempOutput.delete();
                    downloadFile.delete();
                } catch (Exception ex) {
                    LOGGER.error(ex);
                }
            }
        }
        mgfExtractor = new DefaultMGFExtractor(mergedPeakFile);
    }

    @Override
    public double[] getMzValuesBySpectrumId(String spectrumId) {
        LOGGER.debug("Loading mz values for spectrum " + spectrumId);
        Spectrum spectrumBySpectrumId = mgfExtractor.getSpectrumBySpectrumId(spectrumId);
        Set<Double> keys = spectrumBySpectrumId.getPeakList().keySet();
        double[] mzValues = new double[keys.size()];
        Iterator<Double> mzIterator = keys.iterator();
        int i = 0;
        while (mzIterator.hasNext()) {
            mzValues[i] = mzIterator.next();
            i++;
        }
        return mzValues;
    }

    @Override
    public double[] getIntensitiesBySpectrumId(String spectrumId) {
        LOGGER.debug("Loading intensities for spectrum " + spectrumId);
        Spectrum spectrumBySpectrumId = mgfExtractor.getSpectrumBySpectrumId(spectrumId);
        Collection<Double> values = spectrumBySpectrumId.getPeakList().values();
        double[] intensityValues = new double[values.size()];
        Iterator<Double> mzIterator = values.iterator();
        int i = 0;
        while (mzIterator.hasNext()) {
            intensityValues[i] = mzIterator.next();
            i++;
        }
        return intensityValues;
    }

    @Override
    public Map<String, List<Peak>> getPeakMapsBySpectrumIdList(List<String> spectrumIds) {
        LOGGER.debug("Loading peaks for spectrum list with size " + spectrumIds.size());
        Map<String, List<Peak>> peakMap = new HashMap<>();
        for (String spectrumID : mgfExtractor.getSpectrumIds()) {
            peakMap.put(spectrumID, mgfExtractor.getSpectrumPeaksBySpectrumId(spectrumID));
        }
        return peakMap;
    }

    private File downloadFile(FileDetail detail) throws MalformedURLException, Exception {
        URL url = new URL(detail.getDownloadLink());
        FTPDownloader downloader = new FTPDownloader(url.getHost());
        File downloadFile = new File(peakFileOutputFolder, detail.getFileName());
        LOGGER.debug("Downloading : " + url.getPath());
        downloader.downloadFile(url.getPath(), downloadFile);
        if (downloadFile.getAbsolutePath().endsWith(".gz")) {
            File temp = new File(peakFileOutputFolder, downloadFile.getName().replace(".gz", ""));
            gunzip(downloadFile, temp);
            downloadFile.delete();
            downloadFile = temp;
        }
        return downloadFile;
    }

    private void gunzip(File gzipFile, File outputFile) {
        byte[] buffer = new byte[1024];
        try (FileOutputStream out = new FileOutputStream(outputFile); GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(gzipFile))) {
            int len;
            while ((len = gzis.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
    }

    private void mergeMGFFiles(File motherFile, File daughterFile) throws IOException {
        String mgfAsString = FileUtils.readFileToString(daughterFile);
        FileWriter writer = new FileWriter(motherFile, true);
        writer.append(mgfAsString);
    }

    /**
     * returns the master peak file for the assay
     *
     * @return the merged peak file
     */
    public static File getHandledMGF() {
        return mergedPeakFile;
    }

    /**
     * returns the master peak file for the assay
     *
     * @param destinationFolder the folder where the MGF has to be stored
     *
     * @return the moved peak file
     */
    public static File moveHandledMGF(File destinationFolder) throws IOException {
        File targetFile = new File(destinationFolder, mergedPeakFile.getName());
        if (mergedPeakFile.renameTo(targetFile)) {
            LOGGER.debug("File is moved successful!");
        } else {
            throw new IOException("Could not move file !");
        }
        return targetFile;
    }

    /**
     * returns the master peak file for the assay
     *
     * @param destinationFolder the folder where the MGF has to be stored
     * @return the moved and zipped peak file
     */
    public static File moveAndZipHandledMGF(File destinationFolder) throws IOException {
        File targetFile = new File(destinationFolder, mergedPeakFile.getName() + ".zip");
        ZipUtils.zip(mergedPeakFile, targetFile, new WaitingHandlerCLIImpl(), mergedPeakFile.length());
        return targetFile;
    }

}
