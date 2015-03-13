/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.utils;

import com.compomics.pride_asa_pipeline.core.model.webservice.objects.PrideAssay;
import com.compomics.pride_asa_pipeline.core.model.webservice.objects.PrideAssayFile;
import com.compomics.pride_asa_pipeline.core.model.webservice.objects.PridePeptide;
import com.compomics.pride_asa_pipeline.core.model.webservice.objects.PrideProject;
import com.compomics.pride_asa_pipeline.core.model.webservice.objects.PrideProtein;
import com.compomics.pride_asa_pipeline.core.util.PrideMetadataUtils;
import java.util.List;
import static junit.framework.Assert.assertEquals;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Kenneth
 */
public class PrideMetadataUtilsTest {

    public PrideMetadataUtilsTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    private static final PrideMetadataUtils utils = PrideMetadataUtils.getInstance();

    @Test
    public void testGetProject() throws Exception {
        System.out.println("getProject");
        String projectAccession = "PRD000001";
        PrideProject result = utils.getProject(projectAccession);
        assertEquals("PRD000001", result.getAccession());
        assertEquals(5, result.getNumAssays());
        assertEquals(1530, result.getNumProteins());
        assertEquals(6758, result.getNumPeptides());
        assertEquals(17400, result.getNumSpectra());
        assertEquals(2685, result.getNumUniquePeptides());
        assertEquals(6758, result.getNumIdentifiedSpectra());
        assertEquals("instrument model", result.getInstrumentNames()[0]);
        assertEquals("Waters instrument model", result.getInstrumentNames()[1]);
        assertEquals("Homo sapiens (Human)", result.getSpecies()[0]);
        assertEquals("COFRADIC proteome of unstimulated human blood platelets", result.getTitle());
    }

    @Test
    public void testGetAssay() throws Exception {
        System.out.println("getAssay");
        String assayAccession = "3";
        PrideAssay result = utils.getAssay(assayAccession);
        assertEquals("3", result.getAssayAccession());
        assertEquals("Not available", result.getExperimentalFactor());
        assertEquals(1958, result.getIdentifiedSpectrumCount());
        assertEquals(1958, result.getPeptideCount());
        assertEquals(345, result.getProteinCount());
        assertEquals("PRD000001", result.getProjectAccession());
        assertEquals("COFRADIC N-terminal proteome of unstimulated human blood platelets", result.getTitle());
    }

    @Test
    public void testGetAllAssayFiles() throws Exception {
        System.out.println("getAllAssayFiles");
        String assayAccession = "3";
        List<PrideAssayFile> result = utils.getAllAssayFiles(assayAccession);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetProteins() throws Exception {
        System.out.println("getProteins");
        String assayAccession = "3";
        List<PrideProtein> result = utils.getAllProteins(assayAccession);
        assertEquals(345, result.size());
    }

    @Test
    public void testGetPeptides() throws Exception {
        System.out.println("getPeptides");
        String assayAccession = "3";
        List<PridePeptide> result = utils.getAllPeptides(assayAccession);
        assertEquals(1958, result.size());
    }

}
