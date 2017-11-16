package com.compomics.pride_asa_pipeline.core.bypass;

import com.compomics.pride_asa_pipeline.core.logic.inference.enzyme.EnzymePredictor;
import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.ParameterExtractionException;
import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.preferences.DigestionPreferences;
import com.compomics.util.preferences.GenePreferences;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.pride.PrideWebService;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.xmlpull.v1.XmlPullParserException;
import uk.ac.ebi.pride.archive.web.service.model.assay.AssayDetail;
import uk.ac.ebi.pride.archive.web.service.model.peptide.PsmDetail;
import uk.ac.ebi.pride.archive.web.service.model.peptide.PsmDetailList;
import uk.ac.ebi.pride.archive.web.service.model.project.ProjectDetail;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class WebServiceParameterInference {

    private static Logger LOGGER = Logger.getLogger(WebServiceParameterInference.class);

    private int minCharge = 2;
    private int maxCharge = 4;

    private boolean skipEnzyme;

    private String assayIdentifier = "11954";

    public WebServiceParameterInference(String assayIdentifier) {
        this.assayIdentifier = assayIdentifier;
        skipEnzyme = false;
    }

    public WebServiceParameterInference(String assayIdentifier, boolean skipEnzyme) {
        this.assayIdentifier = assayIdentifier;
        this.skipEnzyme = skipEnzyme;
    }

    public IdentificationParameters InferParameters() throws IOException, ParameterExtractionException {
        //init variables
        IdentificationParameters idParam = new IdentificationParameters();
        SearchParameters param = new SearchParameters();

        DigestionPreferences digestionPreferences = new DigestionPreferences();
        digestionPreferences.setCleavagePreference(DigestionPreferences.CleavagePreference.enzyme);

        PtmSettings ptmSettings = new PtmSettings();

        GenePreferences genePreferences = new GenePreferences();
        idParam.setGenePreferences(genePreferences);

        AssayDetail detail = null;

        detail = PrideWebService.getAssayDetail(assayIdentifier);

        if (detail == null) {
            throw new ParameterExtractionException("Assay does not exist in the repository.");
        }

        //ANALYZER_____________________________________________________
        try {
            LOGGER.info("Acquiring analyzer data...");
            AnalyzerData analyzerData = getAnalyzerData(detail);
            LOGGER.info("Instrument : " + analyzerData.getAnalyzerFamily());
            param.setPrecursorAccuracyDalton(analyzerData.getPrecursorMassError());
            param.setFragmentIonAccuracy(analyzerData.getFragmentMassError());
        } catch (Exception e) {
            LOGGER.error("Could not infer the analyzer data : " + e);
            //default mass tolerances
            param.setPrecursorAccuracyDalton(0.6);
            param.setFragmentIonAccuracy(0.1);
        }

        ArrayList<String> unknownPtms = new ArrayList<>();
        try {
            //PTMS_____________________________________________________
            LOGGER.info("Acquiring annotated PTM");
            for (String ptm : GetModifications(detail)) {
                PTMFactory.getInstance().convertPridePtm(ptm, ptmSettings, unknownPtms, false);
            }
        } catch (Exception e) {
            LOGGER.error("Could not infer modifications :" + e);
            PTMFactory.getInstance().convertPridePtm("Carbamidomethylation of C", ptmSettings, unknownPtms, false);
            PTMFactory.getInstance().convertPridePtm("Oxidation of M", ptmSettings, unknownPtms, false);
        }
        if (!skipEnzyme) {
            try {
                //ENZYMES_____________________________________________________
                LOGGER.info("Acquiring enzyme information");
                //get enzyme
                EnzymePredictor predictor = GetEnzymePredictor(detail);
                ArrayList<Enzyme> enzymes = new ArrayList<>();
                Enzyme enzyme = predictor.getMostLikelyEnzyme();
                enzymes.add(enzyme);

                digestionPreferences.setEnzymes(enzymes);
                digestionPreferences.setnMissedCleavages(enzymes.get(0).getName(), predictor.getMissedCleavages());

            } catch (Exception e) {
                LOGGER.error("Could not infer enzymes : " + e);
                String defaultEnzyme = "Trypsin";
                digestionPreferences.addEnzyme(EnzymeFactory.getDefault().getEnzyme(defaultEnzyme));
                digestionPreferences.setnMissedCleavages(defaultEnzyme, 2);
            } finally {
                for (Enzyme enzyme : digestionPreferences.getEnzymes()) {
                    digestionPreferences.setSpecificity(enzyme.getName(), DigestionPreferences.Specificity.specific);
                }
                //these are bugs introduced with new utilities
                /*            digestionPreferences.getXTandemFormat();
                        //debug mods
            PTMFactory.getInstance().loadBackedUpModifications(param, true);*/
            }
        }

        //if min and max charge are the same, we want to expand the range?
        if (minCharge == maxCharge) {
            minCharge = Math.max(1, minCharge - 1);
            maxCharge = Math.min(5, maxCharge + 1);
        }
        //set these in the parameters
        param.setMinChargeSearched(new Charge((int) Math.signum(minCharge), Math.abs(minCharge)));
        param.setMaxChargeSearched(new Charge((int) Math.signum(maxCharge), Math.abs(maxCharge)));

        //compile and print
        param.setDigestionPreferences(digestionPreferences);

        param.setPtmSettings(ptmSettings);

        idParam.setSearchParameters(param);

        System.out.println(idParam.getSearchParameters().toString());
        return idParam;
    }

    public IdentificationParameters InferParameters(File outputFile) throws IOException, ParameterExtractionException {
        IdentificationParameters idParam = InferParameters();
        if (outputFile != null) {
            try {
                IdentificationParameters.saveIdentificationParameters(idParam, outputFile);
            } catch (IOException ex) {
                LOGGER.error("Could not save the parameters at " + outputFile.getAbsolutePath());
            }
        }
        return idParam;
    }

    private AnalyzerData getAnalyzerData(AssayDetail assayDetail) {
        Set<String> instrumentNames = assayDetail.getInstrumentNames();
        AnalyzerData analyzerData;
        String selectedInstrument = "";
        if (instrumentNames.size() > 1) {
            LOGGER.warn("There are multiple instruments, selecting lowest precursor accuraccy...");
            analyzerData = AnalyzerData.getAnalyzerDataByAnalyzerType(instrumentNames.iterator().next());
            for (String anInstrumentName : instrumentNames) {
                AnalyzerData temp = AnalyzerData.getAnalyzerDataByAnalyzerType(anInstrumentName);
                //worst precursor has benefit
                if (analyzerData.getPrecursorAccuraccy() > temp.getPrecursorAccuraccy()) {
                    analyzerData = temp;
                    selectedInstrument = anInstrumentName;
                }
            }
        } else if (instrumentNames.size() == 1) {
            LOGGER.warn("There are multiple instruments, selecting lowest precursor accuraccy...");
            selectedInstrument = instrumentNames.iterator().next();
            analyzerData = AnalyzerData.getAnalyzerDataByAnalyzerType(selectedInstrument);

        } else {
            LOGGER.warn("There are multiple instruments, selecting lowest precursor accuraccy...");
            selectedInstrument = "Unknown...";;
            analyzerData = AnalyzerData.getAnalyzerDataByAnalyzerType("");
        }
        LOGGER.info("The selected instrument was : " + selectedInstrument);
        return analyzerData;
    }

    private Set<String> GetModifications(AssayDetail detail) {
        Set<String> annotatedPTMs = new HashSet<>();
        annotatedPTMs.add("Carbamidomethylation of C");
        annotatedPTMs.add("Oxidation of M");
        try {
            ProjectDetail projectDetail = PrideWebService.getProjectDetail(detail.getProjectAccession());
            Set<String> ptmNames = projectDetail.getPtmNames();
            for (String aPtm : ptmNames) {
                annotatedPTMs.add(aPtm);
            }
        } catch (IOException ex) {
            LOGGER.error("Something went wrong caching the modifications...");
        }
        return annotatedPTMs;
    }

    private EnzymePredictor GetEnzymePredictor(AssayDetail detail) throws ParameterExtractionException {
        EnzymePredictor predictor = null;
        try {
            //  int psmCount = PrideWebService.getPSMCountByAssay(detail.getAssayAccession());
            PsmDetailList psmList = PrideWebService.getPSMsByAssay(detail.getAssayAccession());
            Set<String> sequences = new HashSet<>();
            for (PsmDetail aPsm : psmList.getList()) {
                sequences.add(aPsm.getSequence());
                minCharge = Math.min((aPsm.getCharge()), minCharge);
                maxCharge = Math.min((aPsm.getCharge()), maxCharge);
            }
            predictor = new EnzymePredictor(sequences);
        } catch (IOException | XmlPullParserException ex) {
            LOGGER.error(ex);
        }
        if (predictor == null) {
            throw new ParameterExtractionException("Could not generate a suited enzyme predictor");
        }
        return predictor;
    }

    public static void SyncWithAnnotation(IdentificationParameters inferredParameters, String assay) {
        try {
            //continue
            WebServiceParameterInference webServiceParameterInference = new WebServiceParameterInference(assay, true);
            IdentificationParameters webServiceParameters = webServiceParameterInference.InferParameters();
            //check if we are missing annotated modifications
            compareModifications(inferredParameters, webServiceParameters);
            //check if the mass accuraccy is not worse than what is annotated (the instrument ones)
            compareMassAccuracies(inferredParameters, webServiceParameters);
            //check the enzymes between both versions
            compareEnzyme(inferredParameters, webServiceParameters);
        } catch (IOException ex) {
            LOGGER.error("Could not contact the EBI PRIDE Webservice");
            LOGGER.error(ex);
        } catch (ParameterExtractionException ex) {
            LOGGER.error("Something went wrong during the extraction of parameters through the webservice !");
            LOGGER.error(ex);
        }

    }

    private static void compareModifications(IdentificationParameters inferredParameters, IdentificationParameters webServiceParameters) {
        LOGGER.info("Comparing modifications...");
        ArrayList<String> allInferredPTM = inferredParameters.getSearchParameters().getPtmSettings().getAllModifications();
        ArrayList<String> annotatedPTM = webServiceParameters.getSearchParameters().getPtmSettings().getAllModifications();
        for (String aPTM : annotatedPTM) {
            if (!allInferredPTM.contains(aPTM)) {
                PTM exclusiveMod = webServiceParameters.getSearchParameters().getPtmSettings().getPtm(aPTM);
                inferredParameters.getSearchParameters().getPtmSettings().addVariableModification(exclusiveMod);
                LOGGER.info(aPTM + " was annotated but not inferred...");
            }
        }
    }

    private static void compareMassAccuracies(IdentificationParameters inferredParameters, IdentificationParameters webServiceParameters) {
        LOGGER.info("Comparing mass accuraccies");
        double inferredPrecAcc = inferredParameters.getSearchParameters().getPrecursorAccuracyDalton();
        double annotatedPrecAcc = webServiceParameters.getSearchParameters().getPrecursorAccuracyDalton();
        double inferredFragAcc = inferredParameters.getSearchParameters().getFragmentIonAccuracy();
        double annotatedFragAcc = webServiceParameters.getSearchParameters().getFragmentIonAccuracy();

        if (annotatedPrecAcc < inferredPrecAcc || annotatedFragAcc < inferredFragAcc) {
            inferredParameters.getSearchParameters().setPrecursorAccuracyDalton(annotatedPrecAcc);
            inferredParameters.getSearchParameters().setFragmentIonAccuracy(annotatedFragAcc);
            LOGGER.info("The annotated machine has a higher accuraccy : ");
            LOGGER.info(inferredPrecAcc + "-->" + annotatedPrecAcc + " precursor acc (Da)");
            LOGGER.info(inferredFragAcc + "-->" + annotatedFragAcc + " fragment acc (Da)");
        }

    }

    private static void compareEnzyme(IdentificationParameters inferredParameters, IdentificationParameters webServiceParameters) {
        //This is pointless, we are inferring this through the sequences which are identical  between the file and the webservice
        //@TODO implement this at a later time
    }

}
