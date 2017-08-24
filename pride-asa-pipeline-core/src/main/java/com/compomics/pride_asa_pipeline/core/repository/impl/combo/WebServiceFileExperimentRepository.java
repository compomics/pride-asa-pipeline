package com.compomics.pride_asa_pipeline.core.repository.impl.combo;

import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileExperimentRepository;
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
 * @author Kenneth Verheggen
 */
public class WebServiceFileExperimentRepository extends FileExperimentModificationRepository {

    /**
     * A logger instance
     */
    private static final Logger LOGGER = Logger.getLogger(WebServiceFileExperimentRepository.class);
    /**
     * A temporary folder to store downloaded identification files
     */
    private File tempFolder;

    /**
     * List with allowed peakfiles (other ones do not have a parseer in ms-data-core api)
     */
    private List<String> extensions = new ArrayList<>(Arrays.asList("mzxml", "mzml","mgf","dta","pkl","ms2"));
    
    /**
     * Creates a new File experiment repository
     */
    public WebServiceFileExperimentRepository() {
        this.tempFolder = new File(System.getProperty("user.home") + "/compomics/temp");
    }

    /**
     * Creates a new File experiment repository
     *
     * @param tempFolder the temp folder location (for example when the input
     * files need to be saved as well)
     */
    public WebServiceFileExperimentRepository(File tempFolder) {
        this.tempFolder = tempFolder;
    }

    public WebServiceFileExperimentRepository(File tempFolder, int i) {
        this.tempFolder = tempFolder;
    }

    /**
     * Adds an assay to the fileservice
     *
     * @param assay the experiment accession
     * @throws Exception if a file cannot be correctly obtained
     */
    public void addAssay(String assay) throws Exception {
        List<File> identFiles = getAssayFiles(assay, ProjectFileType.RESULT);
        if (!identFiles.isEmpty()) {
            for (File identFile : identFiles) {
                if (!identFile.getName().toLowerCase().endsWith(".xml") & !identFile.getName().toLowerCase().endsWith(".xml.gz")) {
                    List<File> peakFiles = new ArrayList<>();
                    //@ToDo check why mzid is considered a peakfile here?
                    for (File aFile : getAssayFiles(assay, ProjectFileType.PEAK)) {
                        if (!aFile.getName().toLowerCase().endsWith(".mzid") && !aFile.getName().toLowerCase().endsWith(".mzid.zip") && !aFile.getName().toLowerCase().endsWith(".mzid.gz")) {
                            LOGGER.info("Added a peakFile : " + aFile);
                            peakFiles.add(aFile);
                        }
                    }
                    super.addMzID(assay, identFile, peakFiles);
                } else {
                    //@ToDo think of a better strategy for this, maybe include file name in assay identifier?
                    //here we should check if the parser already has a value for this project. Problem occurs when multiple prideXML files are in a single assay?
                    //default to the largest one for now ?
                    boolean addToParsers = true;
                    if (parserCache.containsParser(assay)) {
                        if (identFile.getName().toUpperCase().contains("PRIDE_EXP_COMPLETE_")) {
                            addToParsers = true;
                            LOGGER.info(identFile.getName() + " was found and will be used");
                        } else {
                            long currentSize = new File(parserCache.getLoadedFiles().get(assay)).length();
                            long newSize = identFile.length();
                            addToParsers = newSize > currentSize;
                            LOGGER.info(identFile.getName() + " is larger than the currently submitted file and will be used");
                        }
                    }
                    if (addToParsers) {
                        super.addPrideXMLFile(assay, identFile);
                    }
                }

            }
        } else {
            throw new FileNotFoundException("No identification file was found for assay number " + assay);
        }
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
            downloadFile.delete();
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
            outputFile.deleteOnExit();
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
    }

    private List<File> getAssayFiles(String experimentAccession, ProjectFileType type) throws Exception {
        ArrayList<File> assayFiles = new ArrayList<>();
        FileDetailList assayFileDetails = PrideWebService.getAssayFileDetails(experimentAccession);
        //try to find existing result files online
        for (FileDetail assayFile : assayFileDetails.getList()) {
            //skip raw files for now?
            if (!assayFile.getFileName().endsWith(".raw")) {
                if (assayFile.getFileType().equals(type)) {
                    assayFiles.add(downloadFile(assayFile));
                } else if (type.equals(ProjectFileType.PEAK)) {
                    //   if (assayFile.getFileName().contains(".dat")) {
                    //FILTER OUT UNSUPPORTED PEAKFILES
                    String extension = assayFile.getFileName().replace(".gz","").replace(".zip","");
                    extension = extension.substring(extension.lastIndexOf(".")+1).toLowerCase();
                    if(extensions.contains(extension)){
                    assayFiles.add(downloadFile(assayFile));
                    }else{
                     LOGGER.info(assayFile.getFileName()+" has an unsupported file type : "+extension);  
                    }//   }
                }
            } else {
                LOGGER.info("Skipping RAW files [might change in future update]");
            }
        }
        LOGGER.info("Download for " + experimentAccession + " completed");
        return assayFiles;
    }

}
