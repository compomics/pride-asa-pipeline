/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.playground;

import com.compomics.pride_asa_pipeline.core.exceptions.MGFExtractionException;
import com.compomics.pride_asa_pipeline.core.logic.parameters.PrideAsapSearchParamExtractor;
import com.compomics.pride_asa_pipeline.core.logic.spectrum.DefaultMGFExtractor;
import com.compomics.pride_asa_pipeline.core.model.pride.web.impl.PrideAssay;
import com.compomics.pride_asa_pipeline.core.model.pride.web.impl.PrideAssayFile;
import com.compomics.pride_asa_pipeline.core.model.pride.web.json.PrideFileType;
import com.compomics.pride_asa_pipeline.core.util.PrideWebUtils;
import com.compomics.pride_asa_pipeline.core.util.reporter.ProjectReporter;
import com.compomics.pride_asa_pipeline.core.util.reporter.impl.DefaultProjectReporter;
import com.compomics.util.experiment.identification.SearchParameters;
import java.io.File;
import java.io.IOException;
import org.json.simple.parser.ParseException;
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

    public static void main(String[] args) throws IOException, ParseException {
        //  File outputFolder = new File(args[0]);
        File outputFolder = new File("C:\\Users\\Kenneth\\Desktop\\MzID_Test\\download");
//String projectAccession = args[1];
        //String projectAccession = "PRD000001";
        String assay = "PRD000214";
        new PrideWebProjectFinder(outputFolder, assay).analyze();
    }

    public PrideWebProjectFinder(File outputFolder, String assayAccession) {
        this.outputFolder = outputFolder;
        this.assayAccession = assayAccession;
    }

    public PrideWebProjectFinder(File outputFolder) {
        this.outputFolder = outputFolder;
    }

    public void analyze() throws IOException, ParseException {
        PrideWebUtils prideService = new PrideWebUtils();
        System.out.println("Getting metadata for assay " + assayAccession);
        PrideAssay prideAssay = prideService.getPrideAssay(assayAccession);
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

    public void analyzeAssay(PrideAssay assay) throws IOException {
        this.assayAccession = assay.getAssayAccession();
        this.projectAccession=assay.getProjectAccession();
        File assayOutputFolder = new File(outputFolder, projectAccession + "/" + assay.getAssayAccession());
        System.out.println("\t Finding metadata for assay : " + assay.getAssayAccession() + " ( part of " + assay.getProjectAccession() + " )");
        File identificationsFile = null;
        File peakFile = null;
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
            //extract parameters
            System.out.println("Extracting search parameters");
            try {
                PrideAsapSearchParamExtractor extractor = new PrideAsapSearchParamExtractor(identificationsFile, peakFile);
                SearchParameters searchParametersFileForProject = extractor.getSearchParametersFileForProject();
                File outputParameters = new File(assayOutputFolder, assay.getAssayAccession() + ".parameters");
                SearchParameters.saveIdentificationParameters(searchParametersFileForProject, outputParameters);
                ProjectReporter reporter = new DefaultProjectReporter(extractor, assayOutputFolder);
                reporter.generateReport();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                //extract MGF
                if (!peakFile.getName().toLowerCase().endsWith(".mgf")) {
                    System.out.println("Converting peak file to MGF");
                    DefaultMGFExtractor mgfExtractor = new DefaultMGFExtractor(peakFile);
                    File outputMGF = new File(assayOutputFolder, assay.getAssayAccession() + ".mgf");
                    outputMGF.createNewFile();
                    mgfExtractor.extractMGF(outputMGF);
                }
            } catch (ClassNotFoundException | MzXMLParsingException | JMzReaderException | MGFExtractionException ex) {
                ex.printStackTrace();
            }
        }
    }

}
