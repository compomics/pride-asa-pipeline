/* 
 * Copyright 2018 compomics.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.compomics.pride_asa_pipeline.core.logic.inference.modification;

import com.compomics.pride_asa_pipeline.core.model.SpectrumAnnotatorResult;
import com.compomics.pride_asa_pipeline.core.model.modification.ModificationAdapter;
import com.compomics.pride_asa_pipeline.core.model.modification.impl.UtilitiesPTMAdapter;
import com.compomics.pride_asa_pipeline.core.model.modification.source.PRIDEModificationFactory;
import com.compomics.pride_asa_pipeline.core.service.ModificationService;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
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
    private double considerationThreshold = 0.01;
    /**
     * The threshold for fixed modifications
     *
     * @ToDo everything should be variable?
     */
    private final double fixedThreshold = 0.99;
    /**
     * The rates in which modifications occur on the provided identifications
     */
    private Map<Modification, Double> modificationRates = new HashMap<>();
    /**
     * The ptm settings (former modification profile)
     */
    protected PtmSettings ptmSettings;
    /**
     * A list of UNIMOD quant related terms
     */
    private final List<Integer> quantAccessions = Arrays.asList(new Integer[]{258, 259, 267, 367, 687, 365, 866, 730, 532, 730, 533, 731, 739, 738, 737, 984, 985, 1341, 1342});

    public ModificationPredictor(SpectrumAnnotatorResult spectrumAnnotatorResult, ModificationService modificationService) {
        this.spectrumAnnotatorResult = spectrumAnnotatorResult;
        this.modificationService = modificationService;
        inferModifications();
    }

    public ModificationPredictor(SpectrumAnnotatorResult spectrumAnnotatorResult, ModificationService modificationService, OutputStream reportStream) {
        this.spectrumAnnotatorResult = spectrumAnnotatorResult;
        this.modificationService = modificationService;
        inferModifications();
    }

    private void inferModifications() {
        LOGGER.info("Inferring modifications...");
//annotate spectra
        HashMap<String, Boolean> asapMods = new HashMap<>();
        Map<Modification, Integer> prideAsapModificationsMap = modificationService.getUsedModifications(spectrumAnnotatorResult);
        LOGGER.info("Estimating modification rates");
        modificationRates = modificationService.estimateModificationRate(prideAsapModificationsMap, spectrumAnnotatorResult, fixedThreshold);
        boolean allowUnlimitedModifications = true;
        if (prideAsapModificationsMap.size() > 6) {
            considerationThreshold = calculateConsiderationThreshold();
            LOGGER.warn("Warning, it is not recommended to search with more than 6 variable PTMs. A consideration threshold (" + considerationThreshold + ") will be applied to limit the combinations");
            allowUnlimitedModifications = false;
        }

        for (Modification aMod : prideAsapModificationsMap.keySet()) {
            //correct positions for oxidation and pyro-glu
            String amodName = aMod.getName();
            if (allowUnlimitedModifications || modificationRates.get(aMod) >= considerationThreshold) {
                double modificationRate = modificationRates.get(aMod);
                //check for quantmods
                boolean isQuantMod = false;
                for (Integer quantTerm : quantAccessions) {
                    if (aMod.getName().toLowerCase().equalsIgnoreCase(PRIDEModificationFactory.getInstance().getModificationNameFromAccession("UNIMOD:" + quantTerm))) {
                        isQuantMod = true;
                        break;
                    }
                }
                if (isQuantMod && modificationRate < (fixedThreshold)) {
                    LOGGER.error(amodName + " is a quant mod, but was not fixed !");
                } else {
             //       LOGGER.info(amodName + "\t" + modificationRate);
                    asapMods.put(amodName, (modificationRate >= fixedThreshold));
                }
            }
        }
        ModificationAdapter adapter = new UtilitiesPTMAdapter();
        ptmSettings = new PtmSettings();
        HashSet<Double> encounteredMasses = new HashSet<>();
        for (Map.Entry<String, Boolean> aMod : asapMods.entrySet()) {
            ArrayList<String> unknownPTM = new ArrayList<>();
            PTMFactory.getInstance().convertPridePtm(aMod.getKey(), ptmSettings, unknownPTM, false);
            if (!unknownPTM.isEmpty()) {
                LOGGER.info(aMod.getKey() + " is not a standard modification. Converting to utilities object");
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
            } else {
      //          LOGGER.info(aMod.getKey() + " was found in the default PTMs");
            }
        }
    }

    private double calculateConsiderationThreshold() {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (double modificationRate : modificationRates.values()) {
            stats.addValue(modificationRate);
        }

        double threshold = Math.max(0.005, stats.getPercentile(2.5));
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
