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

import com.compomics.pride_asa_pipeline.core.model.modification.source.PRIDEModificationFactory;
import com.compomics.pride_asa_pipeline.core.model.modification.impl.AsapModificationAdapter;
import com.compomics.pride_asa_pipeline.core.repository.ModificationRepository;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.util.pride.PrideWebService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.archive.web.service.model.assay.AssayDetail;

/**
 *
 * @author Kenneth Verheggen
 */
public class WSModificationRepository implements ModificationRepository {

    private static final Logger LOGGER = Logger.getLogger(WSModificationRepository.class);

    @Override
    public List<Modification> getModificationsByPeptideId(long peptideId) {
        throw new UnsupportedOperationException("Currently not supported through the webservice");
    }

    @Override
    public List<Modification> getModificationsByExperimentId(String experimentId) {
        LOGGER.debug("Loading modifications for experimentid " + experimentId);
        List<Modification> modifications = new ArrayList<>();
        AsapModificationAdapter adapter = new AsapModificationAdapter();
        try {
            AssayDetail assayDetail = PrideWebService.getAssayDetail(String.valueOf(experimentId));
            for (String aPtmName : assayDetail.getPtmNames()) {
                PRIDEModificationFactory.getInstance().getModification(adapter, aPtmName);
            }
            LOGGER.debug("Finished loading modifications for pride experiment with id " + experimentId);
            return modifications;
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
        return modifications;
    }
}
