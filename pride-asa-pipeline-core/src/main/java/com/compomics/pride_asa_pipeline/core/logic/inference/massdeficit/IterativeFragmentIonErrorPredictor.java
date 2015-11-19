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
public class IterativeFragmentIonErrorPredictor extends FragmentIonErrorPredictor {

    private static final Logger LOGGER = Logger.getLogger(IterativeFragmentIonErrorPredictor.class);
    private static final double initialAccuracy = 1.0;
    
    
    public IterativeFragmentIonErrorPredictor(HashMap<Peptide, double[]> peptideMzMap) throws IOException {
        super(peptideMzMap);
    }

    public void init() {
        double current = initialAccuracy;
        ArrayList<Double> convergenceList = new ArrayList<>();
        convergenceList.add(current);
        int index = 0;
        while (true) {
            index++;
            try {
                analyze(new BigDecimal(current));
                convergenceList.add(getFragmentIonAccuraccy());
                if (convergenceList.size() > 2) {
                    //check the slope with the previous element
                    double y = convergenceList.get(index - 1);
                    double y2 = convergenceList.get(index + 1);
                    double slope = (y2 - y) / 2;
                    //if the slope is less than 5%, then the following iterations won't mather that much
                    //also if the error starts increasing again, that's a bad sign
                    if (Math.abs(slope) < 0.05 || (Math.abs(y2)>Math.abs(y))) {
                        break;
                    }
                }
            } catch (IOException ex) {
                LOGGER.error(ex);
                break;
            }
        }
    }

    private void analyze(BigDecimal currentAccuracy) throws IOException {
        ArrayList<ModificationMatch> mockMods = new ArrayList<>();
        InferenceStatistics fragmentStats = new InferenceStatistics(true);
        //   LOGGER.info("Calculating fragment ion accuraccy using mass deficits...");
        for (Map.Entry<Peptide, double[]> aPeptide : super.peptideMzMap.entrySet()) {
            com.compomics.util.experiment.biology.Peptide utilitiesPeptide = new com.compomics.util.experiment.biology.Peptide(aPeptide.getKey().getSequenceString(), mockMods);
            AminoAcidMassInference aaInference = new AminoAcidMassInference(aPeptide.getValue(), utilitiesPeptide, new BigDecimal(aPeptide.getKey().getCharge()), currentAccuracy);
            List<Double> reportedMassErrors = aaInference.getMassErrors();
            for (Double massError : reportedMassErrors) {
                if (massError < (Atom.C_12.getMonoMass() / 12)) {
                    //mass over charge error?
                    fragmentStats.addValue(massError);
                }
            }
        }
        super.fragmentIonAccuraccy = Math.min(1.0, InferenceStatistics.round(fragmentStats.calculateOptimalMassError(), 3));
        LOGGER.info("Estimated fragment ion accuraccy at " + super.getFragmentIonAccuraccy());
    }

    public double getFragmentIonAccuraccy() {
        return fragmentIonAccuraccy;
    }

}
