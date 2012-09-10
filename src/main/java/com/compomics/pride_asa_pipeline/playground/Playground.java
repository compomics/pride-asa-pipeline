/*
 *

 */
package com.compomics.pride_asa_pipeline.playground;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Niels Hulstaert
 */
public class Playground {

    public static void main(String[] args) {
        //load application context
        //ApplicationContext applicationContext = ApplicationContextProvider.getInstance().getApplicationContext();

        Playground.filterExperimentAccessions(new File("C:\\Users\\niels\\Desktop\\pride_experiment_accessions.txt"), new File("C:\\Users\\niels\\Documents\\annotation_test\\pride"));
    }

    public static File filterExperimentAccessions(File experimentAccessionsFile, File resultsDirectory) {
        File filteredExperimentAccessionsFile = null;

        BufferedReader bufferedReader = null;
        PrintWriter printWriter = null;
        try {
            filteredExperimentAccessionsFile = new File("C:\\Users\\niels\\Desktop\\filtered_pride_experiment_accessions.txt");

            //1. read file names in result directory and put them in a set
            Set<String> processedExperimentAccessions = new HashSet<String>();
            if (!resultsDirectory.isDirectory()) {
                return null;
            }
            for (File resultFile : resultsDirectory.listFiles()) {
                processedExperimentAccessions.add(resultFile.getName().substring(0, resultFile.getName().indexOf('.')));
            }

            //2. read experiment accessions file and write the non processed experiment accessions to the new file
            bufferedReader = new BufferedReader(new FileReader(experimentAccessionsFile));
            printWriter = new PrintWriter(new BufferedWriter(new FileWriter(filteredExperimentAccessionsFile)));

            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                if (!processedExperimentAccessions.contains(line)) {
                    printWriter.println(line);
                }
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Playground.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Playground.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException ex) {
                Logger.getLogger(Playground.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return filteredExperimentAccessionsFile;
    }
}
