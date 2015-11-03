/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.playground;

import com.compomics.pride_asa_pipeline.core.exceptions.MGFExtractionException;
import com.compomics.pride_asa_pipeline.core.logic.parameters.PrideAsapExtractor;
import com.compomics.pride_asa_pipeline.core.logic.spectrum.DefaultMGFExtractor;
import com.compomics.pride_asa_pipeline.core.util.reporter.ProjectReporter;
import com.compomics.pride_asa_pipeline.core.util.reporter.impl.DefaultProjectReporter;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.io.FTPDownloader;
import com.compomics.util.pride.PrideWebService;
import com.compomics.util.pride.prideobjects.webservice.assay.AssayDetail;
import com.compomics.util.pride.prideobjects.webservice.file.FileDetail;
import com.compomics.util.pride.prideobjects.webservice.file.FileDetailList;
import com.compomics.util.pride.prideobjects.webservice.file.FileType;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import org.geneontology.oboedit.dataadapter.GOBOParseException;
import org.json.simple.parser.ParseException;
import org.xmlpull.v1.XmlPullParserException;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;

/**
 *
 * @author Kenneth
 */
public class PrideWebProjectFinder {

    private final long timeout = 30000;
    private final File outputFolder;
    private String projectAccession;
    private String assayAccession;

    public static void main(String[] args) throws IOException, ParseException, MGFExtractionException, MzXMLParsingException, JMzReaderException, XmlPullParserException, ClassNotFoundException, GOBOParseException, InterruptedException, Exception {
        int i = 0;
        //while (true) {
        //  File outputFolder = new File(args[0]);
        File outputFolder = new File("C:\\Users\\Kenneth\\Desktop\\MzID_Test\\download");
//String projectAccession = args[1];
        //String projectAccession = "PRD000001";
        //String assay = "38579";
        //  String assay = "27193";
        String assay = "3";
        i++;
        System.out.println("Starting iteration " + i);
        new PrideWebProjectFinder(outputFolder, assay).analyze();

        Thread.sleep(5000);
        //}
    }

    public PrideWebProjectFinder(File outputFolder, String assayAccession) {
        this.outputFolder = outputFolder;
        this.assayAccession = assayAccession;
    }

    public PrideWebProjectFinder(File outputFolder) {
        this.outputFolder = outputFolder;
    }

    public void analyze() throws IOException, ParseException, MGFExtractionException, MzXMLParsingException, JMzReaderException, XmlPullParserException, ClassNotFoundException, GOBOParseException, Exception {
        System.out.println("Getting metadata for assay " + assayAccession);
        AssayDetail assayDetail = PrideWebService.getAssayDetail(assayAccession);
        projectAccession = assayDetail.getProjectAccession();
        File projectOutputFolder = new File(outputFolder, projectAccession);
        projectOutputFolder.mkdirs();

        System.out.println("Finding assay files");
        FileDetailList assayFileDetails = PrideWebService.getAssayFileDetails(assayAccession);
        if (assayFileDetails.getList().isEmpty()) {
            System.out.println("WARNING : THERE ARE NO ASSAY FILES PRESENT!");
        } else {
            analyzeAssay(assayDetail);
        }
    }

