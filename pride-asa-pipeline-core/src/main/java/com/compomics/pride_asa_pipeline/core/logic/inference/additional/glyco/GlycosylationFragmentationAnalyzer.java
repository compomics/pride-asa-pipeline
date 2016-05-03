package com.compomics.pride_asa_pipeline.core.logic.inference.additional.glyco;

import com.compomics.pride_asa_pipeline.model.Peak;
import com.compomics.pride_asa_pipeline.model.Peptide;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author compomics
 */
public class GlycosylationFragmentationAnalyzer {

    private final Peptide peptide;
    private final List<Peak> peaks;
    private final double markerMass = 203;
    private final double mzTolerance;

    public GlycosylationFragmentationAnalyzer(Peptide peptide, List<Peak> peaks, double mzTolerance) {
        this.peptide = peptide;
        this.peaks = peaks;
        this.mzTolerance = mzTolerance;
    }

    public boolean hasMassPeak() {
        double precursorMassToCharge = peptide.getMzRatio();
        double precursorCharge = peptide.getCharge();
        double markerMz = markerMass / precursorCharge;
        //first check the mz values if this peak is present?

        double mzRatio;
        double mzDiff;
        for (int i = peaks.size(); i > 0; i--) {
            mzRatio = peaks.get(i).getMzRatio();
            mzDiff = precursorMassToCharge - mzRatio;
            if (Math.abs(mzDiff - markerMz) > mzTolerance) {
                System.out.println("Glyco possible !");
                return true;
            }
        }
        return false;
    }

}
