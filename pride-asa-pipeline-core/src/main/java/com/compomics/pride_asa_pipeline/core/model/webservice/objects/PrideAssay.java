/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.model.webservice.objects;

import com.compomics.pride_asa_pipeline.core.model.webservice.fields.AssayField;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Kenneth
 */
public class PrideAssay {

    //standard fields
    private String assayAccession;
    private String projectAccession;
    private String experimentalFactor;
    private int proteinCount;
    private int peptideCount;
    private int uniquePeptideCount;
    private int identifiedSpectrumCount;
    private int totalSpectrumCount;
    private String[] softwares;
    private String[] relatedDisease;
    private boolean hasMS2;
    private boolean hasChromatogram;
    private String[] quantMethodsUsed;
    private String[] tags;
    private String title;
    private String shortLabel;
    private String[] ptms;
    private String[] species;
    private String[] instruments;
    private String[] sampleDetails;
    //files
    private final ArrayList<PrideAssayFile> assayFiles = new ArrayList<>();

    public void setField(AssayField field, String parameter) {
        if (parameter == null) {
            return;
        }
        switch (field) {
            case assayAccession:
                setAssayAccession(parameter);
                break;
            case projectAccession:
                setProjectAccession(parameter);
                break;
            case experimentalFactor:
                setExperimentalFactor(parameter);
                break;
            case proteinCount:
                setProteinCount(Integer.parseInt(parameter));
                break;
            case peptideCount:
                setPeptideCount(Integer.parseInt(parameter));
                break;
            case uniquePeptideCount:
                setUniquePeptideCount(Integer.parseInt(parameter));
                break;
            case identifiedSpectrumCount:
                setIdentifiedSpectrumCount(Integer.parseInt(parameter));
                break;
            case totalSpectrumCount:
                setTotalSpectrumCount(Integer.parseInt(parameter));
                break;
            case softwares:
                setSoftwares(getParameterArray(parameter));
                break;
            case relatedDisease:
                setRelatedDisease(getParameterArray(parameter));
                break;
            case hasMS2:
                setHasMS2(Boolean.parseBoolean(parameter));
                break;
            case hasChromatogram:
                setHasChromatogram(Boolean.parseBoolean(parameter));
                break;
            case quantMethodsUsed:
                setQuantMethodsUsed(getParameterArray(parameter));
                break;
            case tags:
                setTags(getParameterArray(parameter));
                break;
            case title:
                setTitle(parameter);
                break;
            case shortLabel:
                setShortLabel(parameter);
                break;
            case ptms:
                setPtms(getParameterArray(parameter));
                break;
            case species:
                setSpecies(getParameterArray(parameter));
                break;
            case instruments:
                setInstruments(getParameterArray(parameter));
                break;
            case sampleDetails:
                setSampleDetails(getParameterArray(parameter));
                break;
        }
    }

    private String[] getParameterArray(String parameter) {
        return parameter.replace("[", "").replace("]", "").replace("\"", "").split(",");
    }

    public String getAssayAccession() {
        return assayAccession;
    }

    private void setAssayAccession(String assayAccession) {
        this.assayAccession = assayAccession;
    }

    public String getProjectAccession() {
        return projectAccession;
    }

    private void setProjectAccession(String projectAccession) {
        this.projectAccession = projectAccession;
    }

    public String getExperimentalFactor() {
        return experimentalFactor;
    }

    private void setExperimentalFactor(String experimentalFactor) {
        this.experimentalFactor = experimentalFactor;
    }

    public int getProteinCount() {
        return proteinCount;
    }

    private void setProteinCount(int proteinCount) {
        this.proteinCount = proteinCount;
    }

    public int getPeptideCount() {
        return peptideCount;
    }

    private void setPeptideCount(int peptideCount) {
        this.peptideCount = peptideCount;
    }

    public int getUniquePeptideCount() {
        return uniquePeptideCount;
    }

    private void setUniquePeptideCount(int uniquePeptideCount) {
        this.uniquePeptideCount = uniquePeptideCount;
    }

    public int getIdentifiedSpectrumCount() {
        return identifiedSpectrumCount;
    }

    private void setIdentifiedSpectrumCount(int identifiedSpectrumCount) {
        this.identifiedSpectrumCount = identifiedSpectrumCount;
    }

    public int getTotalSpectrumCount() {
        return totalSpectrumCount;
    }

    private void setTotalSpectrumCount(int totalSpectrumCount) {
        this.totalSpectrumCount = totalSpectrumCount;
    }

    public String[] getSoftwares() {
        return softwares;
    }

    private void setSoftwares(String[] softwares) {
        this.softwares = softwares;
    }

    public String[] getRelatedDisease() {
        return relatedDisease;
    }

    private void setRelatedDisease(String[] relatedDisease) {
        this.relatedDisease = relatedDisease;
    }

    public boolean isHasMS2() {
        return hasMS2;
    }

    private void setHasMS2(boolean hasMS2) {
        this.hasMS2 = hasMS2;
    }

    public boolean isHasChromatogram() {
        return hasChromatogram;
    }

    private void setHasChromatogram(boolean hasChromatogram) {
        this.hasChromatogram = hasChromatogram;
    }

    public String[] getQuantMethodsUsed() {
        return quantMethodsUsed;
    }

    private void setQuantMethodsUsed(String[] quantMethodsUsed) {
        this.quantMethodsUsed = quantMethodsUsed;
    }

    public String[] getTags() {
        return tags;
    }

    private void setTags(String[] tags) {
        this.tags = tags;
    }

    public String getTitle() {
        return title;
    }

    private void setTitle(String title) {
        this.title = title;
    }

    public String getShortLabel() {
        return shortLabel;
    }

    private void setShortLabel(String shortLabel) {
        this.shortLabel = shortLabel;
    }

    public String[] getPtms() {
        return ptms;
    }

    private void setPtms(String[] ptms) {
        this.ptms = ptms;
    }

    public String[] getSpecies() {
        return species;
    }

    private void setSpecies(String[] species) {
        this.species = species;
    }

    public String[] getInstruments() {
        return instruments;
    }

    private void setInstruments(String[] instruments) {
        this.instruments = instruments;
    }

    public String[] getSampleDetails() {
        return sampleDetails;
    }

    private void setSampleDetails(String[] sampleDetails) {
        this.sampleDetails = sampleDetails;
    }

    public void addAssayFile(PrideAssayFile aFile) {
        assayFiles.add(aFile);
    }

    public void addAssayFiles(Collection<PrideAssayFile> allAssayFiles) {
        assayFiles.addAll(allAssayFiles);
    }

    public ArrayList<PrideAssayFile> getAssayFiles() {
        return assayFiles;
    }

}
