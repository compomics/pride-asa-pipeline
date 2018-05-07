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
package com.compomics.pride_asa_pipeline.core.logic.modification;

import com.compomics.pride_asa_pipeline.model.Modification;

import java.io.File;
import java.util.Collection;
import java.util.Set;
import org.jdom2.JDOMException;

/**
 *
 * @author Niels Hulstaert
 */
public interface ModificationMarshaller {

    /**
     * Unmarchalls the given modifications XML resource and returns a set of
     * modifications
     *
     * @param modificationsResource the modifications XML resource to be parsed
     * @return the set of modifications
     * @exception JDOMException
     */
    Set<Modification> unmarshall(File modificationsResource) throws JDOMException;

    /**
     * Marshalls the collection of modifications to given XML file.
     *
     * @param modificationsResource the modifications resource
     * @param modifications the modifications collection
     */
    void marshall(File modificationsResource, Collection<Modification> modifications);
}
