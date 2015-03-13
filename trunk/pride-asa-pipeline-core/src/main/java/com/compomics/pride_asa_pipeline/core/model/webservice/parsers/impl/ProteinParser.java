/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.model.webservice.parsers.impl;

import com.compomics.pride_asa_pipeline.core.model.webservice.fields.ProteinField;
import com.compomics.pride_asa_pipeline.core.model.webservice.objects.PrideAssay;
import com.compomics.pride_asa_pipeline.core.model.webservice.objects.PrideProtein;
import com.compomics.pride_asa_pipeline.core.model.webservice.parsers.PrideJsonParser;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Kenneth
 */
public class ProteinParser extends PrideJsonParser {

    private PrideProtein getProtein(JSONObject assayJson) throws IOException, MalformedURLException, ParseException {
        PrideProtein protein = new PrideProtein();
        for (ProteinField aField : ProteinField.values()) {
            Object parameter = assayJson.get(aField.toString());
            if (parameter != null) {
                protein.setField(aField, parameter.toString());
            }
        }
        return protein;
    }

    /**
     *
     * @param assayAccession
     * @return a list of all proteins (if available) for the given assay
     * @throws MalformedURLException
     * @throws IOException
     * @throws ParseException
     */
    public List<PrideProtein> getAllProteins(String assayAccession) throws MalformedURLException, IOException, ParseException {
        AssayParser assayParser = new AssayParser();
        return getAllProteins(assayParser.getAssay(assayAccession));
    }

    /**
     *
     * @param assayAccession
     * @return a list of all proteins (if available) for the given assay
     * @throws MalformedURLException
     * @throws IOException
     * @throws ParseException
     */
    public List<PrideProtein> getAllProteins(PrideAssay assay) throws MalformedURLException, IOException, ParseException {
        ArrayList<PrideProtein> proteins = new ArrayList<>();
        String basicURL = "http://www.ebi.ac.uk:80/pride/ws/archive/protein/list/assay/" + assay.getAssayAccession() + "?show=" + assay.getProteinCount();
        URL queryURL = new URL(basicURL);
        JSONObject objectFromURL = getObjectFromURL(queryURL);
        List<JSONObject> list = (ArrayList<JSONObject>) objectFromURL.get("list");
        for (JSONObject anObject : list) {
            proteins.add(getProtein(anObject));
        }
        return proteins;
    }

}
