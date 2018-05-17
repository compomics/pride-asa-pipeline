/*
 * Copyright 2018 kenne.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.compomics.pride_asa_pipeline.core.util;

import com.compomics.pride_asa_pipeline.core.data.user.UserSuggestedModifications;
import com.compomics.pride_asa_pipeline.core.logic.inference.ParameterExtractor;
import com.compomics.pride_asa_pipeline.core.model.exception.ParameterExtractionException;
import com.compomics.pride_asa_pipeline.core.repository.impl.combo.WebServiceFileExperimentRepository;
import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileExperimentRepository;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import com.compomics.pride_asa_pipeline.core.gui.PipelineProgressMonitor;

/**
 *
 * @author kenne
 */
public class PRIDEAssayRunner {

    private static Options options;

    private static File outputFolder;
    private static File identificationsFile;
    private static ArrayList<File> peakFileList = new ArrayList<>();
    private static ArrayList<String> modList = new ArrayList<>();

    private static String assayAccession;

    private static SearchParameters result;

    private static boolean useWebService;
    private static boolean print;

    public static void main(String[] args) throws ParseException, ParameterExtractionException, Exception {
        LoadOptions();
        ParseCommand(args);
        if (useWebService) {
            ExecuteWSCommand();
        } else {
            ExecuteLocalCommand();
        }
        GenerateOutput();
    }

    private static void LoadOptions() {
        options = new Options();
        options.addOption("o", true, "The output folder");
        options.addOption("i", true, "The identifications file (PRIDEXML or MZID");
        options.addOption("p", true, "A comma separated list of peakfiles (only applies to MZID)");
        options.addOption("mods", true, "A comma separated list of user specified modifications to include");
        options.addOption("WS", false, "Indicates there are no local files and the PRIDE WS should be used");
        options.addOption("assay", true, "The assay identifier in case the PRIDE WS is used");
        options.addOption("print", false, "Prints the results to the console");
    }

    private static void ParseCommand(String[] args) throws ParseException {
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(options, args);

        print = cmd.hasOption("print");

        if (cmd.hasOption("o")) {
            outputFolder = new File(cmd.getOptionValue("o"));
            outputFolder.mkdirs();
        } else {
            throw new ParseException("The outputfolder (-o) is a mandatory parameter !");
        }

        if (cmd.hasOption("mods")) {
            String[] parts = cmd.getOptionValue("mods").split(",");
            modList.addAll(Arrays.asList(parts));
        }

        //CASE WEBSERVICE
        if (cmd.hasOption("WS")) {
            useWebService = true;
            if (cmd.hasOption("assay")) {
                assayAccession = cmd.getOptionValue("assay");
                if (assayAccession.isEmpty()) {
                    throw new ParseException("The assay accession (-assay) can not be empty");
                }
            } else {
                throw new ParseException("The assay accession (-assay) is a mandatory parameter !");
            }
            //CASE REGULAR
        } else {

            if (cmd.hasOption("i")) {
                identificationsFile = new File(cmd.getOptionValue("i"));
                if (!identificationsFile.exists()) {
                    throw new ParseException("The identifications file does not exist !");
                }
            } else {
                throw new ParseException("The identifications file (PRIDEXML or MZID) (-i) is a mandatory parameter !");
            }

            if (cmd.hasOption("p")) {
                String[] parts = cmd.getOptionValue("p").split(",");

                for (String part : parts) {
                    File peakFile = new File(part.trim());
                    if (!identificationsFile.exists()) {
                        throw new ParseException("The peakfile " + peakFile.getAbsolutePath() + " does not exist !");
                    }
                    peakFileList.add(peakFile);
                }
            }
        }

    }

    private static void ExecuteLocalCommand() throws ParameterExtractionException {
        FileExperimentRepository experimentRepository = new FileExperimentRepository();
        PipelineProgressMonitor.info("Setting up experiment repository for assay " + identificationsFile.getName());
        //load the user mods
        for (String modName : modList) {
            UserSuggestedModifications.getInstance().addModification(modName);
        }
        //if there are no peakfiles, assume is a prideXML
        if (peakFileList.isEmpty()) {
            experimentRepository.addPrideXMLFile(identificationsFile.getName(), identificationsFile);
        } else {
            experimentRepository.addMzID(identificationsFile.getName(), identificationsFile, peakFileList);
        }
        PipelineProgressMonitor.info("Attempting to infer searchparameters");
        ParameterExtractor extractor = new ParameterExtractor(identificationsFile.getName());
        result = extractor.getParameters();

    }

    private static void ExecuteWSCommand() throws Exception {
        PipelineProgressMonitor.info("Setting up experiment repository for assay " + assayAccession);
        WebServiceFileExperimentRepository experimentRepository = new WebServiceFileExperimentRepository();
        experimentRepository.addAssay(assayAccession);
        PipelineProgressMonitor.info("Attempting to infer searchparameters");
        ParameterExtractor extractor = new ParameterExtractor(assayAccession);
        result = extractor.getParameters();
    }

    private static void GenerateOutput() throws IOException, ParameterExtractionException {
        if (result != null) {
            File outputFile;
            if (useWebService) {
                outputFile = new File(outputFolder, assayAccession + ".par");
            } else {
                outputFile = new File(outputFolder, identificationsFile.getName() + ".par");
            }
            SearchParameters.saveIdentificationParameters(result, outputFile);
            if (print) {
                System.out.println(result);
            }
        } else {
            throw new ParameterExtractionException("There were no parameters to save...");
        }
    }

}
