/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.logic.modification;

import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.util.preferences.ModificationProfile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.geneontology.oboedit.dataadapter.GOBOParseException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.xmlpull.v1.XmlPullParserException;
import uk.ac.ebi.pride.jaxb.model.ModificationItem;

/**
 *
 * @author Kenneth
 */
public class PTMMapperTest {

    public PTMMapperTest() {
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

    /**
     * Test of lookupRealModName method, of class PTMMapper.
     */
    @Test
    public void getInstance() throws XmlPullParserException, IOException, GOBOParseException {
        System.out.println("Test constructing synonym mapping");
        PTMMapper instance = PTMMapper.getInstance();
        HashMap<String, String> synonymMapping = instance.getSynonymMapping();
        for (String aMod : synonymMapping.keySet()) {
            System.out.println(aMod + " = " + synonymMapping.get(aMod));
        }
        assertFalse(synonymMapping.isEmpty());
    }

}
