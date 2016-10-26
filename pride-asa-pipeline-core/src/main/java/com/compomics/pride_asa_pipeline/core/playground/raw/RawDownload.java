/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.playground.raw;

import com.compomics.util.io.FTPDownloader;
import com.compomics.util.pride.PrideWebService;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import uk.ac.ebi.pride.archive.web.service.model.file.FileDetail;
import uk.ac.ebi.pride.archive.web.service.model.file.FileDetailList;
import uk.ac.ebi.pride.archive.web.service.model.project.ProjectSummary;
import uk.ac.ebi.pride.archive.web.service.model.project.ProjectSummaryList;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class RawDownload {

    private static final Logger LOGGER = Logger.getLogger(RawDownload.class);

    public static void main(String[] args) throws IOException {
        args = new String[]{"C:\\Users\\compomics\\Desktop\\Putty\\Putty", "glycan", "glycomics", "glycopeptides", "glyco"};
        if (args.length == 0) {
            System.out.println("Please specify the target directory");
        }
        File repositoryFile = new File(args[0]);
        if (!repositoryFile.exists()) {
            System.out.println("The repository does not exist !!!!");
        } else {
            initLogging(repositoryFile);
            Set<ProjectSummary> summaries = new HashSet<>();
            if (args.length > 1) {
                LOGGER.info("Looking for projects fitting the terms...");
                for (int i = 1; i < args.length; i++) {
                    String aTerm = args[i];
                    LOGGER.info("Looking for " + aTerm);
                    ProjectSummaryList projectSummaryList = PrideWebService.getProjectSummaryList(aTerm);
                    summaries.addAll(projectSummaryList.getList());
                }
            } else {
                LOGGER.info("Adding all possible projects...");
                summaries.addAll(PrideWebService.getProjectSummaryList("").getList());
            }
            LOGGER.info("Number of projects matching the terms : " + summaries.size());
            File projectSummaryCollection = new File(repositoryFile, "project_summary.txt");
            projectSummaryCollection.getParentFile().mkdirs();
            try (FileWriter out = new FileWriter(projectSummaryCollection)) {
                out.append("Accession\t"
                        + "Title\t"
                        + "Type\t"
                        + "Species\t"
                        + "Instruments\t"
                        + System.lineSeparator()).flush();
                for (ProjectSummary summary : summaries) {
                    out.append(summary.getAccession() + "\t"
                            + summary.getTitle() + "\t"
                            + summary.getSubmissionType() + "\t"
                            + StringUtils.join(summary.getSpecies(), ",") + "\t"
                            + StringUtils.join(summary.getInstrumentNames(), ",")
                            + System.lineSeparator()).flush();
                }
                for (ProjectSummary summary : summaries) {
                    LOGGER.info("Aqcuiring files for " + summary.getAccession());
                    File tempFolder = new File(repositoryFile, summary.getAccession());
                    tempFolder.mkdirs();
                    FileDetailList projectFileDetails = PrideWebService.getProjectFileDetails(summary.getAccession());
                    for (FileDetail detail : projectFileDetails.getList()) {
                        if (detail.getFileName().contains(".raw")) {
                            LOGGER.info("Downloading " + detail.getFileName());
                            try {
                                if (detail.getFileName().endsWith(".zip")) {
                                    download(detail, tempFolder);
                                } else {
                                    downloadAndZip(detail, tempFolder);
                                }
                                LOGGER.info("Completed");
                            } catch (Exception ex) {
                                LOGGER.error("Something went wrong downloading " + detail.getFileName());
                                LOGGER.error(ex);
                            }
                        }
                    }
                }
            }
        }

    }

    private static void download(FileDetail detail, File tempFolder) throws Exception {
        URL url = detail.getDownloadLink();
        FTPDownloader downloader = new FTPDownloader(url.getHost(), true);
        File downloadFile = new File(tempFolder, detail.getFileName());
        downloader.downloadFile(url.getPath(), downloadFile);
        downloader.disconnect();
    }

    private static void downloadAndZip(FileDetail detail, File tempFolder) throws FileNotFoundException, IOException {
        URL url = detail.getDownloadLink();
        File outputFile = new File(tempFolder, detail.getFileName() + ".zip");
        int BUFFER = 2048;
        try (BufferedInputStream in = new BufferedInputStream(url.openStream(), BUFFER); ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));) {
            byte data[] = new byte[BUFFER];
            ZipEntry entry = new ZipEntry(detail.getFileName());
            out.putNextEntry(entry);
            int count;
            while ((count = in.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
        }
    }

    private static void initLogging(File outputFile) {
        FileAppender fa = new FileAppender();
        fa.setName("FileLogger");
        fa.setFile(new File(outputFile, "download.log").getAbsolutePath());
        fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
        fa.setThreshold(Level.ALL);
        fa.setAppend(true);
        fa.activateOptions();

        //add appender to any Logger (here is root)
        Logger.getRootLogger().addAppender(fa);
        //repeat with all other desired appenders
    }

}
