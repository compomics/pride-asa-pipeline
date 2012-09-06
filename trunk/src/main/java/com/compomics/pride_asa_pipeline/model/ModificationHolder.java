package com.compomics.pride_asa_pipeline.model;

import java.util.*;

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
        nonTerminalMods = new EnumMap<AminoAcid, Set<Modification>>(AminoAcid.class);
        nTerminalMods = new EnumMap<AminoAcid, Set<Modification>>(AminoAcid.class);
        cTerminalMods = new EnumMap<AminoAcid, Set<Modification>>(AminoAcid.class);
        massToModification = new HashMap<Double, Set<Modification>>();
    }

    public void addModifications(Collection<Modification> modifications) {
        for (Modification modification : modifications) {
            addModification(modification);
        }
    }

    public void addModification(Modification modification) {
        //add the given modification to its corresponding map
        if (modification.getLocation() == Modification.Location.NON_TERMINAL) {
            //add this modification to all non-terminal amino acids that could be affected
            for (AminoAcid aa : modification.getAffectedAminoAcids()) {
                if (nonTerminalMods.get(aa) == null) {
                    nonTerminalMods.put(aa, new HashSet<Modification>());
                }
                nonTerminalMods.get(aa).add(modification);
            }
        } else if (modification.getLocation() == Modification.Location.N_TERMINAL) {
            //add this modification to all N-terminal amino acids that could be affected
            for (AminoAcid aa : modification.getAffectedAminoAcids()) {
                if (nTerminalMods.get(aa) == null) {
                    nTerminalMods.put(aa, new HashSet<Modification>());
                }
                nTerminalMods.get(aa).add(modification);
            }
        } else if (modification.getLocation() == Modification.Location.C_TERMINAL) {
            //add this modification to all C-terminal amino acids that could be affected
            for (AminoAcid aa : modification.getAffectedAminoAcids()) {
                if (cTerminalMods.get(aa) == null) {
                    cTerminalMods.put(aa, new HashSet<Modification>());
                }
                cTerminalMods.get(aa).add(modification);
            }
        } else {
            //sanity check
            throw new IllegalStateException("Illegal amino acid position!! " + modification.getLocation());
        }

        //update the mass to modification map
        Double mass = modification.getMassShift();
        //if not entry exists yet for the given mass, add one
        if (massToModification.get(mass) == null) {
            Set<Modification> mods = new HashSet<Modification>();
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
        Set<Modification> all = new HashSet<Modification>();
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

    public Set<Modification> getNonTerminalMods(AminoAcid aa) {
        return nonTerminalMods.get(aa);
    }

    public Set<Modification> getNterminalMods(AminoAcid aa) {
        return nTerminalMods.get(aa);
    }

    public Set<Modification> getCterminalMods(AminoAcid aa) {
        return cTerminalMods.get(aa);
    }

    public Set<Modification> mapMassToMod(double mass) {
        return massToModification.get(mass);
    }
}
