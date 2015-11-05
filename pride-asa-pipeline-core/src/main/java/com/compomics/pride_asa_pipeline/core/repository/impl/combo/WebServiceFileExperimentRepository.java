package com.compomics.pride_asa_pipeline.core.repository.impl.combo;

import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileExperimentRepository;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.util.io.FTPDownloader;
import com.compomics.util.pride.PrideWebService;
import com.compomics.util.pride.prideobjects.webservice.file.FileType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.archive.web.service.model.file.FileDetail;
import uk.ac.ebi.pride.archive.web.service.model.file.FileDetailList;

/**
 *
 * @author Kenneth Verheggen
 */
public class WebServiceFileExperimentRepository extends FileExperimentRepository {

    /**
     * A logger instance
     */
    private static final Logger LOGGER = Logger.getLogger(FileExperimentRepository.class);
    /**
     * A temporary folder to store downloaded identification files
     */
    private File tempFolder;

    /**
     * Creates a new File experiment repository
     */
    public WebServiceFileExperimentRepository() {
        this.tempFolder = new File(System.getProperty("user.home") + "/compomics/temp");
    }

    /**
     * Creates a new File experiment repository
     */
    public WebServiceFileExperimentRepository(File tempFolder) {
        this.tempFolder = tempFolder;
    }

    @Override
    public List<Identification> loadExperimentIdentifications(String experimentAccession) {
        List<Identification> identifications = new ArrayList<>();
        try {
            File identificationFileForAssay = getIdentificationFile(experimentAccession);
            if (identificationFileForAssay.getName().toLowerCase().endsWith(".xml")) {
                addPrideXMLFile(experimentAccession,identificationFileForAssay);
            } else {
                addMzID(experimentAccession,identificationFileForAssay, getPeakFiles(experimentAccession));
            }
            identifications = super.loadExperimentIdentifications(identificationFileForAssay.getName());
        } catch (Exception ex) {
            LOGGER.error(ex);
        }
        return identifications;
    }

    private File downloadFile(FileDetail detail) throws Exception {
        URL url = detail.getDownloadLink();
        FTPDownloader downloader = new FTPDownloader(url.getHost());
        File downloadFile = new File(tempFolder, detail.getFileName());
        File temp = null;
        if (downloadFile.getName().endsWith(".gz")) {
            temp = new File(tempFolder, downloadFile.getName().replace(".gz", ""));
        }
        if (temp != null && !temp.exists()) {
            LOGGER.debug("Downloading : " + url.getPath());
            downloader.downloadFile(url.getPath(), downloadFile);
            if (downloadFile.getAbsolutePath().endsWith(".gz")) {
                gunzip(downloadFile, temp);
                downloadFile.delete();
            }
        }
        return temp;
    }

    private void gunzip(File gzipFile, File outputFile) {
        byte[] buffer = new byte[1024];
        try (FileOutputStream out = new FileOutputStream(outputFile); GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(gzipFile))) {
            int len;
            while ((len = gzis.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            gzis.close();
            outputFile.deleteOnExit();
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
    }

    private File getIdentificationFile(String experimentAccession) throws Exception {
        File identificationsFile = null;
        FileDetailList assayFileDetails = PrideWebService.getAssayFileDetails(experimentAccession);
        //try to find existing result files online
        for (FileDetail assayFile : assayFileDetails.getList()) {
            if (assayFile.getFileType().equals(FileType.RESULT.toString())) {
                identificationsFile = downloadFile(assayFile);
            }
        }
        return identificationsFile;
    }

    private List<File> getPeakFiles(String experimentAccession) throws Exception {
        ArrayList<File> peakFiles = new ArrayList<>();
        FileDetailList assayFileDetails = PrideWebService.getAssayFileDetails(experimentAccession);
        //try to find existing result files online
        for (FileDetail assayFile : assayFileDetails.getList()) {
            if (assayFile.getFileType().equals(FileType.PEAK.toString())) {
                peakFiles.add(downloadFile(assayFile));
            }
        }
        return peakFiles;
    }

}
