/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.playground;

import com.compomics.pride_asa_pipeline.core.exceptions.MGFExtractionException;
import com.compomics.util.pride.PrideWebService;
import com.compomics.util.pride.prideobjects.webservice.assay.AssayDetail;
import com.compomics.util.pride.prideobjects.webservice.assay.AssayDetailList;
import com.compomics.util.pride.prideobjects.webservice.peptide.ModificationLocation;
import com.compomics.util.pride.prideobjects.webservice.peptide.PsmDetail;
import com.compomics.util.pride.prideobjects.webservice.peptide.PsmDetailList;
import com.compomics.util.pride.prideobjects.webservice.project.projectsummary.ProjectSummary;
import com.compomics.util.pride.prideobjects.webservice.project.projectsummary.ProjectSummaryList;
import com.compomics.util.pride.prideobjects.webservice.query.PrideFilter;
import com.compomics.util.pride.prideobjects.webservice.query.PrideFilterType;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geneontology.oboedit.dataadapter.GOBOParseException;
import org.json.simple.parser.ParseException;
import org.xmlpull.v1.XmlPullParserException;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;

/**
 *
 * @author Kenneth
 */
public class PrideWebProjectDeaminatedFinder {

    private final File outputFolder;
    private String projectAccession;
    private String assayAccession;
    private static HashMap<String, Integer> peptideIDMap = new HashMap<>();

    public static void main(String[] args) throws IOException {
        File proteinOutputFile = new File(System.getProperty("user.home") + "/sindi/proteins.tsv");
        File sequenceOutputFile = new File(System.getProperty("user.home") + "/sindi/peptides.tsv");

        proteinOutputFile.getParentFile().mkdirs();
        proteinOutputFile.createNewFile();
        sequenceOutputFile.createNewFile();

        //prideService.getProjects(filters, true)
        //    for (PrideProject aProject : toProcess) {
        //  System.out.println("Processing " + aProject.getAccession());
        StringBuilder title = new StringBuilder();
        title.append("accession").append("\t")
                .append("indexesOfN").append("\t")
                .append("indexesOfQ").append("\t")
                .append("deamidated at").append("\t")
                .append("oxidated at").append("\t")
                .append("peptideIndex").append("\t")
                .append("assayId").append("\t")
                .append("projectId").append("\t")
                .append("spectrumId").append(System.lineSeparator());

        System.out.println("Finding all deamidation projects");

        try (FileWriter proteinWriter = new FileWriter(proteinOutputFile); FileWriter peptideWriter = new FileWriter(sequenceOutputFile);) {
            proteinWriter.append(title + System.lineSeparator()).flush();
            ProjectSummaryList projects = PrideWebService.getProjectSummaryList("", new PrideFilter(PrideFilterType.ptmsFilter, "deamidation"));
            int counter = 0;
            for (ProjectSummary aProject : projects.getList()) {
                try {
                    counter++;
                    System.out.println("Processing " + counter + "/" + aProject.getNumAssays());
                    AssayDetailList assayDetails = PrideWebService.getAssayDetails(aProject.getAccession());
                    for (AssayDetail anAssay : assayDetails.getList()) {
                        if (anAssay.getPeptideCount() > 0) {
                            PsmDetailList psMsByAssay = PrideWebService.getPSMsByAssay(anAssay.getAssayAccession());
                            System.out.println(psMsByAssay.getList().size());
                            for (PsmDetail aPeptide : psMsByAssay.getList()) {
                                String protein = aPeptide.getProteinAccession();
                                String projectId = aPeptide.getProjectAccession();
                                String assayId = aPeptide.getAssayAccession();
                                String spectrumId = aPeptide.getSpectrumID();
                                String sequence = aPeptide.getSequence().toUpperCase();
                                int peptideIndex;
                                if (!peptideIDMap.containsKey(sequence)) {
                                    peptideIndex = peptideIDMap.getOrDefault(sequence, peptideIDMap.size() + 1);
                                    peptideWriter.append(peptideIndex + "\t" + sequence).append(System.lineSeparator()).flush();
                                    peptideIDMap.put(sequence, peptideIndex);
                                }
                                peptideIndex = peptideIDMap.get(sequence);
                                String indexesOfN = getIndexesAsString(sequence, "N");
                                String indexesOfQ = getIndexesAsString(sequence, "Q");
                                String deamidated_peptides = "";
                                String oxidated_peptides = "";
                                ModificationLocation[] modifications = aPeptide.getModifications();
                                for (ModificationLocation modLoc : modifications) {
                                    if (modLoc.getModification().equalsIgnoreCase("MOD:00400")) {
                                        deamidated_peptides += modLoc.getLocation() + ",";
                                    }
                                    if (modLoc.getModification().equalsIgnoreCase("MOD:00425")) {
                                        oxidated_peptides += modLoc.getLocation() + ",";
                                    }
                                }
                                if (deamidated_peptides.endsWith(",")) {
                                    deamidated_peptides = deamidated_peptides.substring(0, deamidated_peptides.length() - 1);
                                }
                                if (oxidated_peptides.endsWith(",")) {
                                    oxidated_peptides = oxidated_peptides.substring(0, oxidated_peptides.length() - 1);
                                }
                                StringBuilder line = new StringBuilder();
                                line.append(protein).append("\t")
                                        .append(indexesOfN).append("\t")
                                        .append(indexesOfQ).append("\t")
                                        .append(deamidated_peptides).append("\t")
                                        .append(oxidated_peptides).append("\t")
                                        .append(peptideIndex).append("\t")
                                        .append(assayId).append("\t")
                                        .append(projectId).append("\t")
                                        .append(spectrumId).append(System.lineSeparator());
                                proteinWriter.append(line).append(System.lineSeparator()).flush();

                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(PrideWebProjectDeaminatedFinder.class.getName()).log(Level.SEVERE, null, ex);
        }
        FileWriter peptideWriter;
        peptideWriter = new FileWriter(sequenceOutputFile);
        for (Map.Entry<String, Integer> peptide : peptideIDMap.entrySet()) {
            peptideWriter.append(peptide.getValue() + "\t" + peptide.getKey()).append(System.lineSeparator()).flush();
        }
        peptideWriter.close();
        // }
    }

    private static String getIndexesAsString(String sequence, String letter) {
        String indexes = "";
        if (sequence.contains(letter)) {
            //index of Gln

            for (int index = sequence.indexOf(letter);
                    index >= 0;
                    index = sequence.indexOf(letter, index + 1)) {
                indexes += (index + 1) + ",";
            }

            indexes = indexes.substring(0, indexes.length() - 1);
        }
        return indexes;
    }

    public PrideWebProjectDeaminatedFinder(File outputFolder) {
        this.outputFolder = outputFolder;

    }

    public void analyze() throws IOException, ParseException, MGFExtractionException, MzXMLParsingException, JMzReaderException, XmlPullParserException, ClassNotFoundException, GOBOParseException {
        ProjectSummaryList projectSummaryList = PrideWebService.getProjectSummaryList("", new PrideFilter(PrideFilterType.ptmsFilter, "deamidation"));
        for (ProjectSummary aProject : projectSummaryList.getList()) {
            System.out.println(aProject.getAccession());
        }
    }

}
