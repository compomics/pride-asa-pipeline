/*
 *

 */
package com.compomics.pride_asa_pipeline.core.playground;

import com.compomics.pride_asa_pipeline.core.FileCommandLineRunner;
import com.compomics.pride_asa_pipeline.core.logic.SpectrumAnnotatorResult;
import com.compomics.pride_asa_pipeline.core.service.ModificationService;
import com.compomics.pride_asa_pipeline.core.spring.ApplicationContextProvider;
import com.compomics.pride_asa_pipeline.model.Modification;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.ApplicationContext;

/**
 * @author Niels Hulstaert
 */
public class Playground {

    private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(Playground.class);

    public static void main(String[] args) {
        //load application context
        ApplicationContextProvider.getInstance().setDefaultApplicationContext();
        ApplicationContext applicationContext = ApplicationContextProvider.getInstance().getApplicationContext();        

        FileCommandLineRunner fileCommandLineRunner = applicationContext.getBean("fileCommandLineRunner", FileCommandLineRunner.class);
        fileCommandLineRunner.runFilePipeline(new File("W:\\PRIDE-DATA\\PRIDE-FTP-DOWNLOAD\\PRIDE_Exp_Complete_Ac_3659.xml"), true);
        
        SpectrumAnnotatorResult spectrumAnnotatorResult = fileCommandLineRunner.getFileSpectrumAnnotator().getSpectrumAnnotatorResult();
        ModificationService modificationService = fileCommandLineRunner.getFileSpectrumAnnotator().getModificationService();
        Map<Modification, Integer> usedModifications = modificationService.getUsedModifications(spectrumAnnotatorResult);
        Map<Modification, Double> estimateModificationRate = modificationService.estimateModificationRate(usedModifications, spectrumAnnotatorResult, 0.05);  
        
//        PrideXmlSpectrumAnnotator prideXmlSpectrumAnnotator = (PrideXmlSpectrumAnnotator) applicationContext.getBean("prideXmlSpectrumAnnotator");
//        PrideXmlExperimentService iPrideService = prideXmlSpectrumAnnotator.getExperimentService();
//        iPrideService.init(new File("C:\\Users\\niels\\Desktop\\ExampleDataSets\\PRIDE_Exp_Complete_Ac_15346.xml"));
        
        System.out.println("test");
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
