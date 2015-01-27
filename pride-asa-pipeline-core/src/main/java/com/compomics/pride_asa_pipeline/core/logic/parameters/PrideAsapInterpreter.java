package com.compomics.pride_asa_pipeline.core.logic.parameters;

import com.compomics.pride_asa_pipeline.core.logic.FileSpectrumAnnotator;
import com.compomics.pride_asa_pipeline.core.logic.enzyme.EnzymePredictor;
import com.compomics.pride_asa_pipeline.core.logic.modification.PTMMapper;
import com.compomics.pride_asa_pipeline.core.logic.modification.RepositioningModificationType;
import com.compomics.pride_asa_pipeline.core.repository.FileParser;
import com.compomics.pride_asa_pipeline.core.repository.factory.FileParserFactory;
import com.compomics.pride_asa_pipeline.core.service.FileModificationService;
import com.compomics.pride_asa_pipeline.core.spring.ApplicationContextProvider;
import com.compomics.pride_asa_pipeline.model.AASequenceMassUnknownException;
import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.FragmentIonAnnotation;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.preferences.ModificationProfile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.geneontology.oboedit.dataadapter.GOBOParseException;
import org.springframework.context.ApplicationContext;
import org.xmlpull.v1.XmlPullParserException;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;

public abstract class PrideAsapInterpreter {

    protected ApplicationContext applicationContext;
    protected static FileSpectrumAnnotator fileSpectrumAnnotator;
    // parser that handles the identifications
    protected FileParser parser;
    private static final Logger LOGGER = Logger.getLogger(PrideAsapInterpreter.class);
    // input file (PRIDEXML / MZID)
    private File identificationsFile;
    // hashmap of the countable enzymes according to their termini
    private LinkedHashMap<Enzyme, Integer> enzymeCounts;
    // the enzyme that was highlighted as most likely 
    private Enzyme mainEnzyme;
    // the amount of estimated missed cleavages
    private int missedCleavages;
    // charge states (TODO infer this as well?)
    private int maxCharge = 5;
    private int minCharge = 1;
    // the likely mass errors
    private double mostLikelyPrecursorError;
    private double mostLikelyFragIonAcc;

    private final boolean useAbsoluteMassDelta = true;
    private final PrideAsapStats fragmentIonStats = new PrideAsapStats(useAbsoluteMassDelta);
    private final PrideAsapStats precursorStats = new PrideAsapStats(useAbsoluteMassDelta);

    // TODO these modification parameters are now obsolete?
    private double considerationThreshold;
    private double fixedThreshold = 0.8;

    private final double modificationConsiderationConfidence = 2.5;
  
    private Map<Modification, Double> modificationRates;
    private HashMap<String, Boolean> asapMods;
    protected ModificationProfile modProfile;
    private ArrayList<String> unknownMods;

    public PrideAsapInterpreter(File identificationsFile, File peakFile) throws IOException, ClassNotFoundException, MzXMLParsingException, JMzReaderException, Exception {
        init(identificationsFile, peakFile);
    }

    public ModificationProfile getModProfile() {
        return modProfile;
    }

    public void setModProfile(ModificationProfile modProfile) {
        this.modProfile = modProfile;
    }

    private void init(File identificationsFile, File peakFile) throws Exception {
        this.identificationsFile = identificationsFile;
        this.parser = FileParserFactory.getFileParser(identificationsFile);
        parser.attachSpectra(peakFile);
        ApplicationContextProvider.getInstance().setDefaultApplicationContext();
        applicationContext = ApplicationContextProvider.getInstance().getApplicationContext();
        try {
            fileSpectrumAnnotator = (FileSpectrumAnnotator) applicationContext.getBean("fileSpectrumAnnotator");
            LOGGER.debug("Setting up Pride ASAP for interpretation of inputfile");
            LOGGER.debug("Setting up modification-service");
            LOGGER.debug("Annotating spectra");
            fileSpectrumAnnotator.setFileParser(parser);
            fileSpectrumAnnotator.initIdentifications(this.identificationsFile);
            fileSpectrumAnnotator.annotate();
            //try to find the used modifications
            inferModifications();
            //recalibrate errors
            recalibrateMachineAccuraccy();
            try {
                //estimate the used enzyme
                inferEnzyme();
            } catch (FileNotFoundException | XmlPullParserException ex) {
                LOGGER.error("Could not estimate enzyme...:" + ex);
            }

        } catch (XmlPullParserException | GOBOParseException ex) {
            LOGGER.error("Could not infer modifications...:" + ex);
        }
        //   PTMFactory.getInstance().clearFactory();
    }

    public Set<AnalyzerData> getInstrumentsForProject() {
        LOGGER.info("Getting instrument parameters");
        AnalyzerData lAnalyzerData;
        lAnalyzerData = fileSpectrumAnnotator.getExperimentService().getAnalyzerData();
        HashSet<AnalyzerData> lResults = new HashSet<>();
        lResults.add(lAnalyzerData);
        return lResults;
    }

