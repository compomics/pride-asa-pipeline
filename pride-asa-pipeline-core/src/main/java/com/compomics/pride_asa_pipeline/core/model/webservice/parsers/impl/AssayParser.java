/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.model.webservice.parsers.impl;

import com.compomics.pride_asa_pipeline.core.model.webservice.fields.AssayField;
import com.compomics.pride_asa_pipeline.core.model.webservice.fields.AssayFileField;
import com.compomics.pride_asa_pipeline.core.model.webservice.objects.PrideAssay;
import com.compomics.pride_asa_pipeline.core.model.webservice.objects.PrideAssayFile;
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
public class AssayParser extends PrideJsonParser {

    /**
     *
     * @param assayAccession
     * @return the assay object containing all metadata
     * @throws MalformedURLException
     * @throws IOException
     * @throws ParseException
     */
    public PrideAssay getAssay(String assayAccession) throws MalformedURLException, IOException, ParseException {
        URL assayURL = new URL("http://www.ebi.ac.uk:80/pride/ws/archive/assay/" + assayAccession);
        return getAssayFromJson(getObjectFromURL(assayURL));
    }

    protected PrideAssay getAssayFromJson(JSONObject assayJson) throws IOException, MalformedURLException, ParseException {
        PrideAssay assay = new PrideAssay();
        for (AssayField aField : AssayField.values()) {
            Object parameter = assayJson.get(aField.toString());
            if (parameter != null) {
                assay.setField(aField, parameter.toString());
            }
        }
        assay.addAssayFiles(getAllAssayFiles(assay.getAssayAccession()));
        return assay;
    }

    /**
     *
     * @param assayID
     * @return a list of all files associated with this assay_ID
     * @throws MalformedURLException
     * @throws IOException
     * @throws ParseException
     */
    public List<PrideAssayFile> getAllAssayFiles(String assayID) throws MalformedURLException, IOException, ParseException {
        JSONObject jsonObject = getObjectFromURL(new URL("http://www.ebi.ac.uk:80/pride/ws/archive/file/list/assay/" + assayID));
        List<JSONObject> list = (ArrayList<JSONObject>) jsonObject.get("list");
        List<PrideAssayFile> files = new ArrayList<>();
        for (JSONObject assayFileJSON : list) {
            PrideAssayFile assayFile = new PrideAssayFile();
            for (AssayFileField aField : AssayFileField.values()) {
                Object parameter = assayFileJSON.get(aField.toString());
                if (parameter != null) {
                    assayFile.setField(aField, parameter.toString());
                }
            }
            files.add(assayFile);
        }
        return files;
    }

}
