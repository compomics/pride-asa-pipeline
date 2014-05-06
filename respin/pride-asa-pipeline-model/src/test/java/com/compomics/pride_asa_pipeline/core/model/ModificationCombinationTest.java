/*
 */
package com.compomics.pride_asa_pipeline.core.model;

import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.core.service.PipelineModificationService;
import com.compomics.pride_asa_pipeline.core.util.ResourceUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static junit.framework.Assert.*;
import org.jdom2.JDOMException;
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
public class ModificationCombinationTest {

    @Autowired
    private PipelineModificationService modificationService;

    @Test
    public void testEqualsAndHashCode() throws JDOMException {
        Resource modificationsResource = ResourceUtils.getResourceByRelativePath("modifications_equal_mass.xml");
        Set<Modification> modifications = modificationService.loadPipelineModifications(modificationsResource);

        //create 2 modification combinations with the same modifications, but in a different order in the modification list
        List<Modification> modificationsList = new ArrayList<>();
        modificationsList.addAll(modifications);
        List<Modification> reversedModificationsList = new ArrayList<>();
        for (int i = modificationsList.size() - 1; i >= 0; i--) {
            reversedModificationsList.add(modificationsList.get(i));
        }

        ModificationCombination modificationCombination = new ModificationCombination(modificationsList);
        ModificationCombination reversedModificationCombination = new ModificationCombination(reversedModificationsList);

        assertTrue(modificationCombination.equals(reversedModificationCombination));
        
        //now make a set and check if the contains method works
        Set<ModificationCombination> modificationCombinations = new HashSet<>();
        modificationCombinations.add(modificationCombination);
        
        assertTrue(modificationCombinations.contains(reversedModificationCombination));                
    }
}
