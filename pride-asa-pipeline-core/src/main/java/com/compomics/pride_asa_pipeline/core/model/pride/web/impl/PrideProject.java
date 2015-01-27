/*
 * To change this license header; choose License Headers in Project Properties.
 * To change this template file; choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.model.pride.web.impl;

import com.compomics.pride_asa_pipeline.core.model.pride.web.json.ProjectField;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author Kenneth
 */
public class PrideProject extends HashMap<String, PrideAssay> {

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private int numPeptides;
    private int numIdentifiedSpectra;
    private int numUniquePeptides;
    private String[] keywords;
    private PrideContactDetail submitter;
    private String doi;
    private String reanalysis;
    private String[] experimentTypes;
    private Date submissionDate;
    private ArrayList<PrideContactDetail> labHeads = new ArrayList<>();
    private String sampleProcessingProtocol;
    private String dataProcessingProtocol;
    private String otherOmicsLink;
    private String[] quantificationMethods;
    private int numProteins;
    private int numSpectra;
    private String accession;
    private String title;
    private String projectDescription;
    private int numAssays;
    private String submissionType;
    private Date publicationDate;
    private String[] projectTags;
    private String[] tissues;
    private String[] ptmNames;
    private String[] species;
    private String[] instrumentNames;

    public void setField(ProjectField field, String parameter) {
        if (parameter == null) {
            return;
        }
        switch (field) {
            case numPeptides:
                setNumPeptides(Integer.parseInt(parameter));
                break;
            case numIdentifiedSpectra:
                setNumIdentifiedSpectra(Integer.parseInt(parameter));
                break;
            case numUniquePeptides:
                setNumUniquePeptides(Integer.parseInt(parameter));
                break;
            case keywords:
                setKeywords(getParameterArray(parameter));
                break;
            case doi:
                setDoi(parameter);
                break;
            case reanalysis:
                setReanalysis(parameter);
                break;
            case experimentTypes:
                setExperimentTypes(getParameterArray(parameter));
                break;
            case submissionDate:
                setSubmissionDate(getParameterDate(parameter));
                break;
            case sampleProcessingProtocol:
                setSampleProcessingProtocol(parameter);
                break;
            case dataProcessingProtocol:
                setDataProcessingProtocol(parameter);
                break;
            case otherOmicsLink:
                setOtherOmicsLink(parameter);
                break;
            case quantificationMethods:
                setQuantificationMethods(getParameterArray(parameter));
                break;
            case numProteins:
                setNumProteins(Integer.parseInt(parameter));
                break;
            case numSpectra:
                setNumSpectra(Integer.parseInt(parameter));
                break;
            case accession:
                setAccession(parameter);
                break;
            case title:
                setTitle(parameter);
                break;
            case projectDescription:
                setProjectDescription(parameter);
                break;
            case numAssays:
                setNumAssays(Integer.parseInt(parameter));
                break;
            case submissionType:
                setSubmissionType(parameter);
                break;
            case publicationDate:
                setPublicationDate(getParameterDate(parameter));
                break;
            case projectTags:
                setProjectTags(getParameterArray(parameter));
                break;
            case tissues:
                setTissues(getParameterArray(parameter));
                break;
            case ptmNames:
                setPtmNames(getParameterArray(parameter));
                break;
            case species:
                setSpecies(getParameterArray(parameter));
                break;
            case instrumentNames:
                setInstrumentNames(getParameterArray(parameter));
                break;
        }
    }

    private String[] getParameterArray(String parameter) {
        return parameter.replace("[", "").replace("]", "").replace("\"", "").split(",");
    }

    private Date getParameterDate(String parameter) {
        Date date = new Date();
        try {
            date = sdf.parse(parameter);
        } catch (ParseException ex) {
            //could not parse date !
        }
        return date;
    }

    public PrideProject() {

    }

    public int getNumPeptides() {
        return numPeptides;
    }

    private void setNumPeptides(int numPeptides) {
        this.numPeptides = numPeptides;
    }

    public int getNumIdentifiedSpectra() {
        return numIdentifiedSpectra;
    }

