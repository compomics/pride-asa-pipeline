package com.compomics.pride_asa_pipeline.core.logic.inference.parameters;

import com.compomics.pride_asa_pipeline.core.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.core.logic.DbSpectrumAnnotator;
import com.compomics.pride_asa_pipeline.core.logic.inference.enzyme.EnzymePredictor;
import com.compomics.pride_asa_pipeline.core.logic.inference.modification.source.PRIDEModificationFactory;
import com.compomics.pride_asa_pipeline.core.logic.inference.modification.ModificationAdapter;
import com.compomics.pride_asa_pipeline.core.logic.inference.modification.impl.UtilitiesPTMAdapter;
import com.compomics.pride_asa_pipeline.core.service.ModificationService;
import com.compomics.pride_asa_pipeline.core.spring.ApplicationContextProvider;
import com.compomics.pride_asa_pipeline.model.FragmentIonAnnotation;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.apache.log4j.Logger;
import org.geneontology.oboedit.dataadapter.GOBOParseException;
import org.xmlpull.v1.XmlPullParserException;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;

public abstract class PrideAsapInterpreter {

    /**
     * The spectrum annotator
     */
    protected static DbSpectrumAnnotator spectrumAnnotator;
    /**
     * A logger
     */
    private static final Logger LOGGER = Logger.getLogger(PrideAsapInterpreter.class);
    /**
     * The best fitting enzyme
     */
    private Enzyme mainEnzyme;
    /**
     * The amount of missed cleavages (default = 2)
     */
    private int missedCleavages = 2;
    /**
     * The ratio of missed cleavages
     */
    double missedCleavageRatio;
    /**
     * An ordened set of the encountered charges
     */
    private final TreeSet<Integer> encounteredCharges = new TreeSet<>();
    /**
     * The most likely inferred precursor error (default to 0.6)
     */
    private double mostLikelyPrecursorError = 0.6;
    /**
     * The most likely inferred fragment ion error (default to 0.6)
     */
    private double mostLikelyFragIonAcc = 0.6;
    /**
     * Boolean indicating to use absolute mass errors in the statistical
     * inference
     */
    private final boolean useAbsoluteMassDelta = true;
    /**
     * An statistics object for the fragment ions
     */
    private PrideAsapStats fragmentIonStats = new PrideAsapStats(useAbsoluteMassDelta);
    /**
     * An statistics object for the precursor ion stats
     */
    private PrideAsapStats precursorStats = new PrideAsapStats(useAbsoluteMassDelta);
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
     * A full list of complete peptide objects
     */
    private List<Peptide> completePeptides;
    /**
     * A list of mod terms that are actually MS1 / QUANT terms
     */
    private final List<String> quantTerms = Arrays.asList(new String[]{"itraq", "tmt", "silac"});

    /**
     * Creates a new interpreter
     *
     * @param assay the assay to interpret
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws MzXMLParsingException
     * @throws JMzReaderException
     */
    public PrideAsapInterpreter(String assay) throws IOException, ClassNotFoundException, MzXMLParsingException, JMzReaderException {
        init(assay);
    }

    public PtmSettings getPtmSettings() {
        return ptmSettings;
    }

    public void setModProfile(PtmSettings modProfile) {
        this.ptmSettings = modProfile;
    }

    private void init(String assay) throws IOException, ClassNotFoundException, MzXMLParsingException, JMzReaderException {
        try {
            //load the spectrumAnnotator ---> make sure to use the right springXMLConfig using the webservice repositories
            ApplicationContextProvider.getInstance().setDefaultApplicationContext();
            spectrumAnnotator = (DbSpectrumAnnotator) ApplicationContextProvider.getInstance().getBean("dbSpectrumAnnotator");
            //get assay
            spectrumAnnotator.initIdentifications(assay);
            LOGGER.info("Spectrumannotator delivered was initialized");
            spectrumAnnotator.annotate(assay);
            //try to find the used modifications
            inferModifications();
            //recalibrate errors
            recalibrateMachineAccuraccy();
            try {
                //estimate the used enzyme
                inferEnzyme();
            } catch (FileNotFoundException | XmlPullParserException ex) {
                LOGGER.error("Could not estimate enzyme...:" + ex);
                ex.printStackTrace();
            }
        } catch (XmlPullParserException | GOBOParseException ex) {
            LOGGER.error("Could not infer modifications...:" + ex);
            ex.printStackTrace();
        }

    }

