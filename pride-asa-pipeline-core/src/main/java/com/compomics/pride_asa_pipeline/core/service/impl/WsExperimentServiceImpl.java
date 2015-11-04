package com.compomics.pride_asa_pipeline.core.service.impl;

import com.compomics.util.pride.PrideWebService;
import com.compomics.util.pride.prideobjects.webservice.peptide.PsmDetail;
import com.compomics.util.pride.prideobjects.webservice.peptide.PsmDetailList;
import java.io.IOException;
import java.util.Set;
import org.apache.log4j.Logger;


/**
 * @author Kenneth Verheggen
 */
public class WsExperimentServiceImpl extends DbExperimentServiceImpl {

    private static final Logger LOGGER = Logger.getLogger(WsExperimentServiceImpl.class);
    
    @Override
    public void updateChargeStates(String experimentAccession, Set<Integer> chargeStates) {
        try {
            //get the considered charges for the experiment
            PsmDetailList psMsByAssay = PrideWebService.getPSMsByAssay(experimentAccession);
            for (PsmDetail aDetail : psMsByAssay.getList()) {
                chargeStates.add(aDetail.getCharge());
            }
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
    }

}