    public void analyzeAssay(AssayDetail assay) throws IOException, MGFExtractionException, MzXMLParsingException, JMzReaderException, XmlPullParserException, ClassNotFoundException, GOBOParseException, Exception {
        this.assayAccession = assay.getAssayAccession();
        this.projectAccession = assay.getProjectAccession();
        File assayOutputFolder = new File(outputFolder, projectAccession + "/" + assay.getAssayAccession());
        File assayIdentFiles = new File(assayOutputFolder, "ident");
        File assayReportFiles = new File(assayOutputFolder, "report");
        System.out.println("\t Finding metadata for assay : " + assay.getAssayAccession() + " ( part of " + assay.getProjectAccession() + " )");
        File identificationsFile = null;
        File peakFile = null;

        //GET THE SPECTRA
        List<File> peakFiles = new ArrayList<File>();
        List<File> identFiles = new ArrayList<File>();
        FileDetailList assayFileDetails = PrideWebService.getAssayFileDetails(assayAccession);
        for (FileDetail assayFile : assayFileDetails.getList()) {
            //try to find existing peakfiles online
            if (assayFile.getFileType().equals(FileType.PEAK.toString())) {
                File downloadFile = downloadFile(assayFile);
                peakFiles.add(downloadFile);
                if (downloadFile.getAbsolutePath().endsWith(".xml")) {
                    identFiles.add(downloadFile);
                }
            }
        }
        //iterate the result files if there are no peakfiles?
        for (FileDetail assayFile : assayFileDetails.getList()) {
            if (assayFile.getFileType().equals(FileType.RESULT.toString())) {
                identificationsFile = downloadFile(assayFile);
                identFiles.add(identificationsFile);
                //fill up the peakFiles if required
                // attempt to extract MGF IF it doesn't exist...
                if (peakFiles.isEmpty() && !identificationsFile.getAbsolutePath().toLowerCase().endsWith(".mgf")) {
                    DefaultMGFExtractor mgfExtractor = new DefaultMGFExtractor(identificationsFile);
                    System.out.println("Extracting MGF");
                    File tempMGF = new File(assayOutputFolder, assayAccession + "_extracted.mgf");
                    File logFile = new File(assayReportFiles, tempMGF.getName() + ".conversion.log");
                    logFile.getParentFile().mkdirs();
                    logFile.createNewFile();
                    try (FileOutputStream fileOutputStream = new FileOutputStream(logFile)) {
                        mgfExtractor.extractMGF(tempMGF, fileOutputStream, timeout);
                        if (tempMGF.exists() && tempMGF.length() > 0) {
                            peakFiles.add(tempMGF);
                        }
                    }
                } else {
                    identificationsFile.renameTo(new File(assayOutputFolder, identificationsFile.getName()));
                }

            }
        }
        //is it required to merge the peakfiles into a temp master peak file?
        //move the peakfiles into their designated folder 
        File localTempMGF = new File(System.getProperty("user.home") + "/.compomics/pride-asap-extraction/master.mgf");
        if (localTempMGF.exists()) {
            localTempMGF.delete();
        }
        localTempMGF.getParentFile().mkdirs();
        localTempMGF.createNewFile();
        localTempMGF.deleteOnExit();
        int totalSpectra = 0;
        for (File assayBoundMGF : peakFiles) {
            try (FileWriter out = new FileWriter(localTempMGF, true);
                    BufferedReader br = new BufferedReader(new FileReader(assayBoundMGF))) {
                System.out.println("Appending " + assayBoundMGF.getName() + " to masterMGF");
                for (String line; (line = br.readLine()) != null;) {
                    if (line.startsWith("BEGIN")) {
                        totalSpectra++;
                    }
                    out.append(line.replace("\t", " ") + System.lineSeparator()).flush();
                }
            }
        }

        //GET THE PARAMETERS
        ProjectReporter reporter = null;
        PrideAsapExtractor extractor = null;
        try {
            //TODO what with multiple results?
            extractor = new PrideAsapExtractor(assay.getAssayAccession(),assayIdentFiles);
            //extract parameters
            System.out.println("Extracting search parameters");
            SearchParameters searchParameters = extractor.inferSearchParameters();
            File outputParameters = new File(assayOutputFolder, assay.getAssayAccession() + ".par");

            SearchParameters.saveIdentificationParameters(searchParameters, outputParameters);
            //write a report
            reporter = new DefaultProjectReporter(extractor, assayReportFiles);
            reporter.generateReport();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reporter != null) {
                reporter.clear();
            }
            if (extractor != null) {
                extractor.clear();
            }
        }
    }
    //eliminate all that is not MS2 ?

    private File downloadFile(FileDetail detail) throws Exception {
        URL url = new URL(detail.getDownloadLink());
        FTPDownloader downloader = new FTPDownloader(url.getHost());
        File downloadFile = new File(outputFolder, detail.getFileName());
        System.out.println("Downloading : " + url.getPath());
        downloader.downloadFile(url.getPath(), downloadFile);
        if(downloadFile.getAbsolutePath().endsWith(".gz")){
            File temp =new File(outputFolder,downloadFile.getName().replace(".gz",""));
            gunzip(downloadFile,temp);
            downloadFile.delete();
            downloadFile=temp;
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
            System.out.println("Done");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
