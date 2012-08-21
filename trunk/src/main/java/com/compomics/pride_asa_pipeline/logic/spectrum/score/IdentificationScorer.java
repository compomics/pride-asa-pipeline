package com.compomics.pride_asa_pipeline.logic.spectrum.score;

import com.compomics.pride_asa_pipeline.model.AnnotationData;
import com.compomics.pride_asa_pipeline.model.Peak;
import com.compomics.pride_asa_pipeline.model.Peptide;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: niels Date: 27/10/11 Time: 16:58 To change
 * this template use File | Settings | File Templates.
 */
public interface IdentificationScorer {

    /**
     * Scores the identification; the peptide is matched against the
     * spectrum peaks. The scoring result and fragment ion annotations are
     * returned as annotation data.
     *
     * @param peptide the peptide to score the spectrum against
     * @param peaks the spectrum peaks
     * @return the annotation data (scoring result + fragment ion annotations)
     */
    AnnotationData score(Peptide peptide, List<Peak> peaks);
    
    /**
     * Sets the fragment mass error
     * 
     * @param fragmentMassError the fragment mass error value
     */
    void setFragmentMassError(double fragmentMassError);
}
