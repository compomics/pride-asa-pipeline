/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.playground.raw;

import com.compomics.util.io.compression.ZipUtils;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class RawToMzML {

    private static final Logger LOGGER = Logger.getLogger(RawToMzML.class);
    private static String PATH_TO_MSCONVERT = "C:/Program Files/ProteoWizard/ProteoWizard 3.0.9172/msconvert.exe";

    public static void main(String[] args) throws IOException, InterruptedException {
        //PATH_TO_MSCONVERT = args[0];
        //File inputFolder = new File(args[1]);
        //File outputFolder = new File(args[2]);
        File inputFolder = new File("C:\\Users\\compomics\\Desktop\\Putty\\Putty");
        File outputFolder = new File("C:\\Users\\compomics\\Desktop\\Putty\\Putty\\OUT");
      
        initLogging(inputFolder);
        List<File> processingFilesList = findRawFiles(inputFolder);
        LOGGER.info("Starting conversion");
        int index = 0;
        for (File aRawFile : processingFilesList) {
            index++;
            LOGGER.info("(" + index + "/" + processingFilesList.size() + ") Processing " + aRawFile.getAbsolutePath());
            File outputMzMLFolder = new File(outputFolder, aRawFile.getParentFile().getName());
            if (outputMzMLFolder.exists()) {
                outputMzMLFolder.mkdirs();
            }
            processRAW(aRawFile, outputMzMLFolder);
            LOGGER.info("Processing completed");
        }
    }

    private static boolean processRAW(File inputFile, File outputFolder) {

        int system_out;
        File tempFolder = null;
        try {
            tempFolder = createTempDirectory();
            if (inputFile.getName().endsWith(".zip")) {
                System.out.println("Unzipping to " + tempFolder.getAbsolutePath());
                inputFile = unzip(inputFile, tempFolder);
            }
            if (!new File(outputFolder, inputFile.getName().replace(".raw", ".mzML")).exists()) {
                Process startCommand = startCommand(inputFile, outputFolder);
                startCommand.waitFor();
                system_out = startCommand.exitValue();
            } else {
                System.out.println(inputFile.getName() + " was already converted");
                system_out = 0;
            }
        } catch (IOException | InterruptedException ex) {
            LOGGER.error(ex);
            system_out = -1;
        } finally {
            inputFile.delete();
            if (tempFolder != null) {
                try {
                    FileUtils.cleanDirectory(tempFolder);
                    tempFolder.delete();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return system_out == 0;
    }

    private static List<File> findRawFiles(File dir) {
        List<File> rawFiles = new ArrayList<>();
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                rawFiles.addAll(findRawFiles(file));
            } else if (file.getName().endsWith(".raw.zip")) {
                System.out.println("Adding " + file.getName() + " to list ...");
                rawFiles.add(file);
            }
        }
        return rawFiles;
    }

    private static boolean hasBeenConverted(File dir, String filename) {
        return new File(dir, filename.replace(".raw.zip", ".mzML")).exists();
    }

    private static Process startCommand(File inputFile, File outputFolder) throws IOException {
        String cmd = "\"" + PATH_TO_MSCONVERT + "\" "
                + "\"" + inputFile.getAbsolutePath() + "\" "
                + "--64 "
                + "--zlib "
                + "--filter "
                + "\"peakPicking true 1-\" "
                + "--filter "
                + "\"zeroSamples removeExtra\" "
                + "-o "
                + "\"" + outputFolder.getAbsolutePath() + "\"";
        System.out.println(cmd);
        ProcessBuilder pb = new ProcessBuilder(cmd)
                .inheritIO();
        return pb.start();
    }

    private static File unzip(File zippedFile, File outputFolder) throws IOException {
        ZipUtils.unzip(zippedFile, outputFolder, null);
        return new File(outputFolder, zippedFile.getName().replace(".zip", ""));
    }

    private static File createTempDirectory() throws IOException {
        final File temp;
        temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
        if (!(temp.delete())) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if (!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }
        return (temp);
    }

    private static void initLogging(File outputFile) {
        FileAppender fa = new FileAppender();
        fa.setName("FileLogger");
        fa.setFile(new File(outputFile, "conversion.log").getAbsolutePath());
        fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
        fa.setThreshold(Level.ALL);
        fa.setAppend(true);
        fa.activateOptions();

        //add appender to any Logger (here is root)
        Logger.getRootLogger().addAppender(fa);
        //repeat with all other desired appenders
    }

}
