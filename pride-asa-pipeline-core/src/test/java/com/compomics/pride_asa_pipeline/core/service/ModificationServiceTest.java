/* 
 * Copyright 2018 compomics.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.compomics.pride_asa_pipeline.core.service;

import com.compomics.pride_asa_pipeline.core.logic.modification.ModificationMarshaller;
import com.compomics.pride_asa_pipeline.core.logic.modification.impl.ModificationMarshallerImpl;
import com.compomics.pride_asa_pipeline.core.service.impl.PrideModificationServiceImpl;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.core.model.ModificationHolder;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Set;
import static org.junit.Assert.*;
import org.jdom2.JDOMException;
import org.junit.Before;
import org.junit.Test;


/**
 *
 * @author Niels Hulstaert Hulstaert
 */
public class ModificationServiceTest {

    private ModificationMarshaller modificationMarshaller;
    private DbModificationService modificationService;

    @Before
    public void setup() throws JDOMException {
        modificationMarshaller = new ModificationMarshallerImpl();
        modificationService = new PrideModificationServiceImpl();
    }

    /**
     * Test the case where the PRIDE modifications are not conflicting.
     *
     * @throws JDOMException
     */
    @Test
    public void testFilterModifications_1() throws JDOMException, URISyntaxException {
        File modificationsResource = new File(ModificationServiceTest.class.getClassLoader().getResource("resources/pride_asap_modifications.xml").toURI());

        File prideModificationsResource = new File(ModificationServiceTest.class.getClassLoader().getResource("conflicting_pride_asap_modifications_1.xml").toURI());

        Set<Modification> modifications = modificationMarshaller.unmarshall(modificationsResource);
        Set<Modification> prideModifications = modificationMarshaller.unmarshall(prideModificationsResource);

        //add modifications to to ModificationHolder
        ModificationHolder modificationHolder = new ModificationHolder();
        modificationHolder.addModifications(modifications);

        Set<Modification> conflictingModifications = modificationService.filterModifications(modificationHolder, prideModifications);

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
    public void testFilterModifications_2() throws JDOMException, URISyntaxException {
        File modificationsResource = new File(ModificationServiceTest.class.getClassLoader().getResource("resources/pride_asap_modifications.xml").toURI());
        File prideModificationsResource = new File(ModificationServiceTest.class.getClassLoader().getResource("conflicting_pride_asap_modifications_2.xml").toURI());

        Set<Modification> modifications = modificationMarshaller.unmarshall(modificationsResource);
        Set<Modification> prideModifications = modificationMarshaller.unmarshall(prideModificationsResource);

        //add modifications to to ModificationHolder
        ModificationHolder modificationHolder = new ModificationHolder();
        modificationHolder.addModifications(modifications);

        Set<Modification> conflictingModifications = modificationService.filterModifications(modificationHolder, prideModifications);

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
    public void testFilterModifications_3() throws JDOMException, URISyntaxException {
        File modificationsResource = new File(ModificationServiceTest.class.getClassLoader().getResource("resources/pride_asap_modifications.xml").toURI());
        File prideModificationsResource = new File(ModificationServiceTest.class.getClassLoader().getResource("conflicting_pride_asap_modifications_3.xml").toURI());
        Set<Modification> modifications = modificationMarshaller.unmarshall(modificationsResource);
        Set<Modification> prideModifications = modificationMarshaller.unmarshall(prideModificationsResource);

        //add modifications to to ModificationHolder
        ModificationHolder modificationHolder = new ModificationHolder();
        modificationHolder.addModifications(modifications);

        Set<Modification> conflictingModifications = modificationService.filterModifications(modificationHolder, prideModifications);

        //3 of the 5 PRIDE modifications should conflict
        assertEquals(3, conflictingModifications.size());
    }
}
