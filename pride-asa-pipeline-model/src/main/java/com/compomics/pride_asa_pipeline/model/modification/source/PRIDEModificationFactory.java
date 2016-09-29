package com.compomics.pride_asa_pipeline.model.modification.source;


import com.compomics.pride_asa_pipeline.model.modification.ModificationAdapter;
import com.compomics.pride_asa_pipeline.model.modification.PRIDEModification;
import com.compomics.pride_asa_pipeline.model.modification.impl.AsapModificationAdapter;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.ParameterExtractionException;
import com.compomics.util.io.json.JsonMarshaller;
import com.compomics.util.pride.PrideWebService;
import com.compomics.util.pride.prideobjects.webservice.query.PrideFilter;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;
import uk.ac.ebi.pridemod.ModReader;
import uk.ac.ebi.pridemod.model.UniModPTM;

/**
 *
 * @author Kenneth Verheggen
 */
public class PRIDEModificationFactory {

    /**
     * a logger
     */
    private static final Logger LOGGER = Logger.getLogger(PRIDEModificationFactory.class);
    /**
     * The maximal amount of modifications the factory can load
     */
    private static final int MAX = Integer.MAX_VALUE;
    /**
     * The unimodfactory singleton instance
     */
    private static PRIDEModificationFactory instance;
    /**
     * The map containing the name of the modification and the
     * unimodmodification object
     */
    private static LinkedHashMap<String, PRIDEModification> modificationNameMap = new LinkedHashMap<>();
    /**
     * The map containing the accession of the modification and the
     * unimodmodification name
     */
    private static BidiMap modificationAccessionMap = new DualHashBidiMap();
    /**
     * the minimal mass a modification can have
     */
    private static double minimalModMass;
    /**
     * the maximal mass for a modification
     */
    private static double maximalModMass;
    /**
     * The cache for converted modifications so they don't have to be converted
     * every single time
     */
    private static HashMap<String, Object> modificationCache = new HashMap<>();
    /**
     * The mode (online or offline)
     */
    private final INIT_MODE mode;
    private File modFile;

    protected static enum INIT_MODE {

        ONLINE, OFFLINE;
    }

    public static PRIDEModificationFactory getInstance(INIT_MODE mode) {
        if (instance == null) {
            instance = new PRIDEModificationFactory(mode);
        }
        return instance;
    }

    public static PRIDEModificationFactory getInstance() {
        if (instance == null) {
            instance = new PRIDEModificationFactory(INIT_MODE.OFFLINE);
        }
        return instance;
    }

    public static PRIDEModificationFactory getInstance(File modFile) {
        if (instance == null) {
            instance = new PRIDEModificationFactory(modFile);
        }
        return instance;
    }

