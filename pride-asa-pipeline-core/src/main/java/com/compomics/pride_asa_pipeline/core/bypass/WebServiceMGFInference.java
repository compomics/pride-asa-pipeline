/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.bypass;

import com.compomics.pride_asa_pipeline.core.repository.impl.combo.FileExperimentModificationRepository;
import com.compomics.util.io.FTPDownloader;
import com.compomics.util.pride.PrideWebService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.archive.dataprovider.file.ProjectFileType;
import uk.ac.ebi.pride.archive.web.service.model.file.FileDetail;
import uk.ac.ebi.pride.archive.web.service.model.file.FileDetailList;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class WebServiceMGFInference {

    private String assayIdentifier;

    private File tempFolder = new File(System.getProperty("user.home") + "/pride_bypass");
    private static final Logger LOGGER = Logger.getLogger(WebServiceMGFInference.class);

    public WebServiceMGFInference(String assayIdentifier) {
        this.assayIdentifier = assayIdentifier;
    }

    public List<File> getPrideMGFFile(File outputFolder) throws Exception {
        tempFolder=outputFolder;
        //merge all peak files
        List<File> peakFiles = getPrideMGF();
        for (File aFile : peakFiles) {
            System.out.println(aFile.getAbsolutePath() +" _ "+ aFile.exists());
        }
        return peakFiles;
    }

    private File downloadFile(FileDetail detail) throws Exception {
        if (!tempFolder.exists()) {
            tempFolder.mkdirs();
        }
        URL url = detail.getDownloadLink();
        FTPDownloader downloader = new FTPDownloader(url.getHost(), true);
        File downloadFile = new File(tempFolder, detail.getFileName());
        File temp = new File(tempFolder, downloadFile.getName());
        if (downloadFile.getName().endsWith(".gz")) {
            temp = new File(temp.getAbsolutePath().replace(".gz", ""));
        }
        LOGGER.info("Downloading : " + url.getPath());
        try {
            downloader.downloadFile(url.getPath(), downloadFile);
        } catch (org.apache.commons.net.io.CopyStreamException e) {
            e.getIOException().printStackTrace();
        }
        if (downloadFile.getAbsolutePath().endsWith(".gz")) {
            gunzip(downloadFile, temp);
            downloadFile.deleteOnExit();
        }
        downloader.disconnect();
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
    //        outputFile.deleteOnExit();
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
    }

    private List<File> getPrideMGF() throws Exception {
        ArrayList<File> assayFiles = new ArrayList<>();
        FileDetailList assayFileDetails = PrideWebService.getAssayFileDetails(assayIdentifier);
        //try to find existing result files online
        for (FileDetail assayFile : assayFileDetails.getList()) {
            LOGGER.info("Finding PRIDE - GENERATED filesources");
            if (assayFile.getFileName().toLowerCase().contains(".pride.mgf")) {
                if (assayFile.getFileType().equals(ProjectFileType.PEAK)) {
                    assayFiles.add(downloadFile(assayFile));
                }
            }
        }
        return assayFiles;
    }

}
