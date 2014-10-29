/*
 *

 */
package com.compomics.pride_asa_pipeline.core.logic;

import com.compomics.pride_asa_pipeline.core.model.ModificationHolder;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.core.model.MassRecalibrationResult;
import com.compomics.pride_asa_pipeline.model.AminoAcidSequence;
import com.compomics.pride_asa_pipeline.model.UnknownAAException;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.model.Constants;
import com.compomics.pride_asa_pipeline.model.AASequenceMassUnknownException;
import com.compomics.pride_asa_pipeline.core.model.ModificationCombination;
import com.compomics.pride_asa_pipeline.core.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.core.logic.impl.MassDeltaExplainerImpl;
import com.compomics.pride_asa_pipeline.core.service.PipelineModificationService;
import com.compomics.pride_asa_pipeline.core.util.ResourceUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static junit.framework.Assert.*;
import org.jdom2.JDOMException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Niels Hulstaert Hulstaert
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:springXMLConfig.xml")
public class MassDeltaExplainerTest {

    private static final double MASS_ERROR = -2;
    private MassDeltaExplainer massDeltaExplainer;
    @Autowired
    private PipelineModificationService modificationService;
    private MassRecalibrationResult massRecalibrationResult;

    @Before
    public void initialize() throws IOException, JDOMException {
        //add the pipeline modifications
        ModificationHolder modificationHolder = new ModificationHolder();
        Resource modificationsResource = ResourceUtils.getResourceByRelativePath(PropertiesConfigurationHolder.getInstance().getString("modification.pipeline_modifications_file"));
        modificationHolder.addModifications(modificationService.loadPipelineModifications(modificationsResource));

        massDeltaExplainer = new MassDeltaExplainerImpl(modificationHolder);

        //init MassRecalibrationResult
        massRecalibrationResult = new MassRecalibrationResult();
        massRecalibrationResult.addMassError(1, MASS_ERROR, 0.5);
    }

    @Test
    public void testExplainCompleteIndentifications() throws UnknownAAException, AASequenceMassUnknownException {
        List<Identification> identifications = new ArrayList<>();

        //add identifications
        //the experimental mass was chosen so some of the identifications mass delta could be explained with modification(s)
        //loaded from the pride_asap_modifications.xml in the test resources
        AminoAcidSequence aminoAcidSequence = new AminoAcidSequence("AAKENNYLENNART");
        double experimentalMass = aminoAcidSequence.getSequenceMass() + Constants.MASS_H2O + Constants.MASS_H + MASS_ERROR + 42.01056;
        Peptide peptide = new Peptide(1, experimentalMass, aminoAcidSequence);
        Identification identification_1 = new Identification(peptide, "1", "0", "0");
        identifications.add(identification_1);
        experimentalMass = aminoAcidSequence.getSequenceMass() + Constants.MASS_H2O + Constants.MASS_H + MASS_ERROR + 57.02;
        peptide = new Peptide(1, experimentalMass, aminoAcidSequence);
        Identification identification_2 = new Identification(peptide, "2", "0", "0");
        identifications.add(identification_2);
        experimentalMass = aminoAcidSequence.getSequenceMass() + Constants.MASS_H2O + Constants.MASS_H + MASS_ERROR + 15.994915;
        peptide = new Peptide(1, experimentalMass, aminoAcidSequence);
        Identification identification_3 = new Identification(peptide, "3", "0", "0");
        identifications.add(identification_3);
        experimentalMass = aminoAcidSequence.getSequenceMass() + Constants.MASS_H2O + Constants.MASS_H + MASS_ERROR + 15.994915 + 57.02146;
        peptide = new Peptide(1, experimentalMass, aminoAcidSequence);
        Identification identification_4 = new Identification(peptide, "4", "0", "0");
        identifications.add(identification_4);
        experimentalMass = aminoAcidSequence.getSequenceMass() + Constants.MASS_H2O + Constants.MASS_H + 57.02;
        peptide = new Peptide(1, experimentalMass, aminoAcidSequence);
        Identification identification_5 = new Identification(peptide, "5", "0", "0");
        identifications.add(identification_5);
        experimentalMass = aminoAcidSequence.getSequenceMass() + Constants.MASS_H2O + Constants.MASS_H + MASS_ERROR;
        peptide = new Peptide(1, experimentalMass, aminoAcidSequence);
        Identification identification_6 = new Identification(peptide, "6", "0", "0");
        identifications.add(identification_6);
        experimentalMass = aminoAcidSequence.getSequenceMass() + Constants.MASS_H2O + Constants.MASS_H + MASS_ERROR - 35.0;
        peptide = new Peptide(1, experimentalMass, aminoAcidSequence);
        Identification identification_7 = new Identification(peptide, "7", "0", "0");
        identifications.add(identification_7);
        experimentalMass = aminoAcidSequence.getSequenceMass() + Constants.MASS_H2O + Constants.MASS_H + MASS_ERROR - 48.0;
        peptide = new Peptide(1, experimentalMass, aminoAcidSequence);
        Identification identification_8 = new Identification(peptide, "8", "0", "0");
        identifications.add(identification_8);

        Map<Identification, Set<ModificationCombination>> explanations = massDeltaExplainer.explainCompleteIndentifications(identifications, massRecalibrationResult, null);

        //the mass deltas of 6 identifications can be explained,
        //so the the size of the map should be equal to 6
        assertEquals(6, explanations.size());
        //check if the modification combination sets are not empty
        assertFalse(explanations.get(identification_1).isEmpty());
        assertFalse(explanations.get(identification_2).isEmpty());
        assertFalse(explanations.get(identification_3).isEmpty());
        assertFalse(explanations.get(identification_4).isEmpty());
        assertNull(explanations.get(identification_5));
        assertTrue(explanations.containsKey(identification_6));
        assertFalse(explanations.get(identification_7).isEmpty());
        assertNull(explanations.get(identification_8));

        //the first explanation should contain 2 modification combinations, with 1 modification each
        assertEquals(2, explanations.get(identification_1).size());
        Iterator<ModificationCombination> iterator = explanations.get(identification_1).iterator();
        assertEquals(1, iterator.next().getSize());
        assertEquals(1, iterator.next().getSize());
        //the second explanation should contain 1 modification combination, with 1 modification
        assertEquals(1, explanations.get(identification_2).size());
        assertEquals(1, explanations.get(identification_2).iterator().next().getSize());
        //the third explanation should contain 1 modification combination, with 1 modification
        assertEquals(1, explanations.get(identification_3).size());
        assertEquals(1, explanations.get(identification_3).iterator().next().getSize());
        //the fourth explanation should contain 1 modification combination, with 2 modifications
        assertEquals(1, explanations.get(identification_4).size());
        assertEquals(2, explanations.get(identification_4).iterator().next().getSize());
        //the seventh explanation should contain 1 modification combination, with 1 modification
        assertEquals(1, explanations.get(identification_7).size());
        assertEquals(1, explanations.get(identification_7).iterator().next().getSize());
    }
}