    private PRIDEModificationFactory(File modFile) {
        this.mode = INIT_MODE.OFFLINE;
        LOGGER.info("Loading modification configuration");
        try (InputStream jsonLib = new FileInputStream(modFile);
                OutputStream out = new FileOutputStream(modFile);) {
            IOUtils.copy(jsonLib, out);
            init(modFile);
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
    }

    public static void main(String[] args) {
        PRIDEModificationFactory instance1 = PRIDEModificationFactory.getInstance(INIT_MODE.OFFLINE);
        System.out.println("Found " + instance1.getModificationMap().size() + " mods");
    }

    private PRIDEModificationFactory(INIT_MODE mode) {
        this.mode = mode;
        try {
            if (mode.equals(INIT_MODE.OFFLINE)) {
                LOGGER.info("Loading modification configuration");
                if (modFile == null) {
                    modFile = File.createTempFile("pride_mods", ".tsv");
                    modFile.deleteOnExit();
                }
                try (InputStream jsonLib = getClass().getResourceAsStream("/resources/pride_mods.json");
                        OutputStream out = new FileOutputStream(modFile);) {
                    IOUtils.copy(jsonLib, out);
                    init(modFile);
                }
                //  jsonLib.delete();
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
        Collection<PRIDEModification> fromFile = getFromFile(inputFile);
        for (PRIDEModification aUniMod : fromFile) {
            modificationNameMap.put(aUniMod.getName(), aUniMod);
            modificationAccessionMap.put(aUniMod.getAccession(), aUniMod.getName());
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
        Collection<PRIDEModification> fromFile = getFromPRIDE();
        for (PRIDEModification aUniMod : fromFile) {
            modificationNameMap.put(aUniMod.getName(), aUniMod);
            modificationAccessionMap.put(aUniMod.getAccession(), aUniMod.getName());
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
     * @throws
     * com.compomics.pride_asa_pipeline.core.exceptions.ParameterExtractionException
     */
    public Object getModification(ModificationAdapter adapter, String ptmName) throws ParameterExtractionException {
        if (!modificationCache.containsKey(ptmName)) {
            LOGGER.info(ptmName + " was cached");
            modificationCache.put(ptmName, adapter.convertModification(modificationNameMap.get(refactorName(ptmName))));
        }
        return modificationCache.get(ptmName);
    }

    //THIS METHOD HAS TO DISSAPEAR OVER TIME, IT IS PURELY MAPPING THAT IS NOT YET IN COMPOMICS UTILITIES
    private String refactorName(String ptmName) {
        if (ptmName.equalsIgnoreCase("iodoacetamide -site C")) {
            return "iodoacetamide - site C";
        }
        return ptmName;
    }

    /**
     * Returns an instance of a converted modification using the provided
     * adapter
     *
     * @param adapter the modification adapter
     * @param ptmAccession the modification accession
     * @return an instance of a converted modification using the provided
     * adapter
     */
    public Object getModificationFromAccession(ModificationAdapter adapter, String ptmAccession) throws ParameterExtractionException {
        String modName;
        Object convertModification = null;
        if ((modName = (String) modificationAccessionMap.get(ptmAccession)) != null) {
            convertModification = adapter.convertModification(modificationNameMap.get(modName));
        }
        return convertModification;
    }

    /**
     * Returns the loaded modification name for an accession
     *
     * @param ptmAccession the modification accession
     * @return the loaded modification name for an accession
     */
    public String getModificationNameFromAccession(String ptmAccession) {
        return (String) modificationAccessionMap.get(ptmAccession);
    }

    /**
     * Returns the loaded modification accession for a name
     *
     * @param ptmName the modification name
     * @return the loaded modification accession for a name
     */
    public String getModificationAccessionFromName(String ptmName) {
        return (String) modificationAccessionMap.getKey(ptmName);
    }

    /**
     * Returns a map of modifications
     *
     * @return the modification map
     */
    public LinkedHashMap<String, PRIDEModification> getModificationMap() {
        return modificationNameMap;
    }

    /**
     * Returns a map of modifications
     *
     * @return the modification map
     */
    public BidiMap getAccessionMapping() {
        return modificationAccessionMap;
    }

    /**
     * Returns an ordened set of modifications from high prevalence to low
     * prevalence
     *
     * @return the ordened modification set
     */
    public static LinkedList<Modification> getAsapMods() {
        return getAsapMods(modificationNameMap.size());
    }

    /**
     * Returns an ordened set of modifications from high prevalence to low
     * prevalence
     *
     * @return the ordened modification set
     */
    public static LinkedList<Modification> getAsapMods(int nr) {
        LinkedList<Modification> pride_mods = new LinkedList<>();
        DescriptiveStatistics massStats = new DescriptiveStatistics();
        int count = nr;
        for (PRIDEModification aMod : modificationNameMap.values()) {
            Modification convertModification = new AsapModificationAdapter().convertModification(aMod);
            pride_mods.add(convertModification);
            massStats.addValue(aMod.getMonoDeltaMass());
            count--;
            if (count == 0) {
                break;
            }
        }
        minimalModMass = massStats.getMin();
        maximalModMass = massStats.getMax();
        return pride_mods;
    }

    public static double getMinimalModMass() {
        return minimalModMass;
    }

    public static double getMaximalModMass() {
        return maximalModMass;
    }

    private static TreeSet<PRIDEModification> getFromFile(File inputFile) throws IOException {
        JsonMarshaller marshaller = new JsonMarshaller();
        LOGGER.debug("Getting modifications from file...");
        java.lang.reflect.Type type = new TypeToken<TreeSet<PRIDEModification>>() {
        }.getType();
        return (TreeSet<PRIDEModification>) marshaller.fromJson(type, inputFile);
    }

    public static void generatePRIDEModJson(File jsonFile) throws IOException {
        PRIDEModificationFactory factory = getInstance(INIT_MODE.ONLINE);
        //load the modifications
        LinkedHashMap<String, PRIDEModification> modificationMap1 = factory.getModificationMap();
        //sort them according to frequency (the default comparator)
        TreeSet<PRIDEModification> mods = new TreeSet<>();
        for (PRIDEModification aMod : modificationMap1.values()) {
            mods.add(aMod);
        }
        //save them to a file
        JsonMarshaller marshaller = new JsonMarshaller();
        marshaller.saveObjectToJson(mods, jsonFile);
    }

    public static TreeSet<PRIDEModification> getFromPRIDE() throws InterruptedException, IOException {
        return getFromPRIDE(new ArrayList<PrideFilter>());
    }

    public static TreeSet<PRIDEModification> getFromPRIDE(File outputFile) throws InterruptedException, IOException {
        return getFromPRIDE(outputFile, new ArrayList<PrideFilter>());
    }

    public static TreeSet<PRIDEModification> getFromPRIDE(Collection<PrideFilter> prideFilters) throws InterruptedException, IOException {
        ModReader modReader = ModReader.getInstance();
        System.out.println("Looking for modifications...;");
        ExecutorService executors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        //   ExecutorService executors = Executors.newFixedThreadPool(1);
        ArrayList<Future<PRIDEModification>> finishedMods = new ArrayList<>();
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
        TreeSet<PRIDEModification> mods = new TreeSet<>();
        for (Future<PRIDEModification> aUniModFuture : finishedMods) {
            try {
                PRIDEModification get = aUniModFuture.get();
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

    public static TreeSet<PRIDEModification> getFromPRIDE(File outputFile, Collection<PrideFilter> prideFilters) throws InterruptedException, IOException {
        TreeSet<PRIDEModification> mods = getFromPRIDE(prideFilters);
        outputFile.getParentFile().mkdirs();
        JsonMarshaller marshaller = new JsonMarshaller();
        marshaller.saveObjectToJson(mods, outputFile);
        return mods;
    }

    public static LinkedList<Object> orderModificationsToPrevalence(Collection<String> ptmNames, ModificationAdapter adapter) throws ParameterExtractionException {
        TreeSet<PRIDEModification> orderedModifications = orderModificationsToPrevalence(ptmNames);
        LinkedList<Object> orderedConvertedModifications = new LinkedList<>();
        for (PRIDEModification aModification : orderedModifications) {
            orderedConvertedModifications.add(adapter.convertModification(aModification));
        }
        return orderedConvertedModifications;
    }

    public static TreeSet<PRIDEModification> orderModificationsToPrevalence(Collection<String> ptmNames) {
        TreeSet<PRIDEModification> orderedModifications = new TreeSet<>();
        for (String aPTMName : ptmNames) {
            PRIDEModification uniModModification = modificationNameMap.get(aPTMName);
            if (uniModModification != null) {
                orderedModifications.add(uniModModification);
            }
        }
        return orderedModifications;
    }

    private static class UniModOccurenceGetter implements Callable<PRIDEModification> {

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
        public PRIDEModification call() {
            int size = 0;
            try {
                System.out.println("Querying " + aPTM.getName());
                size = PrideWebService.getProjectCount(aPTM.getAccession().replace(":", "-").replace(" ", "_").replace("/", "").replace("\\", ""));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (aPTM instanceof UniModPTM) {
                return new PRIDEModification(aPTM, size);
            } else {
                return new PRIDEModification(aPTM, size);
            }

        }
    }

}