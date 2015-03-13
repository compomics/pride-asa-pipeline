/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.model.webservice.parsers.impl;

import com.compomics.pride_asa_pipeline.core.model.webservice.fields.PeptideField;
import com.compomics.pride_asa_pipeline.core.model.webservice.objects.PrideAssay;
import com.compomics.pride_asa_pipeline.core.model.webservice.objects.PrideModifiedLocation;
import com.compomics.pride_asa_pipeline.core.model.webservice.objects.PridePeptide;
import com.compomics.pride_asa_pipeline.core.model.webservice.parsers.PrideJsonParser;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Kenneth
 */
public class PeptideParser extends PrideJsonParser {

    private PridePeptide getPeptide(JSONObject assayJson) throws IOException, MalformedURLException, ParseException {
        PridePeptide peptide = new PridePeptide();
        for (PeptideField aField : PeptideField.values()) {
            Object parameter = assayJson.get(aField.toString());
            if (parameter != null) {
                peptide.setField(aField, parameter.toString());
            }
        }
        //get the modifications
        JSONArray mods = (JSONArray) assayJson.get("modifications");
        for (Object aMod : mods) {
            JSONObject aModObject = (JSONObject) aMod;
            PrideModifiedLocation modLoc = new PrideModifiedLocation();
            modLoc.setLocation(Integer.parseInt(aModObject.get("location").toString()));
            modLoc.setModification(aModObject.get("modification").toString());
            peptide.addModification(modLoc);
        }

        //assay.addAssayFiles(getAllAssayFiles(assay.getAssayAccession()));
        return peptide;
    }

    /**
     *
     * @param assayAccession
     * @return a list of all peptides (if available) for the given assay
     * @throws MalformedURLException
     * @throws IOException
     * @throws ParseException
     */
    public List<PridePeptide> getAllPeptides(String assayAccession) throws MalformedURLException, IOException, ParseException {
        AssayParser parser = new AssayParser();
        return getAllPeptides(parser.getAssay(assayAccession));
    }

    /**
     *
     * @param assayAccession
     * @return a list of all peptides (if available) for the given assay
     * @throws MalformedURLException
     * @throws IOException
     * @throws ParseException
     */
    public List<PridePeptide> getAllPeptides(PrideAssay assay) throws MalformedURLException, IOException, ParseException {
        ArrayList<PridePeptide> peptides = new ArrayList<>();
        String basicURL = "http://www.ebi.ac.uk:80/pride/ws/archive/peptide/list/assay/" + assay.getAssayAccession() + "?show=" + assay.getPeptideCount();
        URL queryURL = new URL(basicURL);
        JSONObject objectFromURL = getObjectFromURL(queryURL);
        List<JSONObject> list = (ArrayList<JSONObject>) objectFromURL.get("list");
        for (JSONObject anObject : list) {
            peptides.add(getPeptide(anObject));
        }
        return peptides;
    }

}
