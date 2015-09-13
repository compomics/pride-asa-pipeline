package com.compomics.pride_asa_pipeline.core.logic.parameters;

import com.compomics.pride_asa_pipeline.core.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.core.logic.FileSpectrumAnnotator;
import com.compomics.pride_asa_pipeline.core.logic.enzyme.EnzymePredictor;
import com.compomics.pride_asa_pipeline.core.logic.modification.PTMMapper;
import com.compomics.pride_asa_pipeline.core.repository.FileParser;
import com.compomics.pride_asa_pipeline.core.repository.factory.FileParserFactory;
import com.compomics.pride_asa_pipeline.core.service.FileModificationService;
import com.compomics.pride_asa_pipeline.core.spring.ApplicationContextProvider;
import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.FragmentIonAnnotation;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
    // the enzyme that was highlighted as most likely 
    private Enzyme mainEnzyme;
    EnzymePredictor predictor;
    // the amount of estimated missed cleavages
    private int missedCleavages;
    // charge states (TODO infer this as well?)
    private final int maxCharge = 5;
    private final int minCharge = 1;
    // the likely mass errors
    private double mostLikelyPrecursorError;
    private double mostLikelyFragIonAcc;

    private final boolean useAbsoluteMassDelta = true;

    private PrideAsapStats fragmentIonStats = new PrideAsapStats(useAbsoluteMassDelta);
    private PrideAsapStats precursorStats = new PrideAsapStats(useAbsoluteMassDelta);

    private double considerationThreshold;
    private final double fixedThreshold = 0.8;

    private final double modificationConsiderationConfidence = 2.5;
    private PTMMapper prideToPtmMapper;
    private Map<Modification, Double> modificationRates = new HashMap<>();
    private HashMap<String, Boolean> asapMods = new HashMap<>();
    protected PtmSettings ptmSettings;
    private ArrayList<String> unknownMods = new ArrayList<>();
    double missedCleavageRatio;
    private List<Peptide> completePeptides;

    private List<String> quantTerms = Arrays.asList(new String[]{"itraq", "tmt", "silac"});

    public PrideAsapInterpreter(File identificationsFile, File peakFile) throws IOException, ClassNotFoundException, MzXMLParsingException, JMzReaderException {
        init(identificationsFile, peakFile);
    }

    public PtmSettings getPtmSettings() {
        return ptmSettings;
    }

    public void setModProfile(PtmSettings modProfile) {
        this.ptmSettings = modProfile;
    }

    private void init(File identificationsFile, File peakFile) throws IOException, ClassNotFoundException, MzXMLParsingException, JMzReaderException {
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
                ex.printStackTrace();
            }
        } catch (XmlPullParserException | GOBOParseException ex) {
            LOGGER.error("Could not infer modifications...:" + ex);
            ex.printStackTrace();
        }

    }

    /**
     *
     * @return the analyzerdata for the input file.
     */
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
        precursorStats = fileSpectrumAnnotator.getMassDeltaExplainer().getExplainedMassDeltas();
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
        prideToPtmMapper = PTMMapper.getInstance();
        //annotate spectra
        FileModificationService modificationService = (FileModificationService) fileSpectrumAnnotator.getModificationService();
        Map<Modification, Integer> lPrideAsapModificationsMap = modificationService.getUsedModifications(fileSpectrumAnnotator.getSpectrumAnnotatorResult());
        modificationRates = modificationService.estimateModificationRate(lPrideAsapModificationsMap, fileSpectrumAnnotator.getSpectrumAnnotatorResult(), fixedThreshold);

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
        unknownMods = new ArrayList<>();
        ptmSettings = prideToPtmMapper.buildUniqueMassModProfile(asapMods, unknownMods, mostLikelyPrecursorError);
        prideToPtmMapper.clear();
    }

    private void inferEnzyme() throws IOException, FileNotFoundException, ClassNotFoundException, XmlPullParserException {
        completePeptides = fileSpectrumAnnotator.getExperimentService().loadExperimentIdentifications().getCompletePeptides();
        List<String> completePeptideSequences = new ArrayList<>();
        predictor = new EnzymePredictor();
        for (Peptide aPeptide : completePeptides) {
            completePeptideSequences.add(aPeptide.getSequenceString());
        }
        predictor.addPeptideObjects(completePeptides);
        mainEnzyme = predictor.estimateBestEnzyme();
        missedCleavages = predictor.getMissCleavages();
        missedCleavageRatio = predictor.getMissedCleavageRatio();
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

    public File getInputFile() {
        return identificationsFile;
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

    public void clear() {
        //cleanup the pipeline
        //   PTMFactory.getInstance().clearFactory();
        if (fileSpectrumAnnotator != null) {
            fileSpectrumAnnotator.clearPipeline();
            fileSpectrumAnnotator.clearTmpResources();
        }
        fragmentIonStats.clear();
        precursorStats.clear();
        if (modificationRates != null) {
            modificationRates.clear();
        }
        if (parser != null) {
            parser.clear();
        }
        asapMods.clear();
        unknownMods.clear();
        if (predictor != null) {
            predictor.clear();
        }
        if (completePeptides != null) {
            completePeptides.clear();
        }
        if (prideToPtmMapper != null) {
            prideToPtmMapper.clear();
        }
    }

}
