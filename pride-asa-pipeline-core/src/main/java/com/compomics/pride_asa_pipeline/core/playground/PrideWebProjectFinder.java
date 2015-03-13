/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.playground;

import com.compomics.pride_asa_pipeline.core.exceptions.MGFExtractionException;
import com.compomics.pride_asa_pipeline.core.logic.parameters.PrideAsapExtractor;
import com.compomics.pride_asa_pipeline.core.model.webservice.fields.PrideFileType;
import com.compomics.pride_asa_pipeline.core.model.webservice.objects.PrideAssay;
import com.compomics.pride_asa_pipeline.core.model.webservice.objects.PrideAssayFile;
import com.compomics.pride_asa_pipeline.core.util.PrideMetadataUtils;
import com.compomics.pride_asa_pipeline.core.util.reporter.ProjectReporter;
import com.compomics.pride_asa_pipeline.core.util.reporter.impl.DefaultProjectReporter;
import com.compomics.util.experiment.identification.SearchParameters;
import java.io.File;
import java.io.IOException;
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
        String assay = "38579";
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
        System.out.println("\t Finding metadata for assay : " + assay.getAssayAccession() + " ( part of " + assay.getProjectAccession() + " )");
        File identificationsFile = null;
        File peakFile = null;

        //split up the loop to prevent overriding existing things?
        for (PrideAssayFile assayFile : assay.getAssayFiles()) {
            if (assayFile.getFileType().equals(PrideFileType.RESULT)) {
                identificationsFile = assayFile.downloadFile(assayOutputFolder);
                if (identificationsFile.getName().endsWith(".xml")) {
                    peakFile = identificationsFile;
                    break;
                }
            } else if (assayFile.getFileType().equals(PrideFileType.PEAK)) {
                peakFile = assayFile.downloadFile(assayOutputFolder);
            }
        }

        if (identificationsFile != null && peakFile != null) {
            ProjectReporter reporter = null;
            PrideAsapExtractor extractor = null;
            try {
                extractor = new PrideAsapExtractor(identificationsFile, peakFile);
                //extract MGF IF it doesn't exist...
                if (!peakFile.getAbsolutePath().toLowerCase().endsWith(".mgf")) {
                    System.out.println("Extracting MGF");
                    File logFile = new File(peakFile.getAbsolutePath() + ".conversion.log");
                    File mgfFileForProject = extractor.getMGFFileForProject(logFile);
                }else{
                    System.out.println("The downloaded format is correct");
                }
                //extract parameters
                System.out.println("Extracting search parameters");
                SearchParameters searchParametersFileForProject = extractor.getSearchParametersFileForProject();
                File outputParameters = new File(assayOutputFolder, assay.getAssayAccession() + ".parameters");
                SearchParameters.saveIdentificationParameters(searchParametersFileForProject, outputParameters);
                //write a report
                reporter = new DefaultProjectReporter(extractor, assayOutputFolder);
                reporter.generateReport();
            } finally {
                if (reporter != null) {
                    reporter.clear();
                }
                if (extractor != null) {
                    extractor.clear();
                }
            }
        }
    }
}
