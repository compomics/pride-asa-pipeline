/* 
 * Copyright 2018 compomics.
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
package com.compomics.pride_asa_pipeline.core;

import com.compomics.pride_asa_pipeline.core.spring.ApplicationContextProvider;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

/**
 *
 * @author Niels Hulstaert Hulstaert
 */
public class PrideAsaPipelineStarter {

    //private static final Logger LOGGER = Logger.getLogger(PrideAsaPipelineStarter.class);
    private static final String HEADER = "[Pride automtic spectrum annotation pipeline]\n";
    private static final String USAGE = "java -jar <jar file name>";
    private static Options options;

    /**
     * Main executable.
     *
     * @param commandLineArguments Commmand-line arguments.
     */
    public static void main(String[] commandLineArguments) throws Exception {
        constructOptions();

        displayBlankLines(1, System.out);
        displayHeader(System.out);
        displayBlankLines(2, System.out);
        parse(commandLineArguments);
    }

    public static void launchCommandLineMode(String experimentAccession) throws Exception {
        ApplicationContextProvider.getInstance().setDefaultApplicationContext();
        ApplicationContext applicationContext = ApplicationContextProvider.getInstance().getApplicationContext();
        CommandLineRunner commandLineRunner = (CommandLineRunner) applicationContext.getBean("commandLineRunner");
        commandLineRunner.runPipeline(experimentAccession);
    }

    public static void launchCommandLineMode(File experimentAccessionsFile) throws Exception {
        ApplicationContextProvider.getInstance().setDefaultApplicationContext();
        ApplicationContext applicationContext = ApplicationContextProvider.getInstance().getApplicationContext();
        CommandLineRunner commandLineRunner = (CommandLineRunner) applicationContext.getBean("commandLineRunner");
        commandLineRunner.runPipeline(experimentAccessionsFile);
    }

    public static void launchFileCommandLineMode(File identificationsFile, boolean singleIdentificationsFile) throws Exception {
        ApplicationContextProvider.getInstance().setDefaultApplicationContext();
        ApplicationContext applicationContext = ApplicationContextProvider.getInstance().getApplicationContext();
        CommandLineRunner commandLineRunner = (CommandLineRunner) applicationContext.getBean("commandLineRunner");
        commandLineRunner.runPipeline(identificationsFile);
    }

    /**
     * Apply Apache Commons CLI parser to command-line arguments.
     *
     * @param commandLineArguments Command-line arguments to be processed.
     */
    private static void parse(String[] commandLineArguments) throws Exception {
        CommandLineParser cmdLineParser = new BasicParser();
        CommandLine commandLine;
        try {
            commandLine = cmdLineParser.parse(options, commandLineArguments);
            if (commandLine.hasOption('h')) {
                printHelp(
                        options, 80, "Help", "End of Help",
                        5, 3, true, System.out);
            }
            if (commandLine.hasOption('u')) {
                printUsage(USAGE, options, System.out);
            }
            if (commandLine.hasOption('f')) {
                String experimentAccessionsFilePath = commandLine.getOptionValue('f');
                File experimentAccessionsFile = new File(experimentAccessionsFilePath);
                if (experimentAccessionsFile.exists()) {
                    launchCommandLineMode(experimentAccessionsFile);
                } else {
                    System.out.println("No file with path \"" + experimentAccessionsFilePath + "\" could be found.");
                    printHelp(
                            options, 80, "Help", "End of Help",
                            5, 3, true, System.out);
                }
            }
            if (commandLine.hasOption('p')) {
                String identificationsFilePath = commandLine.getOptionValue('p');
                File identificationsFile = new File(identificationsFilePath);
                if (identificationsFile.exists()) {
                    launchFileCommandLineMode(identificationsFile, true);
                } else {
                    System.out.println("No file with path \"" + identificationsFilePath + "\" could be found.");
                    printHelp(
                            options, 80, "Help", "End of Help",
                            5, 3, true, System.out);
                }
            }
            if (commandLine.hasOption('s')) {
                String identificationsFilePath = commandLine.getOptionValue('s');
                File identificationsFilePathFile = new File(identificationsFilePath);
                if (identificationsFilePathFile.exists()) {
                    launchFileCommandLineMode(identificationsFilePathFile, false);
                } else {
                    System.out.println("No file with path \"" + identificationsFilePath + "\" could be found.");
                    printHelp(
                            options, 80, "Help", "End of Help",
                            5, 3, true, System.out);
                }
            }
            if (commandLine.hasOption('a')) {
                String experimentAccession = commandLine.getOptionValue('a');
                launchCommandLineMode(experimentAccession);
            }
        } catch (ParseException parseException) // checked exception
        {
            System.out.println("Encountered exception while parsing :\n"
                    + parseException.getMessage());
            printHelp(
                    options, 80, "Help", "End of Help",
                    5, 3, true, System.out);
        }
    }

