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
package com.compomics.pride_asa_pipeline.core.model.modification.source;

import com.compomics.pride_asa_pipeline.core.model.modification.ModificationAdapter;
import com.compomics.pride_asa_pipeline.core.model.modification.ModificationAdapter;
import com.compomics.pride_asa_pipeline.core.model.modification.impl.AsapModificationAdapter;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.util.pride.PrideWebService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Kenneth Verheggen
 */
public class AnnotatedModificationService {

    /**
     * The adapter that will convert the pride ptms to asap modifications
     */
    private static final ModificationAdapter asapAdapter = new AsapModificationAdapter();

    public AnnotatedModificationService() {

    }

    public Set<String> getProjectAnnotatedPTMs(String projectAccession) throws IOException {
        return PrideWebService.getProjectDetail(projectAccession).getPtmNames();
    }

    public  Set<String> getAssayAnnotatedPTMs(String assayAccession) throws IOException {
        Set<String> ptmNames = PrideWebService.getAssayDetail(assayAccession).getPtmNames();
        return ptmNames;
    }

    public List<Modification> convertToAsapMods(Set<String> ptmNames) {
        List<Modification> asapMods = new ArrayList<>();
        for (String aPTMName : ptmNames) {
            asapMods.add((Modification) PRIDEModificationFactory.getInstance().getModification(asapAdapter, aPTMName));
        }
        return asapMods;
    }

    /**
     * returns a list of annotated modifications for a given project
     *
     * @param projectAccession the project accession
     * @return a list of annotated modifications for a given project
     * @throws IOException if the service returns an error
     */
    public List<Modification> getProjectAnnotatedModifications(String projectAccession) throws IOException {
        return convertToAsapMods(getProjectAnnotatedPTMs(projectAccession));
    }

    /**
     * returns a list of annotated modifications for a given assay
     *
     * @param assayAccession the assay accession
     * @return a list of annotated modifications for a given assay
     * @throws IOException if the service returns an error
     */
    public List<Modification> getAssayAnnotatedModifications(String assayAccession) throws IOException {
        return convertToAsapMods(getAssayAnnotatedPTMs(assayAccession));
    }

}
