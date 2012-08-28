/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.service;

import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.util.gui.spectrum.SpectrumPanel;

/**
 *
 * @author Niels Hulstaert
 */
public interface SpectrumPanelService {

    /**
     * Constructs a SpectrumPanel for the given Identification
     *
     * @param identification the identification
     * @return the constructed SpectrumPanel
     */
    SpectrumPanel getSpectrumPanel(Identification identification);
}
