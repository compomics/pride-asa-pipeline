/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.model.webservice.objects;

import com.compomics.pride_asa_pipeline.core.model.webservice.fields.PeptideField;
import java.util.ArrayList;

/**
 *
 * @author Kenneth
 */
public class PridePeptide {

    //standard fields
    private String id;
    private String assayAccession;
    private String sequence;
    private String projectAccession;
    private int startPosition;
    private int endPosition;
    private String proteinAccession;
    private String[] searchEngines;
    private String[] searchEngineScores;
    private double retentionTime;
    private int charge;
    private double calculatedMZ;
    private double experimentalMZ;
    private String preAA;
    private String postAA;
    private String spectrumID;
    private String reportedID;
    //files
    private ArrayList<PrideModifiedLocation> modLocations = new ArrayList<>();

    public void setField(PeptideField field, String parameter) {
        if (parameter == null) {
            return;
        }
        switch (field) {
            case id:
                setId(parameter);
                break;
            case modifications:
                setProjectAccession(parameter);
                break;
            case assayAccession:
                setAssayAccession(parameter);
                break;
            case sequence:
                setSequence(parameter);
                break;
            case projectAccession:
                setProjectAccession((parameter));
                break;
            case startPosition:
                setStartPosition(Integer.parseInt(parameter));
                break;
            case endPosition:
                setEndPosition(Integer.parseInt(parameter));
                break;
            case proteinAccession:
                setProteinAccession((parameter));
                break;
            case searchEngines:
                setSearchEngines(getParameterArray(parameter));
                break;
            case searchEngineScores:
                setSearchEngineScores(getParameterArray(parameter));
                break;
            case retentionTime:
                setRetentionTime(Double.parseDouble(parameter));
                break;
            case charge:
                setCharge(Integer.parseInt(parameter));
                break;
            case calculatedMZ:
                setCalculatedMZ(Double.parseDouble(parameter));
                break;
            case experimentalMZ:
                setExperimentalMZ(Double.parseDouble(parameter));
                break;
            case preAA:
                setPreAA(parameter);
                break;
            case postAA:
                setPostAA(parameter);
                break;
            case spectrumID:
                setSpectrumID((parameter));
                break;
            case reportedID:
                setReportedID((parameter));
                break;
        }
    }

    private String[] getParameterArray(String parameter) {
        return parameter.replace("[", "").replace("]", "").replace("\"", "").split(",");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAssayAccession() {
        return assayAccession;
    }

    public void setAssayAccession(String assayAccession) {
        this.assayAccession = assayAccession;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getProjectAccession() {
        return projectAccession;
    }

    public void setProjectAccession(String projectAccession) {
        this.projectAccession = projectAccession;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }

    public int getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(int endPosition) {
        this.endPosition = endPosition;
    }

    public String getProteinAccession() {
        return proteinAccession;
    }

    public void setProteinAccession(String proteinAccession) {
        this.proteinAccession = proteinAccession;
    }

    public String[] getSearchEngines() {
        return searchEngines;
    }

    public void setSearchEngines(String[] searchEngines) {
        this.searchEngines = searchEngines;
    }

    public String[] getSearchEngineScores() {
        return searchEngineScores;
    }

    public void setSearchEngineScores(String[] searchEngineScores) {
        this.searchEngineScores = searchEngineScores;
    }

    public double getRetentionTime() {
        return retentionTime;
    }

    public void setRetentionTime(double retentionTime) {
        this.retentionTime = retentionTime;
    }

    public int getCharge() {
        return charge;
    }

    public void setCharge(int charge) {
        this.charge = charge;
    }

    public double getCalculatedMZ() {
        return calculatedMZ;
    }

    public void setCalculatedMZ(double calculatedMZ) {
        this.calculatedMZ = calculatedMZ;
    }

    public double getExperimentalMZ() {
        return experimentalMZ;
    }

    public void setExperimentalMZ(double experimentalMZ) {
        this.experimentalMZ = experimentalMZ;
    }

    public String getPreAA() {
        return preAA;
    }

    public void setPreAA(String preAA) {
        this.preAA = preAA;
    }

    public String getPostAA() {
        return postAA;
    }

    public void setPostAA(String postAA) {
        this.postAA = postAA;
    }

    public String getSpectrumID() {
        return spectrumID;
    }

    public void setSpectrumID(String spectrumID) {
        this.spectrumID = spectrumID;
    }

    public String getReportedID() {
        return reportedID;
    }

    public void setReportedID(String reportedID) {
        this.reportedID = reportedID;
    }

    public ArrayList<PrideModifiedLocation> getModLocations() {
        return modLocations;
    }

    public void setModLocations(ArrayList<PrideModifiedLocation> modLocations) {
        this.modLocations = modLocations;
    }

    public void addModification(PrideModifiedLocation modLoc) {
        modLocations.add(modLoc);
    }

}
