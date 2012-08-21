/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.service.impl;

import com.compomics.pride_asa_pipeline.model.FragmentIonAnnotation;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Peak;
import com.compomics.pride_asa_pipeline.service.SpectrumPanelService;
import com.compomics.pride_asa_pipeline.service.SpectrumService;
import com.compomics.pride_asa_pipeline.util.PeakUtils;
import com.compomics.util.gui.spectrum.DefaultSpectrumAnnotation;
import com.compomics.util.gui.spectrum.ReferenceArea;
import com.compomics.util.gui.spectrum.SpectrumPanel;
import java.awt.Color;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author niels
 */
public class SpectrumPanelServiceImpl implements SpectrumPanelService {

    private SpectrumService spectrumService;

    public SpectrumService getSpectrumService() {
        return spectrumService;
    }

    public void setSpectrumService(SpectrumService spectrumService) {
        this.spectrumService = spectrumService;
    }

    @Override
    public SpectrumPanel getSpectrumPanel(Identification identification) {
        SpectrumPanel spectrumPanel = null;

        //get spectrum peaks for the selected identification
        List<Peak> peaks = spectrumService.getSpectrumPeaksBySpectrumId(identification.getSpectrumId());

        //initialize new SpectrumPanel
        spectrumPanel = new SpectrumPanel(PeakUtils.getMzRatiosAsArray(peaks), PeakUtils.getIntensitiesAsArray(peaks), identification.getPeptide().getMzRatio(), Integer.toString(identification.getPeptide().getCharge()), "test");

        //add peak annotations
        spectrumPanel.setAnnotations(getPeakAnnotations(identification));

        //add noise threshold area
        spectrumPanel.addReferenceAreaYAxis(getReferenceArea(identification.getAnnotationData().getNoiseThreshold()));

        return spectrumPanel;
    }

    /**
     * Constructs the vector with the peak annotations for the spectrum panel.
     *
     * @param identification the identification
     * @return the vector of peak annotations
     */
    private Vector<DefaultSpectrumAnnotation> getPeakAnnotations(Identification identification) {
        Vector<DefaultSpectrumAnnotation> peakAnnotations = new Vector();

        for (FragmentIonAnnotation fragmentIonAnnotation : identification.getAnnotationData().getFragmentIonAnnotations()) {
            String label = fragmentIonAnnotation.getIon_type_name().substring(0, 1) + getIonChargeString(fragmentIonAnnotation.getIon_charge()) + fragmentIonAnnotation.getFragment_ion_number();
            DefaultSpectrumAnnotation defaultSpectrumAnnotation = new DefaultSpectrumAnnotation(fragmentIonAnnotation.getMz(), fragmentIonAnnotation.getMass_error(), SpectrumPanel.determineColorOfPeak(label), label);
            peakAnnotations.add(defaultSpectrumAnnotation);
        }
        
        return peakAnnotations;
    }

private ReferenceArea getReferenceArea(double noiseThreshold) {
        return new ReferenceArea(
                "", // reference area unique identifier
                //"A", // reference area label
                0.0, // start of area
                noiseThreshold, // end of area
                Color.blue, // color of area
                0.3f, // transparency level
                Boolean.TRUE, // drawn on top of or behind the data
                Boolean.TRUE);
    }

    private String getIonChargeString(int ionCharge) {
        String ionChargeString = "";
        for (int i = 0; i < ionCharge; i++) {
            ionChargeString += "+";
        }

        return ionChargeString;
    }
}
