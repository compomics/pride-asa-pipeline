/*
 * Copyright 2018 davy.
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
import com.compomics.pride_asa_pipeline.core.model.MGFExtractionException;
import com.compomics.pride_asa_pipeline.core.playground.LocalProjectExtractor;
import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileExperimentRepository;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.geneontology.oboedit.dataadapter.GOBOParseException;
import org.xmlpull.v1.XmlPullParserException;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;

/**
 *
 * @author davy
 */
public class FileRunner {

    public static void main(String[] args) throws ParseException {
        Options options = getOptions();
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(options, args);

        String inputFilePath = cmd.getOptionValue("i", "");
        File inputFile = new File(inputFilePath);
        if (inputFilePath.isEmpty()) {
            throw new ParseException("The input file is a mandatory parameter...");
        } else if (!inputFile.exists()) {
            throw new ParseException("The input file does not exist...");
        }

        String outputFolderPath = cmd.getOptionValue("o", "");
        File outputFolder;
        if (outputFolderPath.isEmpty()) {
            outputFolder = new File(System.getProperty("user.dir"));
        } else {
            outputFolder = new File(outputFolderPath);
        }
        outputFolder.mkdirs();

        String customModificationList = cmd.getOptionValue("mods", "");
        String[] customModifications = customModificationList.split(",");
        for (String aModName : customModifications) {
            UserSuggestedModifications.getInstance().addModification(aModName.trim());
        }
        try {
            SearchParameters analyze = new LocalProjectExtractor(outputFolder).analyze(inputFile);
            System.out.println(analyze);
            SearchParameters.saveIdentificationParameters(analyze, new File(outputFolder,inputFile.getName()+".par"));
        } catch (MGFExtractionException ex) {
            LOGGER.error(ex);
        } catch (MzXMLParsingException | JMzReaderException | XmlPullParserException | ClassNotFoundException | GOBOParseException ex) {
            LOGGER.error(ex);
        } catch (Exception ex) {
            LOGGER.error(ex);
        }

    }

    private static Options getOptions() {
        // create Options object
        Options options = new Options();

        // add t option
        options.addOption("o", true, "The output folder");
        options.addOption("i", true, "The input file");
        options.addOption("mods", true, "A comma separated list of modifications that need to be included");
        return options;
    }

    private static final Logger LOGGER = Logger.getLogger(LocalProjectExtractor.class);

    public SearchParameters analyze(File inputFile) throws IOException, MGFExtractionException, MzXMLParsingException, JMzReaderException, XmlPullParserException, ClassNotFoundException, GOBOParseException, Exception {
        LOGGER.info("Setting up experiment repository for assay " + inputFile.getName());
        FileExperimentRepository experimentRepository = new FileExperimentRepository();
        experimentRepository.addPrideXMLFile(inputFile.getName(), inputFile);
        LOGGER.info("Attempting to infer searchparameters");
        ParameterExtractor extractor = new ParameterExtractor(inputFile.getName());
        return extractor.getParameters();
    }

}
