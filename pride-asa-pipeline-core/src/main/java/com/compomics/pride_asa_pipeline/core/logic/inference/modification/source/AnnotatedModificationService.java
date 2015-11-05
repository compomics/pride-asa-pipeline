package com.compomics.pride_asa_pipeline.core.logic.inference.modification.source;

import com.compomics.pride_asa_pipeline.core.logic.inference.modification.ModificationAdapter;
import com.compomics.pride_asa_pipeline.core.logic.inference.modification.ModificationAdapter;
import com.compomics.pride_asa_pipeline.core.logic.inference.modification.impl.AsapModificationAdapter;
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
