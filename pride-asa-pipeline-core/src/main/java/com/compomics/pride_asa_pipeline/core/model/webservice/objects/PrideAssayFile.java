/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.model.webservice.objects;

import com.compomics.pride_asa_pipeline.core.model.webservice.fields.AssayFileField;
import com.compomics.pride_asa_pipeline.core.model.webservice.fields.PrideFileSource;
import com.compomics.pride_asa_pipeline.core.model.webservice.fields.PrideFileType;
import com.compomics.pride_asa_pipeline.core.util.IOUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class PrideAssayFile {

    private String projectAccession;
    private String assayAccession;
    private PrideFileType fileType;
    private PrideFileSource fileSource;
    private long fileSize;
    private String fileName;
    private URL downloadLink;
    private static final Logger LOGGER = Logger.getLogger(PrideAssayFile.class);

    public PrideAssayFile() {
    }

    public void setField(AssayFileField field, String parameter) throws MalformedURLException {
        if (parameter == null) {
            return;
        }
        switch (field) {
            case assayAccession:
                setAssayAccession(parameter);
                break;
            case downloadLink:
                setDownloadLink(new URL(parameter));
                break;
            case fileName:
                setFileName(parameter);
                break;
            case fileSize:
                setFileSize(Long.parseLong(parameter));
                break;
            case fileType:
                setFileType(PrideFileType.valueOf(parameter));
                break;
            case fileSource:
                setFileSource(PrideFileSource.valueOf(parameter));
                break;
            case projectAccession:
                setProjectAccession(parameter);
                break;
        }
    }

    public String getProjectAccession() {
        return projectAccession;
    }

    public void setProjectAccession(String projectAccession) {
        this.projectAccession = projectAccession;
    }

    public String getAssayAccession() {
        return assayAccession;
    }

    public void setAssayAccession(String assayAccession) {
        this.assayAccession = assayAccession;
    }

    public PrideFileType getFileType() {
        return fileType;
    }

    public void setFileType(PrideFileType fileType) {
        this.fileType = fileType;
    }

    public PrideFileSource getFileSource() {
        return fileSource;
    }

    public void setFileSource(PrideFileSource fileSource) {
        this.fileSource = fileSource;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public URL getDownloadLink() {
        return downloadLink;
    }

    public void setDownloadLink(URL downloadLink) {
        this.downloadLink = downloadLink;
    }

    public File downloadFile(File locationFolder) throws IOException {
        long startTime = System.currentTimeMillis();
        File outputFile = new File(locationFolder, fileName.substring(fileName.lastIndexOf("/") + 1));
        URLConnection conn = null;
        try {
            conn = getDownloadLink().openConnection();
            locationFolder.mkdirs();
            outputFile.createNewFile();
            try (InputStream inputStream = conn.getInputStream(); FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                long filesize = conn.getContentLength();
                LOGGER.info("Size of the file to download in mb is: " + Math.ceil((double) filesize / 1024 / 1024));
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    outputStream.flush();
                }
                outputStream.close();
                inputStream.close();
            } catch (IOException e) {
                LOGGER.error("Could not download " + getAssayAccession() + " to " + outputFile);
                LOGGER.error(e);
            }
            if (outputFile.exists() && outputFile.getName().toLowerCase().endsWith(".gz")) {
                LOGGER.info("Unpacking " + outputFile);
                File unzippedFile = new File(outputFile.getAbsolutePath().replace(".gz", ""));
                IOUtils.unzip(outputFile, unzippedFile);
                outputFile.delete();
                outputFile = unzippedFile;
                LOGGER.info("File downloaded and unpacked in " + (System.currentTimeMillis() - startTime) / 1000 + " seconds");
            } else {
                LOGGER.info("File downloaded in " + (System.currentTimeMillis() - startTime) / 1000 + " seconds");
            }
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            if (conn != null) {
                conn = null;
            }
        }
        return outputFile;
    }
}