    private void recalibrateMachineAccuraccy() throws IOException {
        List<Identification> experimentIdentifications = spectrumAnnotator.getSpectrumAnnotatorResult().getUnmodifiedPrecursors();
        HashSet<Identification> alreadyProcessedIdentifications = new HashSet<>();
        precursorStats.clear();
        fragmentIonStats.clear();
        for (Identification anIdentification : experimentIdentifications) {
            encounteredCharges.add(anIdentification.getPeptide().getCharge());
            if (!alreadyProcessedIdentifications.contains(anIdentification)) {
                alreadyProcessedIdentifications.add(anIdentification);
                if (anIdentification.getPipelineExplanationType() != null) {
                    try {
                        List<FragmentIonAnnotation> fragmentIonAnnotations = anIdentification.getAnnotationData().getFragmentIonAnnotations();
                        for (FragmentIonAnnotation anAnnotation : fragmentIonAnnotations) {
                            double frag_mass_error = anAnnotation.getMass_error();
                            //C13 peak
                            if (frag_mass_error < 0.8) {
                                fragmentIonStats.addValue(frag_mass_error);
                            }
                        }
                    } catch (NullPointerException e) {
                        //this can happen if there are no unmodified and identified peptides...
                        LOGGER.error("Not able to extract for " + anIdentification);
                    }
                }
            }
        }
        LOGGER.info("Attempting to find best suited precursor accuraccy (both sides)...");
        precursorStats = spectrumAnnotator.getMassDeltaExplainer().getExplainedMassDeltas();
        mostLikelyPrecursorError = precursorStats.calculateOptimalMassError();
        if (mostLikelyPrecursorError == Double.NaN
                || mostLikelyPrecursorError == Double.NEGATIVE_INFINITY
                || mostLikelyPrecursorError == Double.NEGATIVE_INFINITY) {
            mostLikelyPrecursorError = PropertiesConfigurationHolder.getInstance().getDouble("massrecalibrator.default_error_tolerance");
            ;
        }
        LOGGER.info("Most likely precursor accuraccy found at " + mostLikelyPrecursorError);

        LOGGER.info("Attempting to find best suited fragment ion accuraccy");

        if (fragmentIonStats.getValues().length == 0) {
            fragmentIonStats = precursorStats;
        } else {
            //calculate the optimal mass ?
            mostLikelyFragIonAcc = fragmentIonStats.calculateOptimalMassError();
        }
        if (mostLikelyFragIonAcc == Double.NaN
                || mostLikelyFragIonAcc == Double.NEGATIVE_INFINITY
                || mostLikelyFragIonAcc == Double.POSITIVE_INFINITY) {
            mostLikelyFragIonAcc = PropertiesConfigurationHolder.getInstance().getDouble("massrecalibrator.default_error_tolerance");
        }
        LOGGER.info("Most likely fragment ion accuraccy found at " + mostLikelyFragIonAcc);
        System.out.println("Done");
    }

    private void inferModifications() throws XmlPullParserException, IOException, GOBOParseException {
        //FIND NEW MODIFICATIONS       
        LOGGER.info("FINDING PRIDE-ASAP MODIFICATIONS");
        //annotate spectra
        HashMap<String, Boolean> asapMods = new HashMap<>();
        ModificationService modificationService = spectrumAnnotator.getModificationService();
        Map<Modification, Integer> lPrideAsapModificationsMap = modificationService.getUsedModifications(spectrumAnnotator.getSpectrumAnnotatorResult());
        modificationRates = modificationService.estimateModificationRate(lPrideAsapModificationsMap, spectrumAnnotator.getSpectrumAnnotatorResult(), fixedThreshold);

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
                //0.4 is an arbitrary value TODO verify this !!!
                if (isQuantMod && modificationRate < (0.3)) {
                    LOGGER.error(amodName + " is a quant mod, but was not fixed !");
                } else {
                    LOGGER.info(amodName + "\t" + modificationRate);
                    asapMods.put(amodName, (modificationRate >= fixedThreshold));
                }
            }
        }
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

    private void inferEnzyme() throws IOException, FileNotFoundException, ClassNotFoundException, XmlPullParserException {
        completePeptides = spectrumAnnotator.getIdentifications().getCompletePeptides();
        EnzymePredictor predictor = new EnzymePredictor();
        try {
            predictor.addPeptideObjects(completePeptides);
            mainEnzyme = predictor.estimateBestEnzyme();
            missedCleavages = predictor.getMissCleavages();
            missedCleavageRatio = predictor.getMissedCleavageRatio();
        } finally {
            predictor.clear();
        }

    }

    private double calculateConsiderationThreshold() {
        PrideAsapStats stats = new PrideAsapStats(modificationRates.values(), false);
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

    public Enzyme getMainEnzyme() {
        return mainEnzyme;
    }

    public int getMostLikelyMissedCleavages() {
        return missedCleavages;
    }

    public int getMaxCharge() {
        return encounteredCharges.last();
    }

    public int getMinCharge() {
        return encounteredCharges.first();
    }

    public double getPrecursorAccuraccy() {
        return PrideAsapStats.round(mostLikelyPrecursorError, 3);
    }

    public double getFragmentIonAccuraccy() {
        return PrideAsapStats.round(mostLikelyFragIonAcc, 3);
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

    /**
     * Clears the resources
     */
    public void clear() {
        if (spectrumAnnotator != null) {
            spectrumAnnotator.clearPipeline();
            spectrumAnnotator.clearTmpResources();
        }
        fragmentIonStats.clear();
        precursorStats.clear();
        if (modificationRates != null) {
            modificationRates.clear();
        }
        if (completePeptides != null) {
            completePeptides.clear();
        }
        encounteredCharges.clear();
    }

}
