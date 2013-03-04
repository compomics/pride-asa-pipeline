/*
 */
package com.compomics.pride_asa_pipeline.model;

import com.compomics.pride_asa_pipeline.service.DbModificationService;
import com.compomics.pride_asa_pipeline.util.ResourceUtils;
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
    private DbModificationService modificationService;

    @Test
    public void testEqualsAndHashCode() throws JDOMException {
        Resource modificationsResource = ResourceUtils.getResourceByRelativePath("modifications_equal_mass.xml");
        Set<Modification> modifications = modificationService.loadPipelineModifications(modificationsResource);

        //create 2 modification combinations with the same modifications, but in a different order in the modification list
        List<Modification> modificationsList = new ArrayList<Modification>();
        modificationsList.addAll(modifications);
        List<Modification> reversedModificationsList = new ArrayList<Modification>();
        for (int i = modificationsList.size() - 1; i >= 0; i--) {
            reversedModificationsList.add(modificationsList.get(i));
        }

        ModificationCombination modificationCombination1 = new ModificationCombination(modificationsList);
        ModificationCombination modificationCombination2 = new ModificationCombination(reversedModificationsList);

        assertTrue(modificationCombination1.equals(modificationCombination2));
        
        //now make a set and check if the contains method works
        Set<ModificationCombination> modificationCombinations = new HashSet<ModificationCombination>();
        modificationCombinations.add(modificationCombination1);
        
        assertTrue(modificationCombinations.contains(modificationCombination2));                
    }
}
