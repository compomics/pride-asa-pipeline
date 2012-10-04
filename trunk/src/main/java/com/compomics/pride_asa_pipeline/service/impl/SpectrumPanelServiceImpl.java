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
 * @author Niels Hulstaert
 * @author Harald Barsnes
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

        //get spectrum peaks for the selected identification
        List<Peak> peaks = spectrumService.getSpectrumPeaksBySpectrumId(identification.getSpectrumId());

        //initialize new SpectrumPanel
        SpectrumPanel spectrumPanel = new SpectrumPanel(PeakUtils.getMzRatiosAsArray(peaks),
                PeakUtils.getIntensitiesAsArray(peaks),
                identification.getPeptide().getMzRatio(),
                Integer.toString(identification.getPeptide().getCharge()), "", 40, false, false, false);

        // remove the border
        spectrumPanel.setBorder(null);

        spectrumPanel.showAnnotatedPeaksOnly(Boolean.TRUE);

        //add peak annotations
        if (identification.getAnnotationData().getFragmentIonAnnotations() != null) {
            spectrumPanel.setAnnotations(getPeakAnnotations(identification));
        }

        //add noise threshold area
        ReferenceArea referenceArea = getReferenceArea(identification.getAnnotationData().getNoiseThreshold());
        spectrumPanel.addReferenceAreaYAxis(referenceArea);

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
            String label = fragmentIonAnnotation.getIon_type_name().substring(0, 1);
            label += fragmentIonAnnotation.getFragment_ion_number();
            
            if (fragmentIonAnnotation.getIon_charge() > 1) {
                label += getIonChargeString(fragmentIonAnnotation.getIon_charge());
            }
            
            DefaultSpectrumAnnotation defaultSpectrumAnnotation = new DefaultSpectrumAnnotation(
                    fragmentIonAnnotation.getMz(), fragmentIonAnnotation.getMass_error(), SpectrumPanel.determineColorOfPeak(label), label);
            peakAnnotations.add(defaultSpectrumAnnotation);
        }

        return peakAnnotations;
    }

    private ReferenceArea getReferenceArea(double noiseThreshold) {
        return new ReferenceArea(
                "", // reference area unique identifier
                "", // reference area label
                0.0, // start of area
                noiseThreshold, // end of area
                Color.blue, // color of area
                0.1f, // transparency level
                Boolean.TRUE, // drawn on top of or behind the data
                Boolean.FALSE);
    }

    private String getIonChargeString(int ionCharge) {
        String ionChargeString = "";
        for (int i = 0; i < ionCharge; i++) {
            ionChargeString += "+";
        }

        return ionChargeString;
    }
}
