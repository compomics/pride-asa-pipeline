package com.compomics.pride_asa_pipeline.core.repository.impl.webservice;

import com.compomics.pride_asa_pipeline.core.logic.modification.UniModFactory;
import com.compomics.pride_asa_pipeline.core.logic.modification.conversion.impl.AsapModificationAdapter;
import com.compomics.pride_asa_pipeline.core.repository.ModificationRepository;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.util.pride.PrideWebService;
import com.compomics.util.pride.prideobjects.webservice.assay.AssayDetail;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

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
    public List<Modification> getModificationsByExperimentId(long experimentId) {
        LOGGER.debug("Loading modifications for experimentid " + experimentId);
        List<Modification> modifications = new ArrayList<>();
        AsapModificationAdapter adapter = new AsapModificationAdapter();
        try {
            AssayDetail assayDetail = PrideWebService.getAssayDetail(String.valueOf(experimentId));
            for (String aPtmName : assayDetail.getPtmNames()) {
                UniModFactory.getInstance().getModification(adapter, aPtmName);
            }
            LOGGER.debug("Finished loading modifications for pride experiment with id " + experimentId);
            return modifications;
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
        return modifications;
    }
}
