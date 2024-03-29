package com.compomics.pride_asa_pipeline.core.logic.inference.massdeficit;

import com.compomics.pride_asa_pipeline.core.logic.inference.InferenceStatistics;
import com.compomics.pride_asa_pipeline.core.logic.inference.massdeficit.logic.AminoAcidMassInference;
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

    public FragmentIonErrorPredictor(HashMap<Peptide, double[]> peptideMzMap) throws IOException {
        this.peptideMzMap = peptideMzMap;
        init();
    }

    private void init() throws IOException {
        ArrayList<ModificationMatch> mockMods = new ArrayList<>();
        InferenceStatistics fragmentStats = new InferenceStatistics(true);
        //   LOGGER.info("Calculating fragment ion accuraccy using mass deficits...");
        for (Map.Entry<Peptide, double[]> aPeptide : peptideMzMap.entrySet()) {
            com.compomics.util.experiment.biology.Peptide utilitiesPeptide = new com.compomics.util.experiment.biology.Peptide(aPeptide.getKey().getSequenceString(), mockMods);
            AminoAcidMassInference aaInference = new AminoAcidMassInference(aPeptide.getValue(), utilitiesPeptide, new BigDecimal(aPeptide.getKey().getCharge()), new BigDecimal(1.0));
            List<Double> reportedMassErrors = aaInference.getMassErrors();
            for (Double massError : reportedMassErrors) {
                if (massError < (Atom.C_12.getMonoMass() / 12)) {
                    //mass over charge error?
                    fragmentStats.addValue(massError);
                }
            }
        }
        this.fragmentIonAccuraccy = Math.min(1.0, InferenceStatistics.round(fragmentStats.calculateOptimalMassError(), 3));
        LOGGER.info("Estimated fragment ion accuraccy at " + fragmentIonAccuraccy);
    }

    public double getFragmentIonAccuraccy() {
        return fragmentIonAccuraccy;
    }

}