    private void setNumIdentifiedSpectra(int numIdentifiedSpectra) {
        this.numIdentifiedSpectra = numIdentifiedSpectra;
    }

    public int getNumUniquePeptides() {
        return numUniquePeptides;
    }

    private void setNumUniquePeptides(int numUniquePeptides) {
        this.numUniquePeptides = numUniquePeptides;
    }

    public String[] getKeywords() {
        return keywords;
    }

    private void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }

    public String getDoi() {
        return doi;
    }

    private void setDoi(String doi) {
        this.doi = doi;
    }

    public String getReanalysis() {
        return reanalysis;
    }

    private void setReanalysis(String reanalysis) {
        this.reanalysis = reanalysis;
    }

    public String[] getExperimentTypes() {
        return experimentTypes;
    }

    private void setExperimentTypes(String[] experimentTypes) {
        this.experimentTypes = experimentTypes;
    }

    public Date getSubmissionDate() {
        return submissionDate;
    }

    private void setSubmissionDate(Date submissionDate) {
        this.submissionDate = submissionDate;
    }

    public String getSampleProcessingProtocol() {
        return sampleProcessingProtocol;
    }

    private void setSampleProcessingProtocol(String sampleProcessingProtocol) {
        this.sampleProcessingProtocol = sampleProcessingProtocol;
    }

    public String getDataProcessingProtocol() {
        return dataProcessingProtocol;
    }

    private void setDataProcessingProtocol(String dataProcessingProtocol) {
        this.dataProcessingProtocol = dataProcessingProtocol;
    }

    public String getOtherOmicsLink() {
        return otherOmicsLink;
    }

    private void setOtherOmicsLink(String otherOmicsLink) {
        this.otherOmicsLink = otherOmicsLink;
    }

    public String[] getQuantificationMethods() {
        return quantificationMethods;
    }

    private void setQuantificationMethods(String[] quantificationMethods) {
        this.quantificationMethods = quantificationMethods;
    }

    public int getNumProteins() {
        return numProteins;
    }

    private void setNumProteins(int numProteins) {
        this.numProteins = numProteins;
    }

    public int getNumSpectra() {
        return numSpectra;
    }

    private void setNumSpectra(int numSpectra) {
        this.numSpectra = numSpectra;
    }

    public String getAccession() {
        return accession;
    }

    private void setAccession(String accession) {
        this.accession = accession;
    }

    public String getTitle() {
        return title;
    }

    private void setTitle(String title) {
        this.title = title;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    private void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public int getNumAssays() {
        return numAssays;
    }

    private void setNumAssays(int numAssays) {
        this.numAssays = numAssays;
    }

    public String getSubmissionType() {
        return submissionType;
    }

    private void setSubmissionType(String submissionType) {
        this.submissionType = submissionType;
    }

    public Date getPublicationDate() {
        return publicationDate;
    }

    private void setPublicationDate(Date publicationDate) {
        this.publicationDate = publicationDate;
    }

    public String[] getProjectTags() {
        return projectTags;
    }

    private void setProjectTags(String[] projectTags) {
        this.projectTags = projectTags;
    }

    public String[] getTissues() {
        return tissues;
    }

    private void setTissues(String[] tissues) {
        this.tissues = tissues;
    }

    public String[] getPtmNames() {
        return ptmNames;
    }

    private void setPtmNames(String[] ptmNames) {
        this.ptmNames = ptmNames;
    }

    public String[] getSpecies() {
        return species;
    }

    private void setSpecies(String[] species) {
        this.species = species;
    }

    public String[] getInstrumentNames() {
        return instrumentNames;
    }

    private void setInstrumentNames(String[] instrumentNames) {
        this.instrumentNames = instrumentNames;
    }

    public void setSubmitter(PrideContactDetail submitter) {
        this.submitter = submitter;
    }

    public void addLabHead(PrideContactDetail contact) {
        labHeads.add(contact);
    }

    public PrideContactDetail getSubmitter() {
        return submitter;
    }

    public ArrayList<PrideContactDetail> getLabHeads() {
        return labHeads;
    }

}
