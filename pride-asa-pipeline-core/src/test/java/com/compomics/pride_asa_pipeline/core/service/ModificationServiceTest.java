/*
 */
package com.compomics.pride_asa_pipeline.core.service;

import com.compomics.pride_asa_pipeline.core.logic.modification.ModificationMarshaller;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.core.model.ModificationHolder;
import com.compomics.pride_asa_pipeline.core.util.ResourceUtils;
import java.util.Set;
import static org.junit.Assert.*;
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
public class ModificationServiceTest {

    @Autowired
    private ModificationMarshaller modificationMarshaller;
    @Autowired
    private DbModificationService dbModificationService;

    /**
     * Test the case where the PRIDE modifications are not conflicting.
     *
     * @throws JDOMException
     */
    @Test
    public void testFilterModifications_1() throws JDOMException {
        Resource modificationsResource = ResourceUtils.getResourceByRelativePath("resources/pride_asap_modifications.xml");
        Resource prideModificationsResource = ResourceUtils.getResourceByRelativePath("conflicting_pride_asap_modifications_1.xml");
        Set<Modification> modifications = modificationMarshaller.unmarshall(modificationsResource);
        Set<Modification> prideModifications = modificationMarshaller.unmarshall(prideModificationsResource);

        //add modifications to to ModificationHolder
        ModificationHolder modificationHolder = new ModificationHolder();
        modificationHolder.addModifications(modifications);

        Set<Modification> conflictingModifications = dbModificationService.filterModifications(modificationHolder, prideModifications);

        //none of the modifications should conflict
        assertTrue(conflictingModifications.isEmpty());
    }

    /**
     * Test the case where the PRIDE modifications are conflicting with the
     * pipeline modifications.
     *
     * @throws JDOMException
     */
    @Test
    public void testFilterModifications_2() throws JDOMException {
        Resource modificationsResource = ResourceUtils.getResourceByRelativePath("resources/pride_asap_modifications.xml");
        Resource prideModificationsResource = ResourceUtils.getResourceByRelativePath("conflicting_pride_asap_modifications_2.xml");
        Set<Modification> modifications = modificationMarshaller.unmarshall(modificationsResource);
        Set<Modification> prideModifications = modificationMarshaller.unmarshall(prideModificationsResource);

        //add modifications to to ModificationHolder
        ModificationHolder modificationHolder = new ModificationHolder();
        modificationHolder.addModifications(modifications);

        Set<Modification> conflictingModifications = dbModificationService.filterModifications(modificationHolder, prideModifications);

        //2 of the 3 PRIDE modifications should conflict
        assertEquals(2, conflictingModifications.size());
    }

    /**
     * Test the case where the PRIDE modifications are conflicting with
     * themselves. Only one of the conflicting modifications should be kept.
     *
     * @throws JDOMException
     */
    @Test
    public void testFilterModifications_3() throws JDOMException {
        Resource modificationsResource = ResourceUtils.getResourceByRelativePath("resources/pride_asap_modifications.xml");
        Resource prideModificationsResource = ResourceUtils.getResourceByRelativePath("conflicting_pride_asap_modifications_3.xml");
        Set<Modification> modifications = modificationMarshaller.unmarshall(modificationsResource);
        Set<Modification> prideModifications = modificationMarshaller.unmarshall(prideModificationsResource);

        //add modifications to to ModificationHolder
        ModificationHolder modificationHolder = new ModificationHolder();
        modificationHolder.addModifications(modifications);

        Set<Modification> conflictingModifications = dbModificationService.filterModifications(modificationHolder, prideModifications);

        //3 of the 5 PRIDE modifications should conflict
        assertEquals(3, conflictingModifications.size());
    }
}
