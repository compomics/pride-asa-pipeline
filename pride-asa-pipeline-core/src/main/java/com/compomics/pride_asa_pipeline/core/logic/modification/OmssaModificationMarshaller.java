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

import com.compomics.omssa.xsd.UserModCollection;
import com.compomics.pride_asa_pipeline.model.Modification;
import java.util.Set;
import org.jdom2.JDOMException;
import org.springframework.core.io.Resource;

/**
 *
 * @author Niels Hulstaert Hulstaert
 */
public interface OmssaModificationMarshaller {

    /**
     * Marshalls the modifications used in the pipeline to the OMSSA format
     *
     * @param modificationSet the set of found modifications
     * @return the usermod collection
     */
    UserModCollection marshallModifications(Set<Modification> modificationSet);
      /**
     * Marshalls the modifications used in the pipeline to the OMSSA format
     *
     * @param searchGuiModificationsResource
     * @return the set of searchGUI modifications with their prevalence value in PRIDE
     * @throws org.jdom2.JDOMException
     */
    Set<Modification> unmarshall(Resource searchGuiModificationsResource) throws JDOMException;
}