    private void recalibrateMachineAccuraccy() throws IOException {
        List<Identification> experimentIdentifications = fileSpectrumAnnotator.getSpectrumAnnotatorResult().getUnmodifiedPrecursors();
        HashSet<Identification> alreadyProcessedIdentifications = new HashSet<>();
        precursorStats.clear();
        fragmentIonStats.clear();
        for (Identification anIdentification : experimentIdentifications) {
            if (!alreadyProcessedIdentifications.contains(anIdentification)) {
                alreadyProcessedIdentifications.add(anIdentification);
                if (anIdentification.getPipelineExplanationType() != null) {
                    try {
                        List<FragmentIonAnnotation> fragmentIonAnnotations = anIdentification.getAnnotationData().getFragmentIonAnnotations();
                        for (FragmentIonAnnotation anAnnotation : fragmentIonAnnotations) {
                            fragmentIonStats.addValue(anAnnotation.getMass_error());
                        }
                        precursorStats.addValue(Math.abs(anIdentification.getPeptide().calculateMassDelta()));
                    } catch (AASequenceMassUnknownException | NullPointerException e) {
                        //this can happen if there are no unmodified and identified peptides...
                        LOGGER.error("Not able to extract for " + anIdentification);
                    }
                }
            }
        }
        LOGGER.info("Attempting to find best suited precursor accuraccy...");
        mostLikelyPrecursorError = precursorStats.getDropOffValue();
        LOGGER.debug("Most likely precursor accuraccy found at " + mostLikelyPrecursorError + " da (based on slope drop off value)");
        LOGGER.info("Attempting to find best suited fragment ion accuraccy");
        mostLikelyFragIonAcc = fragmentIonStats.getMax();
        LOGGER.debug("Most likely fragment ion accuraccy found at " + mostLikelyPrecursorError + " da (maximum value)");
        System.out.println("Done");
    }

    private void inferModifications() throws XmlPullParserException, IOException, GOBOParseException {
        // FIND NEW MODIFICATIONS       
        LOGGER.info("FINDING PRIDE-ASAP MODIFICATIONS");
        PTMMapper prideToPtmMapper = PTMMapper.getInstance();
        asapMods = new HashMap<>();
        //annotate spectra
        FileModificationService modificationService = (FileModificationService) fileSpectrumAnnotator.getModificationService();
        Map<Modification, Integer> lPrideAsapModificationsMap = modificationService.getUsedModifications(fileSpectrumAnnotator.getSpectrumAnnotatorResult());
        modificationRates = modificationService.estimateModificationRate(lPrideAsapModificationsMap, fileSpectrumAnnotator.getSpectrumAnnotatorResult(), 0.75);
        // Write annotation scores 
        // Check pride discovered mods
        considerationThreshold = calculateConsiderationThreshold();
        for (Modification aMod : lPrideAsapModificationsMap.keySet()) {
            String amodName = aMod.getName().toLowerCase();
            //correct positions for oxidation and pyro-glu
            if (modificationRates.get(aMod) >= considerationThreshold) {
                boolean isFixed = modificationRates.get(aMod) > fixedThreshold;
                boolean needsRemapping = false;
                RepositioningModificationType type = RepositioningModificationType.OXIDATION;
                for (RepositioningModificationType aType : RepositioningModificationType.values()) {
                    if (amodName.toLowerCase().contains(aType.getMark().toLowerCase()) && !needsRemapping) {
                        needsRemapping = true;
                        type = aType;
                    }
                }
                if (needsRemapping) {
                    LOGGER.debug("Remapping pride mod " + type + "to position");
                    try {
                        for (String aString : prideToPtmMapper.correctPrideAsapPositions(aMod, type)) {
                            asapMods.put(aString.toLowerCase(), isFixed);
                        }
                    } catch (NullPointerException e) {
                        LOGGER.error(e);
                    }
                } else {
                    amodName = prideToPtmMapper.lookupRealModName(aMod.getName()).toLowerCase();
                    asapMods.put(amodName, isFixed);
                }
            }
        }
        unknownMods = new ArrayList<>();
        modProfile = prideToPtmMapper.buildUniqueMassModProfile(asapMods, unknownMods, mostLikelyPrecursorError);
    }

    private void inferEnzyme() throws IOException, FileNotFoundException, ClassNotFoundException, XmlPullParserException {
        List<Peptide> completePeptides = fileSpectrumAnnotator.getExperimentService().loadExperimentIdentifications().getCompletePeptides();
        EnzymePredictor predictor = new EnzymePredictor();
        predictor.addPeptideObjects(completePeptides);
        enzymeCounts = predictor.getEnzymeCounts();
        mainEnzyme = predictor.getMainEnzyme(enzymeCounts);
        missedCleavages = predictor.estimateMaxMissedCleavages(mainEnzyme);
    }

    private double calculateConsiderationThreshold() {
        PrideAsapStats stats = new PrideAsapStats(modificationRates.values(), false);
        return Math.max(0.025, stats.getPercentile(modificationConsiderationConfidence));
    }

    public double getConsiderationThreshold() {
        return considerationThreshold;
    }

    public double getFixedThreshold() {
        return fixedThreshold;
    }

    public File getInputFile() {
        return identificationsFile;
    }

    public LinkedHashMap<Enzyme, Integer> getEnzymeCounts() {
        return enzymeCounts;
    }

    public Enzyme getMainEnzyme() {
        return mainEnzyme;
    }

    public int getMostLikelyMissedCleavages() {
        return missedCleavages;
    }

    public int getMaxCharge() {
        return maxCharge;
    }

    public int getMinCharge() {
        return minCharge;
    }

    public double getPrecursorAccuraccy() {
        return PrideAsapStats.round(mostLikelyPrecursorError);
    }

    public double getFragmentIonAccuraccy() {
        return PrideAsapStats.round(mostLikelyFragIonAcc);
    }

    public Map<Modification, Double> getModificationRates() {
        return modificationRates;
    }

    public PrideAsapStats getFragmentIonStats() {
        return fragmentIonStats;
    }

    public PrideAsapStats getPrecursorStats() {
        return precursorStats;
    }

}
