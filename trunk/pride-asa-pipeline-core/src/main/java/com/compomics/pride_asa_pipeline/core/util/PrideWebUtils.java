/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.util;

import com.compomics.pride_asa_pipeline.core.model.pride.web.impl.PrideAssay;
import com.compomics.pride_asa_pipeline.core.model.pride.web.impl.PrideAssayFile;
import com.compomics.pride_asa_pipeline.core.model.pride.web.impl.PrideContactDetail;
import com.compomics.pride_asa_pipeline.core.model.pride.web.impl.PrideProject;
import com.compomics.pride_asa_pipeline.core.model.pride.web.json.AssayField;
import com.compomics.pride_asa_pipeline.core.model.pride.web.json.AssayFileField;
import com.compomics.pride_asa_pipeline.core.model.pride.web.json.ContactField;
import com.compomics.pride_asa_pipeline.core.model.pride.web.json.PrideFileType;
import com.compomics.pride_asa_pipeline.core.model.pride.web.json.PrideFilter;
import com.compomics.pride_asa_pipeline.core.model.pride.web.json.ProjectField;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Kenneth
 */
public class PrideWebUtils {

    private final JSONParser jsonParser = new JSONParser();
    private static final int BUFFER_SIZE = 4096;

    public PrideProject getProjectField(String projectAccession, ProjectField aField) throws IOException, ParseException {
        System.out.println("Loading " + projectAccession);
        URL projectURL = new URL("http://www.ebi.ac.uk:80/pride/ws/archive/project/" + projectAccession);
        JSONObject projectObject = getObjectFromURL(projectURL);
        PrideProject prideProject = new PrideProject();
        Object parameter = projectObject.get(aField.toString());
        prideProject.setField(aField, parameter.toString());
        return prideProject;
    }

    public PrideProject getPrideProject(String projectAccession) throws IOException, ParseException {
        URL projectURL = new URL("http://www.ebi.ac.uk:80/pride/ws/archive/project/" + projectAccession);
        JSONObject projectObject = getObjectFromURL(projectURL);
        PrideProject prideProject = getProject(projectObject);
        //submitter
        PrideContactDetail submitter = getContact((JSONObject) projectObject.get("submitter"));
        prideProject.setSubmitter(submitter);
        //contacts
        List<JSONObject> labHeads = (ArrayList<JSONObject>) projectObject.get("labHeads");

        for (JSONObject anObject : labHeads) {
            prideProject.addLabHead(getContact(anObject));
        }

        URL assayURL = new URL("http://www.ebi.ac.uk:80/pride/ws/archive/assay/list/project/" + projectAccession);
        JSONObject assayObject = getObjectFromURL(assayURL);
        //assays
        List<JSONObject> list = (ArrayList<JSONObject>) assayObject.get("list");
        for (JSONObject anObject : list) {
            PrideAssay assay = getAssay(anObject);
            prideProject.put(assay.getAssayAccession(), assay);
        }
        return prideProject;
    }

    public PrideAssay getPrideAssay(String assayAccession) throws MalformedURLException, IOException, ParseException {
        URL assayURL = new URL("http://www.ebi.ac.uk:80/pride/ws/archive/assay/" + assayAccession);
        return getAssay(getObjectFromURL(assayURL));
    }

    private PrideContactDetail getContact(JSONObject contactJson) {
        PrideContactDetail contact = new PrideContactDetail();
        for (ContactField aField : ContactField.values()) {
            Object parameter = contactJson.get(aField.toString());
            if (parameter != null) {
                contact.setField(aField, parameter.toString());
            }
        }
        return contact;
    }

    private PrideAssay getAssay(JSONObject assayJson) throws IOException, MalformedURLException, ParseException {
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

    private PrideProject getProject(JSONObject projectJSON) {
        PrideProject project = new PrideProject();
        for (ProjectField aField : ProjectField.values()) {
            Object parameter = projectJSON.get(aField.toString());
            if (parameter != null) {
                project.setField(aField, parameter.toString());
            }
        }
        return project;
    }

    private JSONObject getObjectFromURL(URL url) throws IOException, ParseException {
        return (JSONObject) jsonParser.parse(new InputStreamReader(url.openStream()));
    }

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

    public List<PrideAssayFile> getAllAssayFiles(String assayID, PrideFileType fileType) throws MalformedURLException, IOException, ParseException {
        List<PrideAssayFile> files = getAllAssayFiles(assayID);
        List<PrideAssayFile> filteredFiles = new ArrayList<>();
        for (PrideAssayFile assayFile : files) {
            if (assayFile.getFileType().equals(fileType)) {
                filteredFiles.add(assayFile);
            }
        }
        return filteredFiles;
    }

    public List<PrideProject> getProjects(Collection<PrideFilter> filters, boolean quickLoad) throws MalformedURLException, IOException, ParseException {
        ArrayList<PrideProject> projects = new ArrayList<>();
        String basicURL = "http://www.ebi.ac.uk:80/pride/ws/archive/project/list?show=99999999";
        for (PrideFilter aFilter : filters) {
            basicURL = basicURL + "&" + aFilter.getType().toString() + "=" + aFilter.getValue();
        }
        URL queryURL = new URL(basicURL);
        //assays
        JSONObject objectFromURL = getObjectFromURL(queryURL);
        List<JSONObject> list = (ArrayList<JSONObject>) objectFromURL.get("list");
        for (JSONObject anObject : list) {
            if (quickLoad) {
                projects.add(getProject(anObject));
            } else {
                projects.add(getPrideProject(getProject(anObject).getAccession()));
            }
        }
        return projects;
    }

}
