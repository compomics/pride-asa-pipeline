/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.model.webservice.objects;

import com.compomics.pride_asa_pipeline.core.model.webservice.fields.ProteinField;

/**
 *
 * @author Kenneth
 */
public class PrideProtein {

    //standard fields
    private String[] description;
    private String sequence;
    private String[] synonyms;
    private String assayAccession;
    private String projectAccession;
    private String accession;
    private String sequenceType;

    public void setField(ProteinField field, String parameter) {
        if (parameter == null) {
            return;
        }
        switch (field) {
            case accession:
                setAccession(parameter);
                break;
            case assayAccession:
                setAccession(parameter);
                break;
            case projectAccession:
                setAccession(parameter);
                break;
            case description:
                setDescription(getParameterArray(parameter));
                break;
            case sequence:
                setSequence(parameter);
                break;
            case sequenceType:
                setSequenceType(parameter);
                break;
            case synonyms:
                setSynonyms(getParameterArray(parameter));
                break;
        }
    }

    private String[] getParameterArray(String parameter) {
        return parameter.replace("[", "").replace("]", "").replace("\"", "").split(",");
    }

    public String[] getDescription() {
        return description;
    }

    public void setDescription(String[] description) {
        this.description = description;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String[] getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(String[] synonyms) {
        this.synonyms = synonyms;
    }

    public String getAssayAccession() {
        return assayAccession;
    }

    public void setAssayAccession(String assayAccession) {
        this.assayAccession = assayAccession;
    }

    public String getProjectAccession() {
        return projectAccession;
    }

    public void setProjectAccession(String projectAccession) {
        this.projectAccession = projectAccession;
    }

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public String getSequenceType() {
        return sequenceType;
    }

    public void setSequenceType(String sequenceType) {
        this.sequenceType = sequenceType;
    }

}
