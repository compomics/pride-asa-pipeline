package com.compomics.pride_asa_pipeline.core;

import com.compomics.pride_asa_pipeline.core.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.core.logic.FileSpectrumAnnotator;
import com.compomics.pride_asa_pipeline.core.service.ResultHandler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 *
 * @author Niels Hulstaert
 */
public class FileCommandLineRunner {

    private static final Logger LOGGER = Logger.getLogger(FileCommandLineRunner.class);
    private FileSpectrumAnnotator FileSpectrumAnnotator;
    private ResultHandler fileResultHandler;

    public FileSpectrumAnnotator getFileSpectrumAnnotator() {
        return FileSpectrumAnnotator;
    }

    public void setFileSpectrumAnnotator(FileSpectrumAnnotator fileSpectrumAnnotator) {
        this.FileSpectrumAnnotator = fileSpectrumAnnotator;
    }

    public ResultHandler getFileResultHandler() {
        return fileResultHandler;
    }

    public void setFileResultHandler(ResultHandler fileResultHandler) {
        this.fileResultHandler = fileResultHandler;
    }

    /**
     * Runs the pride XML file in command line mode.
     *
     * @param identificationsFile the identifications file
     * @param identificationsFile is the file a sinlge identifications file or
     * is does it contain identification file paths
     */
    public void runFilePipeline(File identificationsFile, boolean singleIdentificationsFile) {
        try {
            if (singleIdentificationsFile) {
                runFilePipeline(identificationsFile);
            } else {
                Set<String> identificationsFilePaths = readIdentificationsFilePaths(identificationsFile);
                for (String identificationsFilePath : identificationsFilePaths) {
                    File identificationsSingleFile = new File(identificationsFilePath);
                    if (identificationsSingleFile.exists()) {
                        runFilePipeline(identificationsSingleFile);
                    } else {
                        LOGGER.error("The given identifications file " + identificationsSingleFile + " could not be found. This file will be skipped.");
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Runs the identification file in command line mode.
     *
     * @param identificationsFile the identification file
     */
    private void runFilePipeline(File identificationsFile) {
        try {
            String experimentAccession = identificationsFile.getName().substring(0, identificationsFile.getName().indexOf(".xml"));

            //init the annotiation
            FileSpectrumAnnotator.initIdentifications(identificationsFile);

            //check if the experiment has "useful" identifications
            if (FileSpectrumAnnotator.getIdentifications().getCompleteIdentifications().isEmpty()) {
                LOGGER.warn("No useful identifications were found for experiment " + experimentAccession + ". This experiment will be skipped.");
                FileSpectrumAnnotator.clearTmpResources();
            } else {
                //check if the maximum systematic mass error is exceeded
                if (FileSpectrumAnnotator.getSpectrumAnnotatorResult().getMassRecalibrationResult().exceedsMaximumSystematicMassError()) {
                    LOGGER.warn("One or more systematic mass error exceed the maximum value of " + PropertiesConfigurationHolder.getInstance().getDouble("massrecalibrator.maximum_systematic_mass_error") + ", experiment " + experimentAccession + " will be skipped.");
                    FileSpectrumAnnotator.clearTmpResources();
                } else {
                    //continue with the annotiation
                    FileSpectrumAnnotator.annotate();
                    //write result to file
                    fileResultHandler.writeResultToFile(FileSpectrumAnnotator.getSpectrumAnnotatorResult());
                    fileResultHandler.writeUsedModificationsToFile(experimentAccession, FileSpectrumAnnotator.getModificationService().getUsedModifications(FileSpectrumAnnotator.getSpectrumAnnotatorResult()).keySet());
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private Set<String> readIdentificationsFilePaths(File experimentAccessionsFile) {
        Set<String> identificationsFilePaths = new HashSet<>();

        //read the experiment accession from the file
        try (BufferedReader br = new BufferedReader(new FileReader(experimentAccessionsFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                identificationsFilePaths.add(line);
            }
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return identificationsFilePaths;
    }
}