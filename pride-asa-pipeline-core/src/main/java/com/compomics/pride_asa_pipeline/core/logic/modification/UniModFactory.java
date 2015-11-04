package com.compomics.pride_asa_pipeline.core.logic.modification;

import com.compomics.pride_asa_pipeline.core.logic.modification.conversion.ModificationAdapter;
import com.compomics.pride_asa_pipeline.core.logic.modification.conversion.UniModModification;
import com.compomics.pride_asa_pipeline.core.logic.modification.conversion.impl.AsapModificationAdapter;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.util.io.json.JsonMarshaller;
import com.compomics.util.pride.PrideWebService;
import com.compomics.util.pride.prideobjects.webservice.query.PrideFilter;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import uk.ac.ebi.pridemod.ModReader;
import uk.ac.ebi.pridemod.model.UniModPTM;

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
    /**
     * The mode (online or offline)
     */
    private final INIT_MODE mode;

    protected static enum INIT_MODE {

        ONLINE, OFFLINE;
    }

    public static UniModFactory getInstance(INIT_MODE mode) {
        if (instance == null) {
            instance = new UniModFactory(mode);
        }
        return instance;
    }

    public static UniModFactory getInstance() {
        if (instance == null) {
            instance = new UniModFactory(INIT_MODE.OFFLINE);
        }
        return instance;
    }

    private UniModFactory(INIT_MODE mode) {
        this.mode = mode;
        try {
            if (mode.equals(INIT_MODE.OFFLINE)) {
                //TODO TURN THIS INTO A STREAM
                File jsonLib = new ClassPathResource("resources/pride_mods.json").getFile();
                init(jsonLib);
              //  jsonLib.delete();
            } else {
                init();
            }
        } catch (IOException | InterruptedException ex) {
            LOGGER.error(ex);
        }
    }

    /**
     * Initialises the factory from an input json file
     *
     * @param inputFile the input file
     * @throws IOException an exception if the file could not be correctly
     * handled
     */
    private void init(File inputFile) throws IOException {
        Collection<UniModModification> fromFile = getFromFile(inputFile);
        for (UniModModification aUniMod : fromFile) {
            modificationMap.put(aUniMod.getName(), aUniMod);
        }
    }

    /**
     * Initialises the factory from the webservice
     *
     * @throws IOException an exception if the file could not be correctly
     * handled
     * @throws InterruptedException if the multithreading pool fails
     */
    private void init() throws IOException, InterruptedException {
        Collection<UniModModification> fromFile = getFromPRIDE();
        for (UniModModification aUniMod : fromFile) {
            modificationMap.put(aUniMod.getName(), aUniMod);
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
     * Returns an ordened set of modifications from high prevalence to low
     * prevalence
     *
     * @return the ordened modification set
     */
    public static LinkedList<Modification> getAsapMods() {
        LinkedList<Modification> pride_mods = new LinkedList<>();
        for (UniModModification aMod : modificationMap.values()) {
            pride_mods.add(new AsapModificationAdapter().convertModification(aMod));
        }
        return pride_mods;
    }

    private static TreeSet<UniModModification> getFromFile(File inputFile) throws IOException {
        JsonMarshaller marshaller = new JsonMarshaller();
        LOGGER.debug("Getting modifications from file...");
        java.lang.reflect.Type type = new TypeToken<TreeSet<UniModModification>>() {
        }.getType();
        return (TreeSet<UniModModification>) marshaller.fromJson(type, inputFile);
    }

    public static void main(String[] args) throws IOException {
        UniModFactory factory = getInstance(INIT_MODE.OFFLINE);
        LinkedHashMap<String, UniModModification> modificationMap1 = factory.getModificationMap();
        TreeSet<UniModModification> mods = new TreeSet<>();
        for (UniModModification aMod : modificationMap1.values()) {
            mods.add(aMod);
        }
        JsonMarshaller marshaller = new JsonMarshaller();
        File file = new File("C:\\Users\\Kenneth\\Desktop\\MzID_Test\\pride_mods.json");
        marshaller.saveObjectToJson(mods, file);
        TreeSet<UniModModification> fromPRIDE = getFromFile(file);
        System.out.println(fromPRIDE.first().getAccession());
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
                    Double monoDeltaMass = get.getMonoDeltaMass();
                    Double avgDeltaMass = get.getAveDeltaMass();
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

    public static LinkedList<Object> orderModificationsToPrevalence(Collection<String> ptmNames, ModificationAdapter adapter) {
        TreeSet<UniModModification> orderedModifications = orderModificationsToPrevalence(ptmNames);
        LinkedList<Object> orderedConvertedModifications = new LinkedList<>();
        for (UniModModification aModification : orderedModifications) {
            orderedConvertedModifications.add(adapter.convertModification(aModification));
        }
        return orderedConvertedModifications;
    }

    public static TreeSet<UniModModification> orderModificationsToPrevalence(Collection<String> ptmNames) {
        TreeSet<UniModModification> orderedModifications = new TreeSet<>();
        for (String aPTMName : ptmNames) {
            UniModModification uniModModification = modificationMap.get(aPTMName);
            if (uniModModification != null) {
                orderedModifications.add(uniModModification);
            }
        }
        return orderedModifications;
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
                System.out.println("Querying " + aPTM.getName());
                size = PrideWebService.getProjectCount(aPTM.getAccession().replace(":", "-").replace(" ", "_").replace("/", "").replace("\\", ""));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (aPTM instanceof UniModPTM) {
                return new UniModModification(aPTM, size);
            } else {
                return new UniModModification(aPTM, size);
            }

        }
    }

}
