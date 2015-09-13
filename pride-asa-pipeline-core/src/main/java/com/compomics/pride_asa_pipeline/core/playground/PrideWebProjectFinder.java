/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.playground;

import com.compomics.pride_asa_pipeline.core.exceptions.MGFExtractionException;
import com.compomics.pride_asa_pipeline.core.logic.parameters.PrideAsapExtractor;
import com.compomics.pride_asa_pipeline.core.logic.spectrum.DefaultMGFExtractor;
import com.compomics.pride_asa_pipeline.core.model.webservice.fields.PrideFileType;
import com.compomics.pride_asa_pipeline.core.model.webservice.objects.PrideAssay;
import com.compomics.pride_asa_pipeline.core.model.webservice.objects.PrideAssayFile;
import com.compomics.pride_asa_pipeline.core.util.PrideMetadataUtils;
import com.compomics.pride_asa_pipeline.core.util.reporter.ProjectReporter;
import com.compomics.pride_asa_pipeline.core.util.reporter.impl.DefaultProjectReporter;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    public static void main(String[] args) throws IOException, ParseException, MGFExtractionException, MzXMLParsingException, JMzReaderException, XmlPullParserException, ClassNotFoundException, GOBOParseException, InterruptedException {
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

    public void analyze() throws IOException, ParseException, MGFExtractionException, MzXMLParsingException, JMzReaderException, XmlPullParserException, ClassNotFoundException, GOBOParseException {
        PrideMetadataUtils prideService = PrideMetadataUtils.getInstance();
        System.out.println("Getting metadata for assay " + assayAccession);
        PrideAssay prideAssay = prideService.getAssay(assayAccession);
        projectAccession = prideAssay.getProjectAccession();
        File projectOutputFolder = new File(outputFolder, projectAccession);
        projectOutputFolder.mkdirs();

        System.out.println("Finding assay files");

        if (prideAssay.getAssayFiles().isEmpty()) {
            System.out.println("WARNING : THERE ARE NO ASSAY FILES PRESENT!");
        } else {
            analyzeAssay(prideAssay);
        }
    }

    public void analyzeAssay(PrideAssay assay) throws IOException, MGFExtractionException, MzXMLParsingException, JMzReaderException, XmlPullParserException, ClassNotFoundException, GOBOParseException {
        this.assayAccession = assay.getAssayAccession();
        this.projectAccession = assay.getProjectAccession();
        File assayOutputFolder = new File(outputFolder, projectAccession + "/" + assay.getAssayAccession());
        File assayPeakFiles = new File(assayOutputFolder, "peak");
        File assayIdentFiles = new File(assayOutputFolder, "ident");
        File assayReportFiles = new File(assayOutputFolder, "report");
        System.out.println("\t Finding metadata for assay : " + assay.getAssayAccession() + " ( part of " + assay.getProjectAccession() + " )");
        File identificationsFile = null;
        File peakFile = null;

        //GET THE SPECTRA
        //split up the loop to prevent overriding existing things?
        if (!assayPeakFiles.exists()) {
            assayPeakFiles.mkdirs();
        }
        List<File> peakFiles = new ArrayList<File>();
        List<File> identFiles = new ArrayList<File>();
        
        for (PrideAssayFile assayFile : assay.getAssayFiles()) {
            //try to find existing peakfiles online
            if (assayFile.getFileType().equals(PrideFileType.PEAK)) {
                File downloadFile = assayFile.downloadFile(assayPeakFiles);
                peakFiles.add(downloadFile);
                if(downloadFile.getAbsolutePath().endsWith(".xml")){
                    identFiles.add(downloadFile);
                }
            }
        }
        //iterate the result files if there are no peakfiles?
        for (PrideAssayFile assayFile : assay.getAssayFiles()) {
            if (assayFile.getFileType().equals(PrideFileType.RESULT)) {
                identificationsFile = assayFile.downloadFile(assayIdentFiles);
                identFiles.add(identificationsFile);
                //fill up the peakFiles if required
                // attempt to extract MGF IF it doesn't exist...
                if (!identificationsFile.getAbsolutePath().toLowerCase().endsWith(".mgf")) {
                    DefaultMGFExtractor mgfExtractor = new DefaultMGFExtractor(identificationsFile);
                    System.out.println("Extracting MGF");
                    File tempMGF = new File(assayPeakFiles, assayAccession + "_extracted.mgf");
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
                    identificationsFile.renameTo(new File(assayPeakFiles, identificationsFile.getName()));
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
                    out.append(line.replace("\t"," ") + System.lineSeparator()).flush();
                }
            }
        }

        //GET THE PARAMETERS
            ProjectReporter reporter = null;
            PrideAsapExtractor extractor = null;
                try {
                    //TODO what with multiple results?
                    extractor = new PrideAsapExtractor(identFiles.get(0), localTempMGF);
                    //extract parameters
                    System.out.println("Extracting search parameters");
                    SearchParameters searchParameters = extractor.getSearchParametersFileForProject();
                    File outputParameters = new File(assayOutputFolder, assay.getAssayAccession() + ".parameters");

                    SearchParameters.saveIdentificationParameters(searchParameters, outputParameters);
                    //write a report
                    File parameterReport = new File(assayReportFiles, "extracted_parameters.txt");
                    try (FileWriter writer = new FileWriter(parameterReport)) {
                        writer.append("Total spectra considered in assay : " + totalSpectra + System.lineSeparator());
                        writer.append(SearchParameters.getIdentificationParameters(outputParameters).toString()).flush();
                    }
                    reporter = new DefaultProjectReporter(extractor, assayReportFiles);
                    reporter.generateReport();
                }catch(Exception e){
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
        }
