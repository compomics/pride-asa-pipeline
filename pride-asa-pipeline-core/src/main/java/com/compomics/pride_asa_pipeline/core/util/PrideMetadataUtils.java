/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.util;

import com.compomics.pride_asa_pipeline.core.model.webservice.PrideFilter;
import com.compomics.pride_asa_pipeline.core.model.webservice.fields.PrideFileType;
import com.compomics.pride_asa_pipeline.core.model.webservice.objects.PrideAssay;
import com.compomics.pride_asa_pipeline.core.model.webservice.objects.PrideAssayFile;
import com.compomics.pride_asa_pipeline.core.model.webservice.objects.PridePeptide;
import com.compomics.pride_asa_pipeline.core.model.webservice.objects.PrideProject;
import com.compomics.pride_asa_pipeline.core.model.webservice.objects.PrideProtein;
import com.compomics.pride_asa_pipeline.core.model.webservice.parsers.impl.AssayParser;
import com.compomics.pride_asa_pipeline.core.model.webservice.parsers.impl.PeptideParser;
import com.compomics.pride_asa_pipeline.core.model.webservice.parsers.impl.ProjectParser;
import com.compomics.pride_asa_pipeline.core.model.webservice.parsers.impl.ProteinParser;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Kenneth
 */
public class PrideMetadataUtils {

    private static final Logger LOGGER = Logger.getLogger(PrideMetadataUtils.class);

    private static PrideMetadataUtils service;

    private PrideMetadataUtils() {

    }

    /**
     *
     * @return a pridemedata service instance
     */
    public static PrideMetadataUtils getInstance() {
        if (service == null) {
            service = new PrideMetadataUtils();
        }
        return service;
    }

    // PROJECTS
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
    private List<PrideProject> getProjects(ProjectParser parser, Collection<PrideFilter> filters, boolean loadAssays) throws MalformedURLException, IOException, ParseException {
        return parser.getProjects(filters, loadAssays);
    }

    // PROJECTS
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
        return getProjects(new ProjectParser(), filters, loadAssays);
    }

    // PROJECTS
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
    public List<PrideProject> getProjects(String query, Collection<PrideFilter> filters, boolean loadAssays) throws MalformedURLException, IOException, ParseException {
        return getProjects(new ProjectParser(query), filters, loadAssays);
    }

    // PROJECTS
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
    private List<PrideProject> getProjects(ProjectParser parser, Collection<PrideFilter> filters, boolean loadAssays, int cacheSize) throws MalformedURLException, IOException, ParseException {
        return parser.getProjects(filters, loadAssays, cacheSize);
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
    public List<PrideProject> getProjects(String query, Collection<PrideFilter> filters, boolean loadAssays, int cacheSize) throws MalformedURLException, IOException, ParseException {
        ProjectParser parser = new ProjectParser(query);
        return getProjects(parser, filters, loadAssays, cacheSize);
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
        return getProjects(new ProjectParser(), filters, loadAssays, cacheSize);
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
    private List<PrideProject> getProjects(ProjectParser parser, PrideFilter filter, boolean loadAssays) throws MalformedURLException, IOException, ParseException {
        return parser.getProjects(filter, loadAssays);
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
    public List<PrideProject> getProjects(String query, PrideFilter filter, boolean loadAssays) throws MalformedURLException, IOException, ParseException {
        ProjectParser parser = new ProjectParser(query);
        return getProjects(parser, filter, loadAssays);
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
        ProjectParser parser = new ProjectParser();
        return getProjects(parser, filter, loadAssays);
    }

    /**
     *
     * @param projectAccession
     * @return the full project, complete with assays
     * @throws java.net.MalformedURLException
     * @throws IOException
     * @throws ParseException
     */
    public PrideProject getProject(String projectAccession) throws MalformedURLException, IOException, ParseException {
        ProjectParser parser = new ProjectParser();
        return parser.getProject(projectAccession);
    }

    // ASSAYS
    /**
     *
     * @param assayAccession
     * @return the assay object containing all metadata
     * @throws MalformedURLException
     * @throws IOException
     * @throws ParseException
     */
    public PrideAssay getAssay(String assayAccession) throws MalformedURLException, IOException, ParseException {
        AssayParser parser = new AssayParser();
        PrideAssay prideAssay = parser.getAssay(assayAccession);
        return prideAssay;
    }

    /**
     *
     * @param assayAccession
     * @return a list of all files associated with this assay_ID
     * @throws MalformedURLException
     * @throws IOException
     * @throws ParseException
     */
    public List<PrideAssayFile> getAllAssayFiles(String assayAccession) throws MalformedURLException, IOException, ParseException {
        AssayParser parser = new AssayParser();
        List<PrideAssayFile> allAssayFiles = parser.getAllAssayFiles(assayAccession);
        return allAssayFiles;
    }

    /**
     *
     * @param assayAccession
     * @param fileType
     * @return a list of all files associated with this assay_ID and have the
     * specified filetype
     * @throws MalformedURLException
     * @throws IOException
     * @throws ParseException
     */
    public List<PrideAssayFile> getAllAssayFiles(String assayAccession, PrideFileType fileType) throws MalformedURLException, IOException, ParseException {
        List<PrideAssayFile> files = getAllAssayFiles(assayAccession);
        List<PrideAssayFile> filteredFiles = new ArrayList<>();
        for (PrideAssayFile assayFile : files) {
            if (assayFile.getFileType().equals(fileType)) {
                filteredFiles.add(assayFile);
            }
        }
        return filteredFiles;
    }

    // PROTEINS
    /**
     *
     * @param assay
     * @return a list of all proteins (if available) for the given assay
     * @throws MalformedURLException
     * @throws IOException
     * @throws ParseException
     */
    public List<PrideProtein> getAllProteins(PrideAssay assay) throws MalformedURLException, IOException, ParseException {
        ProteinParser parser = new ProteinParser();
        List<PrideProtein> prideProteins = parser.getAllProteins(assay);
        return prideProteins;
    }

    /**
     *
     * @param assayAccession
     * @return a list of all proteins (if available) for the given assay_ID
     * @throws MalformedURLException
     * @throws IOException
     * @throws ParseException
     */
    public List<PrideProtein> getAllProteins(String assayAccession) throws MalformedURLException, IOException, ParseException {
        ProteinParser parser = new ProteinParser();
        List<PrideProtein> prideProteins = parser.getAllProteins(assayAccession);
        return prideProteins;
    }

    // PEPTIDES
    /**
     *
     * @param assay
     * @return a list of all peptides (if available) for the given assay
     * @throws MalformedURLException
     * @throws IOException
     * @throws ParseException
     */
    public List<PridePeptide> getAllPeptides(PrideAssay assay) throws MalformedURLException, IOException, ParseException {
        PeptideParser parser = new PeptideParser();
        List<PridePeptide> pridePeptides = parser.getAllPeptides(assay);
        return pridePeptides;
    }

    /**
     *
     * @param assayAccession
     * @return a list of all peptides (if available) for the given assay_ID
     * @throws MalformedURLException
     * @throws IOException
     * @throws ParseException
     */
    public List<PridePeptide> getAllPeptides(String assayAccession) throws MalformedURLException, IOException, ParseException {
        PeptideParser parser = new PeptideParser();
        List<PridePeptide> pridePeptides = parser.getAllPeptides(assayAccession);
        return pridePeptides;
    }

}
