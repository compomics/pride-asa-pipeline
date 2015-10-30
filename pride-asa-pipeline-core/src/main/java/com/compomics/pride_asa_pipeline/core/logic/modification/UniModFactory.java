/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.logic.modification;

import com.compomics.pride_asa_pipeline.core.logic.modification.conversion.ModificationAdapter;
import com.compomics.pride_asa_pipeline.core.logic.modification.conversion.UniModModification;
import com.compomics.pride_asa_pipeline.core.logic.modification.conversion.impl.AsapModificationAdapter;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.util.io.json.JsonMarshaller;
import com.compomics.util.pride.PrideWebService;
import com.compomics.util.pride.prideobjects.webservice.query.PrideFilter;
import com.compomics.util.pride.prideobjects.webservice.query.PrideFilterType;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import uk.ac.ebi.pridemod.ModReader;

/**
 *
 * @author Kenneth
 */
public class UniModFactory {

    private static final Logger LOGGER = Logger.getLogger(UniModFactory.class);
    private static final int MAX = Integer.MAX_VALUE;
    private static UniModFactory instance;
    private static LinkedHashMap<String, UniModModification> modificationMap = new LinkedHashMap<>();

    public static UniModFactory getInstance() {
        if (instance == null) {
            instance = new UniModFactory();
        }
        return instance;
    }

    private UniModFactory() {

    }

    public void init(File inputFile) throws IOException {
        Collection<UniModModification> fromFile = getFromFile(inputFile);
        for (UniModModification aUniMod : fromFile) {
            modificationMap.put(aUniMod.getPtm().getName(), aUniMod);
        }
    }

    public void init() throws IOException, InterruptedException {
        Collection<UniModModification> fromFile = getFromPRIDE();
        for (UniModModification aUniMod : fromFile) {
            modificationMap.put(aUniMod.getPtm().getName(), aUniMod);
        }
    }

    public Object getModification(ModificationAdapter adapter, String ptmName) {
        return adapter.convertModification(modificationMap.get(ptmName));
    }

    public LinkedHashMap<String, UniModModification> getModificationMap() {
        return modificationMap;
    }

    public static TreeSet<Modification> getAsapMods() {
        TreeSet<Modification> pride_mods = new TreeSet<>();
        try {
            PrideFilter speciesFilter = new PrideFilter(PrideFilterType.speciesFilter, "9606");
            ArrayList<PrideFilter> filters = new ArrayList<>();
            filters.add(speciesFilter);
            TreeSet<UniModModification> uniMods = UniModFactory.getFromPRIDE(filters);
            for (UniModModification aMod : uniMods) {
                pride_mods.add(new AsapModificationAdapter().convertModification(aMod));
            }
        } catch (InterruptedException | IOException ex) {
            LOGGER.error("Could not load modifications from the webserver : " + ex);
        }
        return pride_mods;
    }

    private static Collection<UniModModification> getFromFile(File inputFile) throws IOException {
        JsonMarshaller marshaller = new JsonMarshaller();
        LOGGER.debug("Getting modifications from file...");
        java.lang.reflect.Type type = new TypeToken<List<UniModModification>>() {
        }.getType();
        return (Collection<UniModModification>) marshaller.fromJson(type, inputFile);
    }

    public static TreeSet<UniModModification> getFromPRIDE() throws InterruptedException, IOException {
        return getFromPRIDE(new ArrayList<PrideFilter>());
    }

    public static TreeSet<UniModModification> getFromPRIDE(File outputFile) throws InterruptedException, IOException {
        return getFromPRIDE(outputFile, new ArrayList<PrideFilter>());
    }

    public static TreeSet<UniModModification> getFromPRIDE(Collection<PrideFilter> prideFilters) throws InterruptedException, IOException {
        ModReader modReader = ModReader.getInstance();
        System.out.println("Looking for modifications...;");
        ExecutorService executors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        //   ExecutorService executors = Executors.newFixedThreadPool(1);
        ArrayList<Future<UniModModification>> finishedMods = new ArrayList<>();
        System.out.println("Gathering modification occurences from pride...");
        for (uk.ac.ebi.pridemod.model.PTM aPTM : modReader.getPTMListByPatternName("")) {
            if (finishedMods.size() != MAX) {
                finishedMods.add(executors.submit(new UniModOccurenceGetter(aPTM, prideFilters)));
            } else {
                break;
            }
        }
        executors.shutdown();
        executors.awaitTermination(100, TimeUnit.DAYS);
        TreeSet<UniModModification> mods = new TreeSet<>();
        for (Future<UniModModification> aUniModFuture : finishedMods) {
            try {
                UniModModification get = aUniModFuture.get();
                if (get.getFrequency() > 0 | prideFilters.isEmpty()) {
                    Double monoDeltaMass = get.getPtm().getMonoDeltaMass();
                    Double avgDeltaMass = get.getPtm().getAveDeltaMass();
                    if (monoDeltaMass != null && avgDeltaMass != null && monoDeltaMass != 0.0 && avgDeltaMass != 0.0) {
                        mods.add(get);
                    }
                }
            } catch (ExecutionException ex) {
                LOGGER.error(ex);
                ex.printStackTrace();
            }
        }
        return mods;
    }

    public static TreeSet<UniModModification> getFromPRIDE(File outputFile, Collection<PrideFilter> prideFilters) throws InterruptedException, IOException {
        TreeSet<UniModModification> mods = getFromPRIDE(prideFilters);
        outputFile.getParentFile().mkdirs();
        JsonMarshaller marshaller = new JsonMarshaller();
        marshaller.saveObjectToJson(mods, outputFile);
        return mods;
    }

    private static class UniModOccurenceGetter implements Callable<UniModModification> {

        private final uk.ac.ebi.pridemod.model.PTM aPTM;
        private Collection<PrideFilter> prideFilters = new ArrayList<>();

        private UniModOccurenceGetter(uk.ac.ebi.pridemod.model.PTM aPTM) {
            this.aPTM = aPTM;
        }

        private UniModOccurenceGetter(uk.ac.ebi.pridemod.model.PTM aPTM, Collection<PrideFilter> prideFilters) {
            this.aPTM = aPTM;
            this.prideFilters.addAll(prideFilters);
        }

        //convert ptm to pride asap ptm?
        @Override
        public UniModModification call() {
            int size = 0;
            try {
                size = PrideWebService.getProjectCount(aPTM.getName().replace(":", "-").replace(" ", "_").replace("/", "").replace("\\", ""));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new UniModModification(aPTM, size);
        }
    }

}
