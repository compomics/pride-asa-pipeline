/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.model.webservice.parsers.impl;

import com.compomics.pride_asa_pipeline.core.model.webservice.PrideFilter;
import com.compomics.pride_asa_pipeline.core.model.webservice.fields.ContactField;
import com.compomics.pride_asa_pipeline.core.model.webservice.fields.ProjectField;
import com.compomics.pride_asa_pipeline.core.model.webservice.objects.PrideAssay;
import com.compomics.pride_asa_pipeline.core.model.webservice.objects.PrideContactDetail;
import com.compomics.pride_asa_pipeline.core.model.webservice.objects.PrideProject;
import com.compomics.pride_asa_pipeline.core.model.webservice.parsers.PrideJsonParser;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Kenneth
 */
public class ProjectParser extends PrideJsonParser {

    private static final Logger LOGGER = Logger.getLogger(ProjectParser.class);
    private final String baseQueryAddress = "http://www.ebi.ac.uk:80/pride/ws/archive/project/list?";
    private static final int cacheSize = 10;
    private final String query;
    private final String queryAddress;

    public ProjectParser(String query) {
        this.query = "query=" + query.replace(" ", "%20");
        queryAddress = baseQueryAddress + this.query + "&show=";
    }

    public ProjectParser() {
        this.query = "";
        queryAddress = baseQueryAddress + "&show=";
    }

    /**
     *
     * @param filters a collection of filters for the projects
     * @param loadAssays TRUE = load assay information as well (might take
     * longer)
     * @return
     * @throws MalformedURLException
     * @throws IOException
     * @throws ParseException
     */
    public List<PrideProject> getProjects(Collection<PrideFilter> filters, boolean loadAssays) throws MalformedURLException, IOException, ParseException {
        String basicURL = queryAddress + cacheSize;
        for (PrideFilter aFilter : filters) {
            basicURL += "&" + aFilter.getType().toString() + "=" + aFilter.getValue();
        }
        return getProjectsFromURL(basicURL, loadAssays);
    }

    /**
     *
     * @param filters a collection of filters for the projects
     * @param loadAssays TRUE = load assay information as well (might take
     * longer)
     * @return
     * @throws MalformedURLException
     * @throws IOException
     * @throws ParseException
     */
    public List<PrideProject> getProjects(Collection<PrideFilter> filters, boolean loadAssays, int cacheSize) throws MalformedURLException, IOException, ParseException {
        String basicURL = queryAddress + cacheSize;
        for (PrideFilter aFilter : filters) {
            basicURL += "&" + aFilter.getType().toString() + "=" + aFilter.getValue();
        }
        return getProjectsFromURL(basicURL, loadAssays);
    }

    /**
     *
     * @param filter a filter for the projects
     * @param loadAssays TRUE = load assay information as well (might take
     * longer)
     * @return
     * @throws MalformedURLException
     * @throws IOException
     * @throws ParseException
     */
    public List<PrideProject> getProjects(PrideFilter filter, boolean loadAssays) throws MalformedURLException, IOException, ParseException {
        String basicURL = queryAddress + "&" + filter.getType().toString() + "=" + filter.getValue();
        return getProjectsFromURL(basicURL, loadAssays);
    }

    private List<PrideProject> getProjectsFromURL(String queryURLAsString, boolean loadAssays) throws MalformedURLException, IOException, ParseException {
        //assays
        ArrayList<PrideProject> projects = new ArrayList<>();
        //buffer the results
        boolean moreResults = true;
        int page = 0;
        while (moreResults) {
            URL pagedURL = new URL(queryURLAsString + "&page=" + page);
            JSONObject objectFromURL = getObjectFromURL(pagedURL);
            List<JSONObject> list = (ArrayList<JSONObject>) objectFromURL.get("list");
            if (list.isEmpty()) {
                moreResults = false;
            } else {
                for (JSONObject anObject : list) {
                    try {
                        if (loadAssays) {
                            projects.add(getProject(getProjectFromJson(anObject).getAccession()));
                        } else {
                            projects.add(getProjectFromJson(anObject));
                        }
                    } catch (IOException e) {
                        LOGGER.error("Could not parse " + anObject.toJSONString());
                    }
                }
                LOGGER.debug("Recieved results for page " + page + " ( " + projects.size() + " projects discovered)");
                page++;
            }
        }
        return projects;
    }

    private PrideProject getProjectFromJson(JSONObject projectJSON) {
        PrideProject project = new PrideProject();
        for (ProjectField aField : ProjectField.values()) {
            Object parameter = projectJSON.get(aField.toString());
            if (parameter != null) {
                project.setField(aField, parameter.toString());
            }
        }
        return project;
    }

    /**
     *
     * @param projectAccession
     * @return the full project, complete with assays
     * @throws IOException
     * @throws ParseException
     */
    public PrideProject getProject(String projectAccession) throws IOException, ParseException {
        String query = "http://www.ebi.ac.uk/pride/ws/archive/project/" + projectAccession;
        URL projectURL = new URL(query);
        JSONObject projectObject = getObjectFromURL(projectURL);
        PrideProject prideProject = getProjectFromJson(projectObject);
        //submitter
        PrideContactDetail submitter = getContact((JSONObject) projectObject.get("submitter"));
        prideProject.setSubmitter(submitter);
        //contacts
        List<JSONObject> labHeads = (ArrayList<JSONObject>) projectObject.get("labHeads");

        for (JSONObject anObject : labHeads) {
            prideProject.addLabHead(getContact(anObject));
        }

        URL assayURL = new URL("http://www.ebi.ac.uk/pride/ws/archive/assay/list/project/" + projectAccession);
        JSONObject assayObject = getObjectFromURL(assayURL);
        //   System.out.println(assayObject);
        //assays
        List<JSONObject> list = (ArrayList<JSONObject>) assayObject.get("list");
        AssayParser assayParser = new AssayParser();
        for (JSONObject anObject : list) {
            PrideAssay assay = assayParser.getAssayFromJson(anObject);
            prideProject.put(assay.getAssayAccession(), assay);
        }
        return prideProject;
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

}
