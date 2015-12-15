package com.compomics.pride_asa_pipeline.core.model;

import com.compomics.pride_asa_pipeline.model.AminoAcid;
import com.compomics.pride_asa_pipeline.model.Modification;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A construct to hold all registered modifications ordered by their possible
 * position on the precursor sequence. (e.g. N-terminal or C-terminal or
 * non-terminal modifications)
 *
 * @author Florian Reisinger Date: 19-Aug-2009
 * @since 0.1
 */
public class ModificationHolder {

    private Map<AminoAcid, Set<Modification>> nonTerminalMods;
    private Map<AminoAcid, Set<Modification>> nTerminalMods;
    private Map<AminoAcid, Set<Modification>> cTerminalMods;
    private Map<Double, Set<Modification>> massToModification;

    public ModificationHolder() {
        nonTerminalMods = new EnumMap<>(AminoAcid.class);
        nTerminalMods = new EnumMap<>(AminoAcid.class);
        cTerminalMods = new EnumMap<>(AminoAcid.class);
        massToModification = new HashMap<>();
    }

    public void addModifications(Collection<Modification> modifications) {
        for (Modification modification : modifications) {
            addModification(modification);
        }
    }

    public void addModification(Modification modification) {
        if (null != modification.getLocation()) //add the given modification to its corresponding map
        switch (modification.getLocation()) {
            case NON_TERMINAL:
                //add this modification to all non-terminal amino acids that could be affected
                for (AminoAcid aa : modification.getAffectedAminoAcids()) {
                    if (nonTerminalMods.get(aa) == null) {
                        nonTerminalMods.put(aa, new HashSet<Modification>());
                    }
                    nonTerminalMods.get(aa).add(modification);
                }   break;
            case N_TERMINAL:
                //add this modification to all N-terminal amino acids that could be affected
                for (AminoAcid aa : modification.getAffectedAminoAcids()) {
                    if (nTerminalMods.get(aa) == null) {
                        nTerminalMods.put(aa, new HashSet<Modification>());
                    }
                    nTerminalMods.get(aa).add(modification);
                }   break;
            case C_TERMINAL:
                //add this modification to all C-terminal amino acids that could be affected
                for (AminoAcid aa : modification.getAffectedAminoAcids()) {
                    if (cTerminalMods.get(aa) == null) {
                        cTerminalMods.put(aa, new HashSet<Modification>());
                    }
                    cTerminalMods.get(aa).add(modification);
                }   break;
            default:
                //sanity check
                throw new IllegalStateException("Illegal amino acid position!! " + modification.getLocation());
        }

        //update the mass to modification map
        Double mass = modification.getMassShift();
        //if not entry exists yet for the given mass, add one
        if (massToModification.get(mass) == null) {
            Set<Modification> mods = new HashSet<>();
            massToModification.put(mass, mods);
        }
        massToModification.get(mass).add(modification);
    }

    public boolean containsModForAA(AminoAcid aa) {
        //if there is a modification for either non-terminal, N-terminal or C-terminal position, return true.
        return (nonTerminalMods.containsKey(aa) || nTerminalMods.containsKey(aa) || cTerminalMods.containsKey(aa));
    }

    public int getNumberOfModifications() {
        return getAllModifications().size();
    }

    public Set<Modification> getAllModifications() {
        //ToDo: maybe cache as additional internal set?
        Set<Modification> all = new HashSet<>();
        //combine all modifications for all amino acids for all possible positions
        for (Set<Modification> modifications : nonTerminalMods.values()) {
            all.addAll(modifications);
        }
        for (Set<Modification> modifications : nTerminalMods.values()) {
            all.addAll(modifications);
        }
        for (Set<Modification> modifications : cTerminalMods.values()) {
            all.addAll(modifications);
        }
        return all;
    }

    public Set<Modification> getNonTerminalMods(AminoAcid aminoAcid) {
        return nonTerminalMods.get(aminoAcid);
    }

    public Set<Modification> getNterminalMods(AminoAcid aminoAcid) {
        return nTerminalMods.get(aminoAcid);
    }

    public Set<Modification> getCterminalMods(AminoAcid aminoAcid) {
        return cTerminalMods.get(aminoAcid);
    }

    public Set<Modification> mapMassToMod(double mass) {
        return massToModification.get(mass);
    }
}
