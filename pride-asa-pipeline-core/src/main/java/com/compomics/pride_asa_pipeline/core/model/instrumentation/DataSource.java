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
package com.compomics.pride_asa_pipeline.core.model.instrumentation;

import com.compomics.pride_asa_pipeline.core.cache.ParserCache;
import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileExperimentRepository;
import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import java.io.File;
import java.util.TreeSet;
import com.compomics.pride_asa_pipeline.core.gui.PipelineProgressMonitor;

import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.CachedDataAccessController;
import uk.ac.ebi.pride.utilities.data.core.CvParam;
import uk.ac.ebi.pride.utilities.data.core.ParamGroup;
import uk.ac.ebi.pride.utilities.data.core.Precursor;
import uk.ac.ebi.pride.utilities.data.core.Spectrum;

/**
 *
 * @author Kenneth Verheggen
 */
public class DataSource {

    //parsing from file
    private final FileExperimentRepository repository = new FileExperimentRepository();
    private final CachedDataAccessController parser;
    private String source;
    private String detector;
    private String massSpectrometer;
    private String sourceName;
    //read from the file
    private final String experiment;
    private final MassSpecInstrumentation instrumentation = new MassSpecInstrumentation();

    public DataSource(File prideXML) {
        experiment = prideXML.getName();
        repository.addPrideXMLFile(experiment, prideXML);
        parser = ParserCache.getInstance().getParser(experiment, true);
    }

    public void init() {
        preloadInstrument();
    }


    private void preloadInstrument() {

        source = parser.getMzGraphMetaData().getInstrumentConfigurations().get(0).getSource().get(0).getCvParams().get(0).getAccession();
        sourceName = parser.getMzGraphMetaData().getInstrumentConfigurations().get(0).getSource().get(0).getCvParams().get(0).getName();
        detector = parser.getMzGraphMetaData().getInstrumentConfigurations().get(0).getDetector().get(0).getCvParams().get(0).getName();
        massSpectrometer = parser.getMzGraphMetaData().getInstrumentConfigurations().get(0).getId();
        AnalyzerData analyzerDataByAnalyzerType = AnalyzerData.getAnalyzerDataByAnalyzerType(massSpectrometer);
        //check the initial accuraccy as being the full mass error?
        instrumentation.setData(AnalyzerData.getDataDerivedAnalyzerDataByAnalyzerType(
                analyzerDataByAnalyzerType.getPrecursorMassError(),
                analyzerDataByAnalyzerType.getFragmentMassError(),
                massSpectrometer));

        inferCharges(source);
        PipelineProgressMonitor.info("Detected instrument : " + massSpectrometer
                + "-" + sourceName
                + "-" + detector
                + " [" + instrumentation.getPossibleCharges().first().toString() + " to " + instrumentation.getPossibleCharges().last().toString() + "]");
    }


    public String getExperiment() {
        return experiment;
    }


    public MassSpecInstrumentation getInstrumentation() {
        return instrumentation;
    }

    public FileExperimentRepository getRepository() {
        return repository;
    }

    public CachedDataAccessController getParser() {
        return parser;
    }

    private void inferCharges(String source) {
        int[] detectedCharges = findChargeRange();
        //IF MALDI ---> only single charge or even charges possible...
        if (source.contains(ControlledVocabulary.MALDI.getTerm())) {
            //check the possible charges and reduce them to 1 in case they were searched with...
            if (detectedCharges[1] > 1) {
                //we may have an issue here...maldi does not usually produce higher ions so this is a non standard study
                PipelineProgressMonitor.warn("The detected ion source was MALDI and usually does not produce ions above charge state 1+, ignoring higher states");
            }
            instrumentation.getPossibleCharges().add(1);
        } else if (source.contains(ControlledVocabulary.ESI.getTerm())) {

            if (detectedCharges[0] == 1) {
                //we may have an issue here...esi does not usually produce lower ions so this is a non standard study
                PipelineProgressMonitor.warn("The detected ion source was ESI and usually does not produce singly charged ions, ignoring single charge state");
            }
            for (int i = Math.max(2, detectedCharges[0]); i <= detectedCharges[1] + 1; i++) {
                instrumentation.getPossibleCharges().add(i);
            }
        } else {
            for (int i = detectedCharges[0]; i <= detectedCharges[1] + 1; i++) {
                instrumentation.getPossibleCharges().add(i);
            }
        }
    }

    private int[] findChargeRange() {
        TreeSet<Integer> charges = new TreeSet<>();
        for (Comparable spectrumID : parser.getSpectrumIds()) {
            if (charges.size() >= 1000) {
                PipelineProgressMonitor.info("Sampled 1000 spectra");
                break;
            }
            Spectrum spectrumById = parser.getSpectrumById(spectrumID);
            for (Precursor precursor : spectrumById.getPrecursors()) {
                for (ParamGroup group : precursor.getSelectedIons()) {
                    for (CvParam param : group.getCvParams()) {
                        if (ControlledVocabulary.CHARGE_STATE.getTerm().equalsIgnoreCase(param.getAccession())) {
                            charges.add(Integer.parseInt(param.getValue()));
                        }
                    }
                }

            }
        }
        if (charges.isEmpty()) {
            return new int[]{1, 3};
        } else {
            return new int[]{charges.first(), charges.last()};
        }

    }

}
