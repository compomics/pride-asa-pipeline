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
import com.compomics.pride_asa_pipeline.core.model.pride.web.impl.PrideProject;
import com.compomics.pride_asa_pipeline.core.model.pride.web.json.PrideFileType;
import com.compomics.pride_asa_pipeline.core.util.PrideWebUtils;
import com.compomics.pride_asa_pipeline.core.util.reporter.ProjectReporter;
import com.compomics.pride_asa_pipeline.core.util.reporter.impl.DefaultProjectReporter;
import com.compomics.util.experiment.identification.SearchParameters;
import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.json.simple.parser.ParseException;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;

/**
 *
 * @author Kenneth
 */
public class PrideWebProjectFinder {

    private static final Logger LOGGER = Logger.getLogger(PrideWebProjectFinder.class);
    private final File outputFolder;
    private final String projectAccession;

    public static void main(String[] args) throws IOException, ParseException {
      //  File outputFolder = new File(args[0]);
        File outputFolder = new File("C:\\Users\\Kenneth\\Desktop\\MzID_Test\\download");
//String projectAccession = args[1];
        String projectAccession = "PXD001423";
        //String projectAccession = "PRD000214";
        new PrideWebProjectFinder(outputFolder, projectAccession).analyze();
    }

    public PrideWebProjectFinder(File outputFolder, String projectAccession) {
        this.outputFolder = outputFolder;
        this.projectAccession = projectAccession;
    }

    public void analyze() throws IOException, ParseException {
        File projectOutputFolder = new File(outputFolder, projectAccession);
        projectOutputFolder.mkdirs();

        PrideWebUtils prideService = new PrideWebUtils();
        LOGGER.info("Getting metadata for " + projectAccession);
        PrideProject aProject = prideService.getPrideProject(projectAccession);

        LOGGER.info("Finding assay files for " + aProject.getAccession());

        for (PrideAssay assay : aProject.values()) {
            analyzeAssay(assay);
        }
    }

    public void analyzeAssay(PrideAssay assay) throws IOException {
        File assayOutputFolder = new File(outputFolder, assay.getAssayAccession());
        LOGGER.info("\t Finding metadata for assay : " + assay.getAssayAccession());
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
            LOGGER.info("Extracting search parameters");
            try {
                PrideAsapSearchParamExtractor extractor = new PrideAsapSearchParamExtractor(identificationsFile, peakFile);
                SearchParameters searchParametersFileForProject = extractor.getSearchParametersFileForProject();
                File outputParameters = new File(assayOutputFolder, assay.getProjectAccession() + "_" + assay.getAssayAccession() + ".parameters");
                SearchParameters.saveIdentificationParameters(searchParametersFileForProject, outputParameters);
                ProjectReporter reporter = new DefaultProjectReporter(extractor, assayOutputFolder);
                reporter.generateReport();
            } catch (Exception ex) {
                LOGGER.error(ex);
            }
            try {
                //extract MGF
                if (!peakFile.getName().toLowerCase().endsWith(".mgf")) {
                    LOGGER.info("Converting peak file to MGF");
                    DefaultMGFExtractor mgfExtractor = new DefaultMGFExtractor(peakFile);
                    File outputMGF = new File(assayOutputFolder, assay.getAssayAccession() + ".mgf");
                    outputMGF.createNewFile();
                    mgfExtractor.extractMGF(outputMGF);
                }
            } catch (ClassNotFoundException | MzXMLParsingException | JMzReaderException | MGFExtractionException ex) {
                LOGGER.error(ex);
            }
        }
    }

}
