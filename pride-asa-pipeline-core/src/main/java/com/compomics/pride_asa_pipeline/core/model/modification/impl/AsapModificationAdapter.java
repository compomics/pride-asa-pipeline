package com.compomics.pride_asa_pipeline.core.model.modification.impl;

import com.compomics.pride_asa_pipeline.core.model.modification.ModificationAdapter;
import com.compomics.pride_asa_pipeline.core.model.modification.PRIDEModification;
import com.compomics.pride_asa_pipeline.model.AminoAcid;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.Modification.Location;
import java.util.HashSet;
import uk.ac.ebi.pridemod.model.Specificity;

/**
 *
 * @author Kenneth Verheggen
 */
public class AsapModificationAdapter implements ModificationAdapter<Modification> {

    private HashSet<AminoAcid> affectedAminoAcid;

    @Override
    public Modification convertModification(PRIDEModification mod) {
        Double averageIsotopicMass = mod.getAveDeltaMass();
        Double monoIsotopicMass = mod.getMonoDeltaMass();
        //TODO calcualte this from the formula?
        if (averageIsotopicMass == null) {
            averageIsotopicMass = 0.0;
        }
        if (monoIsotopicMass == null) {
            monoIsotopicMass = 0.0;
        }
        Location modLocation;
        try {
            modLocation = getLocation(mod);
        } catch (NullPointerException e) {
            modLocation = Location.NON_TERMINAL;
        }
        return new Modification(mod.getName(),
                monoIsotopicMass,
                averageIsotopicMass,
                modLocation,
                affectedAminoAcid,
                mod.getAccession(),
                mod.getAccession());
    }

    private Location getLocation(uk.ac.ebi.pridemod.model.PTM ptm) {
        HashSet<Location> affectedLocations = new HashSet<>();
        affectedAminoAcid = new HashSet<>();
        for (Specificity specificity : ptm.getSpecificityCollection()) {
            if (specificity.getName().equals(specificity.getName().NONE)) {
                return Modification.Location.NON_TERMINAL;
            }
            affectedAminoAcid.add(AminoAcid.getAA(specificity.getName().toString()));
            Location currentLocation;
            switch (specificity.getPosition()) {
                case CTERM:
                    currentLocation = (Modification.Location.C_TERMINAL);
                    break;
                case NTERM:
                    currentLocation = (Modification.Location.N_TERMINAL);
                    break;
                case PCTERM:
                    currentLocation = (Modification.Location.C_TERMINAL);
                    break;
                case PNTERM:
                    currentLocation = (Modification.Location.N_TERMINAL);
                    break;
                default:
                    currentLocation = (Modification.Location.NON_TERMINAL);
            }
            affectedLocations.add(currentLocation);
        }
        Location location;
        if (affectedLocations.size() != 1) {
            location = Modification.Location.NON_TERMINAL;
        } else {
            location = affectedLocations.iterator().next();
        }
        return location;
    }

}
