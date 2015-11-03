package com.compomics.pride_asa_pipeline.core.logic.modification;

import com.compomics.pride_asa_pipeline.core.logic.modification.conversion.ModificationAdapter;
import com.compomics.pride_asa_pipeline.core.logic.modification.conversion.impl.AsapModificationAdapter;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.util.pride.PrideWebService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kenneth Verheggen
 */
public class AnnotatedModificationService {

    /**
     * The unimod factory that can retrieve modifications and convert them to
     * utilities/pride asap
     *
     */
    private static final UniModFactory factory = UniModFactory.getInstance();
    /**
     * The adapter that will convert the pride ptms to asap modifications
     */
    private static final ModificationAdapter asapAdapter = new AsapModificationAdapter();

    public AnnotatedModificationService() {

    }

    public String[] getProjectAnnotatedPTMs(String projectAccession) throws IOException {
        return PrideWebService.getProjectDetail(projectAccession).getPtmNames();
    }

    public String[] getAssayAnnotatedPTMs(String assayAccession) throws IOException {
        String projectAccession = PrideWebService.getAssayDetail(assayAccession).getProjectAccession();
        return PrideWebService.getProjectDetail(projectAccession).getPtmNames();
    }

    public List<Modification> convertToAsapMods(String[] ptmNames) {
        List<Modification> asapMods = new ArrayList<>();
        for (String aPTMName : ptmNames) {
            asapMods.add((Modification) factory.getModification(asapAdapter, aPTMName));
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
