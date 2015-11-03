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
 * @author Kenneth Verheggen
 */
public class UniModFactory {

    /**
     * a logger
     */
    private static final Logger LOGGER = Logger.getLogger(UniModFactory.class);
    /**
     * The maximal amount of modifications the factory can load
     */
    private static final int MAX = Integer.MAX_VALUE;
    /**
     * The unimodfactory singleton instance
     */
    private static UniModFactory instance;
    /**
     * The map containing the name of the modification and the
     * unimodmodification object
     */
    private static LinkedHashMap<String, UniModModification> modificationMap = new LinkedHashMap<>();

    public static UniModFactory getInstance() {
        if (instance == null) {
            instance = new UniModFactory();
        }
        return instance;
    }

    private UniModFactory() {

    }

    /**
     * Initialises the factory from an input json file
     *
     * @param inputFile the input file
     * @throws IOException an exception if the file could not be correctly
     * handled
     */
    public void init(File inputFile) throws IOException {
        Collection<UniModModification> fromFile = getFromFile(inputFile);
        for (UniModModification aUniMod : fromFile) {
            modificationMap.put(aUniMod.getPtm().getName(), aUniMod);
        }
    }

    /**
     * Initialises the factory from the webservice
     *
     * @throws IOException an exception if the file could not be correctly
     * handled
     * @throws InterruptedException if the multithreading pool fails
     */
    public void init() throws IOException, InterruptedException {
        Collection<UniModModification> fromFile = getFromPRIDE();
        for (UniModModification aUniMod : fromFile) {
            modificationMap.put(aUniMod.getPtm().getName(), aUniMod);
        }
    }

    /**
     * Returns an instance of a converted modification using the provided
     * adapter
     *
     * @param adapter the modification adapter
     * @param ptmName the modification name
     * @return an instance of a converted modification using the provided
     * adapter
     */
    public Object getModification(ModificationAdapter adapter, String ptmName) {
        return adapter.convertModification(modificationMap.get(ptmName));
    }

    /**
     * Returns a map of modifications
     *
     * @return the modification map
     */
    public LinkedHashMap<String, UniModModification> getModificationMap() {
        return modificationMap;
    }

    /**
     * Returns an ordened set of human modifications from high prevalence to low
     * prevalence
     *
     * @param taxonomyID the taxonomyID to filter on
     * @return the ordened modification set
     */
    public static TreeSet<Modification> getAsapMods(String taxonomyID) {
        TreeSet<Modification> pride_mods = new TreeSet<>();
        try {
            PrideFilter speciesFilter = new PrideFilter(PrideFilterType.speciesFilter, taxonomyID);
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

    /**
     * Returns an ordened set of human modifications from high prevalence to low
     * prevalence
     *
     * @return the ordened modification set for taxonomyID 9606
     */
    public static TreeSet<Modification> getAsapMods() {
        return getAsapMods("9606");
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
