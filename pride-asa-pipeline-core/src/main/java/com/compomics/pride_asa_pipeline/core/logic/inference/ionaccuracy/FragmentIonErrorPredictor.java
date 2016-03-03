package com.compomics.pride_asa_pipeline.core.logic.inference.ionaccuracy;

import com.compomics.pride_asa_pipeline.core.logic.inference.InferenceStatistics;
import com.compomics.pride_asa_pipeline.core.logic.inference.additional.contaminants.MassScanResult;
import com.compomics.pride_asa_pipeline.core.logic.inference.ionaccuracy.massdeficit.logic.AminoAcidMassInference;
import com.compomics.pride_asa_pipeline.core.util.report.impl.TotalReportGenerator;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.utilities.mol.Atom;

/**
 * This class uses mass deficits to estimate the crude used fragment ion
 * accuraccy PRIOR to annotating
 *
 * @author Kenneth Verheggen
 */
public class FragmentIonErrorPredictor {

    /**
     * The map of peptides to mz values
     */
    final HashMap<Peptide, double[]> peptideMzMap;
    /**
     * The estimated fragment ion accuraccy
     */
    double fragmentIonAccuraccy = 1.0;
    /**
     * The logger
     */
    private static final Logger LOGGER = Logger.getLogger(FragmentIonErrorPredictor.class);
    /**
     * The amino acid mass inferences to calculate mass deficits from
     */
    private final List<AminoAcidMassInference> aaInferences = new ArrayList<>();
    /**
     * The statistics object for fragmentation 
     */
    private InferenceStatistics fragmentStats;

    public FragmentIonErrorPredictor(HashMap<Peptide, double[]> peptideMzMap) throws IOException {
        this.peptideMzMap = peptideMzMap;
        init();
    }

    private void init() throws IOException {
        ArrayList<ModificationMatch> mockMods = new ArrayList<>();
        fragmentStats = new InferenceStatistics(true);
        //   LOGGER.info("Calculating fragment ion accuraccy using mass deficits...");
        LOGGER.info("Allowing errors smaller than " + (Atom.C_12.getMonoMass() / 12));
        for (Map.Entry<Peptide, double[]> aPeptide : peptideMzMap.entrySet()) {
            com.compomics.util.experiment.biology.Peptide utilitiesPeptide = new com.compomics.util.experiment.biology.Peptide(aPeptide.getKey().getSequenceString(), mockMods);
            AminoAcidMassInference aaInference = new AminoAcidMassInference(aPeptide.getValue(), utilitiesPeptide, new BigDecimal(aPeptide.getKey().getCharge()), new BigDecimal(1.0));
            aaInferences.add(aaInference);
            List<Double> reportedMassErrors = aaInference.getMassErrors();
            for (Double massError : reportedMassErrors) {
                fragmentStats.addValue(massError);
            }
        }
        double acc = InferenceStatistics.round(fragmentStats.calculateOptimalMassError(), 3);
        System.out.println(fragmentStats);
        this.fragmentIonAccuraccy = Math.min(1.0, acc);
        LOGGER.info("Estimated fragment ion accuraccy at " + fragmentIonAccuraccy);
        TotalReportGenerator.setFragmentAccMethod(fragmentStats.getMethodUsed());
        if (fragmentIonAccuraccy == 0.0
                || fragmentIonAccuraccy >= 1.0) {
            fragmentIonAccuraccy = MassScanResult.estimateFragmentIonToleranceBasedOnContaminants();
            TotalReportGenerator.setFragmentAccMethod("Estimated based on known mass spectrometry related contaminants");
        }
    }

    public double getFragmentIonAccuraccy() {
        return fragmentIonAccuraccy;
    }

    public List<AminoAcidMassInference> getAaInferences() {
        return aaInferences;
    }

    public InferenceStatistics getFragmentIonStats() {
        return fragmentStats;
    }

    public void clear() {
        if (aaInferences != null) {
            aaInferences.clear();
        }
        if (fragmentStats != null) {
            fragmentStats.clear();
        }
    }

}
