package com.compomics.pride_asa_pipeline.core.model.modification.impl;

import com.compomics.pride_asa_pipeline.core.exceptions.ParameterExtractionException;
import com.compomics.pride_asa_pipeline.core.model.modification.PRIDEModification;
import com.compomics.pride_asa_pipeline.core.model.modification.source.PRIDEModificationFactory;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.biology.PTM;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author compomics
 */
public class UtilitiesPTMAdapterTest {

    private UtilitiesPTMAdapter adapter;
    private ArrayList<String> ptmList;

    public UtilitiesPTMAdapterTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        adapter = new UtilitiesPTMAdapter();
        ptmList = new ArrayList<>();
       // ptmList.add("1-thioglycine (C-terminal)");
      ptmList.add("L-3',4',5'-trihydroxyphenylalanine");
     //   ptmList.add("S-geranylgeranyl-L-cysteine methyl ester");
        ptmList.add("Cation:Na");
        ptmList.add("Biotin");
        ptmList.add("Acetyl");
        ptmList.add("Carbamyl");
        ptmList.add("N6-(11-cis)-retinylidene-L-lysine");
        ptmList.add("N2-formyl-L-tryptophan");
        ptmList.add("acetylated L-cysteine");
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of testPTM method, of class UtilitiesPTMAdapter.
     *
     * @throws
     * com.compomics.pride_asa_pipeline.core.exceptions.ParameterExtractionException
     * when a modification is not interpretable
     */
    @Test
    public void testPTMConversion() throws ParameterExtractionException {
        for (String ptm : ptmList) {
            System.out.println("Testing ptm : " + ptm);
            assertTrue(testPTM(ptm));
        }
    }

    private boolean testPTM(String testMod) throws ParameterExtractionException {
        PRIDEModificationFactory instance = PRIDEModificationFactory.getInstance();
        LinkedHashMap<String, PRIDEModification> modificationMap = instance.getModificationMap();
        PRIDEModification get = modificationMap.get(testMod);
        System.out.println(get.getFormula());
        PTM convertModification;
        convertModification = adapter.convertModification(modificationMap.get(testMod));
        double addedMass = convertModification.getAtomChainAdded().getMass();
        double removedMass = convertModification.getAtomChainRemoved().getMass();
        double delta = Math.abs((addedMass + removedMass) - get.getMonoDeltaMass());
        System.out.println(delta);
        return delta < 1;
    }
}
