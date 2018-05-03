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
package com.compomics.pride_asa_pipeline.core.repository.impl.webservice;

import com.compomics.pride_asa_pipeline.core.repository.ExperimentRepository;
import com.compomics.pride_asa_pipeline.model.AminoAcidSequence;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.model.UnknownAAException;
import com.compomics.util.pride.PrideWebService;
import com.compomics.util.pride.prideobjects.webservice.query.PrideFilter;
import com.compomics.util.pride.prideobjects.webservice.query.PrideFilterType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.archive.web.service.model.assay.AssayDetail;
import uk.ac.ebi.pride.archive.web.service.model.assay.AssayDetailList;
import uk.ac.ebi.pride.archive.web.service.model.peptide.PsmDetail;
import uk.ac.ebi.pride.archive.web.service.model.peptide.PsmDetailList;
import uk.ac.ebi.pride.archive.web.service.model.project.ProjectSummary;
import uk.ac.ebi.pride.archive.web.service.model.project.ProjectSummaryList;
import uk.ac.ebi.pride.archive.web.service.model.protein.ProteinDetail;
import uk.ac.ebi.pride.archive.web.service.model.protein.ProteinDetailList;

/**
 *
 * @author Kenneth Verheggen
 */
public class WSExperimentRepository implements ExperimentRepository {

    private static final Logger LOGGER = Logger.getLogger(WSExperimentRepository.class);

    @Override
    public Map<String, String> findAllExperimentAccessions() {
        return findExperimentAccessionsByTaxonomy(-1);
    }

    @Override
    public Map<String, String> findExperimentAccessionsByTaxonomy(int taxonomyId) {
        Map<String, String> allExperimentsAccessions = new TreeMap<>();
        try {
            ProjectSummaryList projectSummaryList;
            if (taxonomyId == -1) {
                projectSummaryList = PrideWebService.getProjectSummaryList("", new PrideFilter(PrideFilterType.speciesFilter, String.valueOf(taxonomyId)));
            } else {
                projectSummaryList = PrideWebService.getProjectSummaryList("");
            }
            for (ProjectSummary aProjectSummary : projectSummaryList.getList()) {
                AssayDetailList assayDetails = PrideWebService.getAssayDetails(aProjectSummary.getAccession());
                for (AssayDetail anAssayDetail : assayDetails.getList()) {
                    allExperimentsAccessions.put(anAssayDetail.getAssayAccession(), anAssayDetail.getTitle());
                }
            }
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
        return allExperimentsAccessions;
    }

    @Override
    public List<Identification> loadExperimentIdentifications(String experimentAccession) {
        LOGGER.debug("Start loading identifications for experiment " + experimentAccession);
        List<Identification> identifications = new ArrayList<>();
        try {
            PsmDetailList psmByAssay = PrideWebService.getPSMsByAssay(experimentAccession);
            for (PsmDetail aDetail : psmByAssay.getList()) {
                try {
                    AminoAcidSequence aaSequence = new AminoAcidSequence(aDetail.getSequence());
                    Peptide peptide = new Peptide(aDetail.getCharge(), aDetail.getExperimentalMZ().doubleValue(), aaSequence);
                    Identification ident = new Identification(peptide, aDetail.getExperimentalMZ().toString(), aDetail.getReportedID(), aDetail.getSpectrumID());
                    identifications.add(ident);
                } catch (UnknownAAException ex) {
                    LOGGER.error(ex);
                }
            }
            LOGGER.debug("Finished loading " + identifications.size() + " identifications for experiment " + experimentAccession);
        } catch (IOException ex) {
            LOGGER.error(ex);
        }

        return identifications;
    }

    @Override
    public long getNumberOfSpectra(String experimentAccession) {
        LOGGER.debug("Start counting number of spectra for experiment " + experimentAccession);
        long numberOfSpectra = 0;
        try {
            numberOfSpectra = PrideWebService.getAssayDetail(experimentAccession).getTotalSpectrumCount();
            LOGGER.debug("Finished counting number of spectra for experiment " + experimentAccession);
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
        return numberOfSpectra;
    }

    @Override
    public List<String> getProteinAccessions(String experimentAccession) {
        LOGGER.debug("Start retrieving protein accessions for experiment " + experimentAccession);
        List<String> proteinAccessions = new ArrayList<>();
        try {
            ProteinDetailList proteinIdentificationByAssay = PrideWebService.getProteinIdentificationByAssay(experimentAccession);
            for (ProteinDetail proteinDetail : proteinIdentificationByAssay.getList()) {
                proteinAccessions.add(proteinDetail.getAccession());
            }
            LOGGER.debug("Start retrieving protein accessions for experiment " + experimentAccession);
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
        return proteinAccessions;
    }

    @Override
    public long getNumberOfPeptides(String experimentAccession) {
        LOGGER.debug("Start counting number of peptides for experiment " + experimentAccession);
        long numberOfPeptides = 0;
        try {
            numberOfPeptides = PrideWebService.getAssayDetail(experimentAccession).getPeptideCount();
            LOGGER.debug("Finished counting number of peptides for experiment " + experimentAccession);
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
        return numberOfPeptides;
    }

    @Override
    public List<Map<String, Object>> getSpectraMetadata(String experimentAccession) {
        throw new UnsupportedOperationException("No longer supported through the webservice");
    }

}
