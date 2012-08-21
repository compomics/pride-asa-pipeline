/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline;

import com.compomics.pride_asa_pipeline.gui.controller.MainController;
import com.compomics.pride_asa_pipeline.pipeline.CommandLineRunner;
import com.compomics.pride_asa_pipeline.spring.ApplicationContextProvider;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author Niels Hulstaert
 */
public class PrideAsaPipelineStarter {

    private static final Logger LOGGER = Logger.getLogger(PrideAsaPipelineStarter.class);
    private static final String HEADER = "[Pride automtic spectrum annotation pipeline]\n";
    private static final String USAGE = "java -jar <jar file name>";
    private static Options options = new Options();

    /**
     * Main executable.
     *
     * @param commandLineArguments Commmand-line arguments.
     */
    public static void main(String[] commandLineArguments) {
        displayBlankLines(1, System.out);
        displayHeader(System.out);
        displayBlankLines(2, System.out);
                
        parse(commandLineArguments);
    }
    
    public static void launchGuiMode() {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            LOGGER.error(ex.getMessage(), ex);
        } catch (InstantiationException ex) {
            LOGGER.error(ex.getMessage(), ex);
        } catch (IllegalAccessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                ApplicationContext applicationContext = ApplicationContextProvider.getInstance().getApplicationContext();
                MainController mainController = (MainController) applicationContext.getBean("mainController");
                mainController.init();
            }
        });
    }
    
    public static void launchCommandLineMode(String experimentAccession){
        ApplicationContext applicationContext = ApplicationContextProvider.getInstance().getApplicationContext();
        CommandLineRunner commandLineRunner = (CommandLineRunner) applicationContext.getBean("commandLineRunner");        
        commandLineRunner.runPipeline(experimentAccession);
    }
    
    public static void launchCommandLineMode(File experimentAccessionsFile){
        ApplicationContext applicationContext = ApplicationContextProvider.getInstance().getApplicationContext();
        CommandLineRunner commandLineRunner = (CommandLineRunner) applicationContext.getBean("commandLineRunner");        
        commandLineRunner.runPipeline(experimentAccessionsFile);
    }
    
    /**
     * Apply Apache Commons CLI parser to command-line arguments.
     *
     * @param commandLineArguments Command-line arguments to be processed.
     */
    private static void parse(String[] commandLineArguments) {
        CommandLineParser cmdLineParser = new BasicParser();

        Options options = constructOptions();
        CommandLine commandLine;
        try {
            commandLine = cmdLineParser.parse(options, commandLineArguments);
            if (commandLine.hasOption('g')) {
                launchGuiMode();
            }
            if (commandLine.hasOption('h')) {
                printHelp(
                        constructOptions(), 80, "Help", "End of Help",
                        5, 3, true, System.out);
            }
            if (commandLine.hasOption('u')) {
                printUsage(USAGE, constructOptions(), System.out);
            }
            if (commandLine.hasOption('f')) {
                
            }
            if (commandLine.hasOption('a')) {
                printUsage(USAGE, constructOptions(), System.out);
            }
        } catch (ParseException parseException) // checked exception
        {
            System.out.println("Encountered exception while parsing :\n"
                    + parseException.getMessage());
            printHelp(
                        constructOptions(), 80, "Help", "End of Help",
                        5, 3, true, System.out);
        }
    }

    /**
     * Construct and provide Options.
     *
     * @return Options expected from command-line.
     */
    private static Options constructOptions() {
        Options options = new Options();

        options.addOption("g", "gui", Boolean.FALSE, "GUI mode");
        options.addOption("h", "help", Boolean.FALSE, "Help");
        options.addOption("u", "usage", Boolean.FALSE, "Usage");

        Option accessionStringOption = new Option("a", "accession", Boolean.TRUE, "Experiment accession");
        accessionStringOption.setArgName("accession");
        Option accessionFileOption = new Option("f", "accessions_file", Boolean.TRUE, "Experiment accessions file path");
        accessionFileOption.setArgName("file_path");
        OptionGroup commandLineModeOptionGroup = new OptionGroup();
        commandLineModeOptionGroup.addOption(accessionFileOption);
        commandLineModeOptionGroup.addOption(accessionStringOption);

        options.addOptionGroup(commandLineModeOptionGroup);

        return options;
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