    /**
     * Construct Options.
     *
     */
    private static void constructOptions() {
        options = new Options();

        options.addOption("h", "help", Boolean.FALSE, "Help");
        options.addOption("u", "usage", Boolean.FALSE, "Usage");

        Option accessionStringOption = new Option("a", "accession", true, "Experiment accession");
        accessionStringOption.setArgName("accession");
        Option identificationsFileOption = new Option("p", "identifications_file", true, "Identifications file path");
        identificationsFileOption.setArgName("identifications_file_path");
        Option identificationsFilePathFileOption = new Option("s", "identifications_file_paths_file", true, "Identifications paths file path");
        identificationsFileOption.setArgName("identifications_file_paths_file_path");
        Option accessionFileOption = new Option("f", "accessions_file", true, "Experiment accessions file path");
        accessionFileOption.setArgName("file_path");
        OptionGroup commandLineModeOptionGroup = new OptionGroup();
        commandLineModeOptionGroup.addOption(accessionStringOption);
        commandLineModeOptionGroup.addOption(identificationsFileOption);
        commandLineModeOptionGroup.addOption(identificationsFilePathFileOption);
        commandLineModeOptionGroup.addOption(accessionFileOption);

        options.addOptionGroup(commandLineModeOptionGroup);
    }

    /**
     * Display example application header.
     *
     * @out OutputStream to which header should be written.
     */
    private static void displayHeader(OutputStream out) {
        try {
            out.write(HEADER.getBytes());
        } catch (IOException ioEx) {
            System.out.println(HEADER);
        }
    }

    /**
     * Write the provided number of blank lines to the provided OutputStream.
     *
     * @param numberBlankLines Number of blank lines to write.
     * @param out OutputStream to which to write the blank lines.
     */
    private static void displayBlankLines(
            int numberBlankLines,
            OutputStream out) {
        try {
            for (int i = 0; i < numberBlankLines; ++i) {
                out.write("\n".getBytes());
            }
        } catch (IOException ioEx) {
            for (int i = 0; i < numberBlankLines; ++i) {
                System.out.println();
            }
        }
    }

    /**
     * Print usage information to provided OutputStream.
     *
     * @param applicationName Name of application to list in usage.
     * @param options Command-line options to be part of usage.
     * @param out OutputStream to which to write the usage information.
     */
    private static void printUsage(
            String applicationName,
            Options options,
            OutputStream out) {
        PrintWriter writer = new PrintWriter(out);
        HelpFormatter usageFormatter = new HelpFormatter();
        usageFormatter.printUsage(writer, 80, applicationName, options);
        writer.flush();
    }

    /**
     * Write "help" to the provided OutputStream.
     */
    private static void printHelp(
            Options options,
            int printedRowWidth,
            String header,
            String footer,
            int spacesBeforeOption,
            int spacesBeforeOptionDescription,
            boolean displayUsage,
            final OutputStream out) {
        PrintWriter writer = new PrintWriter(out);
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(
                writer,
                printedRowWidth,
                USAGE,
                header,
                options,
                spacesBeforeOption,
                spacesBeforeOptionDescription,
                footer,
                displayUsage);
        writer.flush();
    }

}
