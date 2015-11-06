package com.compomics.pride_asa_pipeline.core.logic.inference.modification;

import com.compomics.pride_asa_pipeline.core.logic.inference.InferenceStatistics;
import com.compomics.pride_asa_pipeline.core.model.SpectrumAnnotatorResult;
import com.compomics.pride_asa_pipeline.core.model.modification.ModificationAdapter;
import com.compomics.pride_asa_pipeline.core.model.modification.impl.UtilitiesPTMAdapter;
import com.compomics.pride_asa_pipeline.core.model.modification.source.PRIDEModificationFactory;
import com.compomics.pride_asa_pipeline.core.service.ModificationService;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class ModificationPredictor {

    /**
     * LOGGER
     */
    private static final Logger LOGGER = Logger.getLogger(ModificationPredictor.class);
    /*
     * The used SpectrumAnnotatorResult
     */
    private final SpectrumAnnotatorResult spectrumAnnotatorResult;
    /*
     * The used modificationService
     */
    private final ModificationService modificationService;
    /**
     * The consideration threshold for modifications. At least this value must
     * carry the modification for it to be considered in the extraction (default
     * = 5%)
     */
    private double considerationThreshold = 0.05;
    /**
     * The threshold for fixed modifications
     */
    private final double fixedThreshold = 0.8;
    /**
     * The confidence in which the modification consideration should lie (using
     * a percentile for this value)
     */
    private final double modificationConsiderationConfidence = 2.5;
    /**
     * The rates in which modifications occur on the provided identifications
     */
    private Map<Modification, Double> modificationRates = new HashMap<>();
    /**
     * The ptm settings (former modification profile)
     */
    protected PtmSettings ptmSettings;
    /**
     * A list of mod terms that are actually MS1 / QUANT terms
     */
    private final List<String> quantTerms = Arrays.asList(new String[]{"itraq", "tmt", "silac"});

    public ModificationPredictor(SpectrumAnnotatorResult spectrumAnnotatorResult, ModificationService modificationService) {
        this.spectrumAnnotatorResult = spectrumAnnotatorResult;
        this.modificationService = modificationService;
        inferModifications();
    }

    private void inferModifications() {
        LOGGER.info("Inferring modifications...");
//annotate spectra
        HashMap<String, Boolean> asapMods = new HashMap<>();
        Map<Modification, Integer> lPrideAsapModificationsMap = modificationService.getUsedModifications(spectrumAnnotatorResult);
        LOGGER.info("Estimating modification rates");
        modificationRates = modificationService.estimateModificationRate(lPrideAsapModificationsMap, spectrumAnnotatorResult, fixedThreshold);

// Write annotation scores 
        // Check pride discovered mods
        considerationThreshold = calculateConsiderationThreshold();
        for (Modification aMod : lPrideAsapModificationsMap.keySet()) {
            //correct positions for oxidation and pyro-glu
            String amodName = aMod.getName();
            if (modificationRates.get(aMod) >= considerationThreshold) {
                double modificationRate = modificationRates.get(aMod);
                //check for quantmods
                boolean isQuantMod = false;
                for (String quantTerm : quantTerms) {
                    if (aMod.getName().toLowerCase().contains(quantTerm)) {
                        isQuantMod = true;
                        break;
                    }
                }
                //0.3 is an arbitrary value TODO verify this !!!
                if (isQuantMod && modificationRate < (0.3)) {
                    LOGGER.error(amodName + " is a quant mod, but was not fixed !");
                } else {
                    LOGGER.info(amodName + "\t" + modificationRate);
                    asapMods.put(amodName, (modificationRate >= fixedThreshold));
                }
            }
        }
        LOGGER.info("Converting modifications to utilities objects");
        ModificationAdapter adapter = new UtilitiesPTMAdapter();
        HashSet<Double> encounteredMasses = new HashSet<>();
        for (Map.Entry<String, Boolean> aMod : asapMods.entrySet()) {
            PTM aUtilitiesMod = (PTM) PRIDEModificationFactory.getInstance().getModification(adapter, aMod.getKey());
            if (!encounteredMasses.contains(aUtilitiesMod.getRoundedMass())) {
                encounteredMasses.add(aUtilitiesMod.getRoundedMass());
                if (aMod.getValue()) {
                    ptmSettings.addFixedModification(aUtilitiesMod);
                } else {
                    ptmSettings.addVariableModification(aUtilitiesMod);
                }
            } else {
                LOGGER.warn("Duplicate mass, " + aMod.getKey() + " will be ignored");
            }
        }
    }

    private double calculateConsiderationThreshold() {
        InferenceStatistics stats = new InferenceStatistics(modificationRates.values(), false);
        double threshold = Math.max(0.025, stats.getPercentile(modificationConsiderationConfidence));
        System.out.println("ConsiderationThreshold = " + threshold);
        return threshold;
    }

    public double getConsiderationThreshold() {
        return considerationThreshold;
    }

    public double getFixedThreshold() {
        return fixedThreshold;
    }

    public Map<Modification, Double> getModificationRates() {
        return modificationRates;
    }

    public PtmSettings getPtmSettings() {
        return ptmSettings;
    }

}
