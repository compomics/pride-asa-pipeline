/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.playground.util;

import com.compomics.util.pride.PrideWebService;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TreeMap;
import uk.ac.ebi.pride.archive.web.service.model.assay.AssayDetail;
import uk.ac.ebi.pride.archive.web.service.model.assay.AssayDetailList;
import uk.ac.ebi.pride.archive.web.service.model.project.ProjectSummary;
import uk.ac.ebi.pride.archive.web.service.model.project.ProjectSummaryList;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class IdentificationTimelineMaker {

    static TreeMap<Integer, Integer> annualIdentifications = new TreeMap<>();
    static TreeMap<Integer, Integer> annualSpectra = new TreeMap<>();

    public static void main(String[] args) throws IOException {
        File outputFile = new File("C:\\Users\\compomics\\Desktop\\Putty\\Putty\\pride_data.tsv");
        ProjectSummaryList projectSummaryList = PrideWebService.getProjectSummaryList("");
        int i = 0;
        for (ProjectSummary summary : projectSummaryList.getList()) {
            try{
            i++;
            System.out.println("Collecting data for " + summary.getAccession() + " (" + i + "/" + projectSummaryList.getList().size() + ")");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(summary.getPublicationDate());
            int year = calendar.get(Calendar.YEAR);
            AssayDetailList assayDetails = PrideWebService.getAssayDetails(summary.getAccession());
            for (AssayDetail detail : assayDetails.getList()) {
                annualIdentifications.put(year, annualIdentifications.getOrDefault(year, 0) + detail.getIdentifiedSpectrumCount());
                annualSpectra.put(year, annualSpectra.getOrDefault(year, 0) + detail.getTotalSpectrumCount());
            }
            }catch(Exception e){
              e.printStackTrace();
            }
        }
        try (FileWriter out = new FileWriter(outputFile)) {
            out.append("YEAR\tIDENTIFIED\tTOTAL" + System.lineSeparator());
            for (int year : annualIdentifications.keySet()) {
                out.append(year + "\t" + annualIdentifications.getOrDefault(year, 0) + "\t" + annualSpectra.getOrDefault(year, 0) + System.lineSeparator()).flush();
            }
        }
    }

}
