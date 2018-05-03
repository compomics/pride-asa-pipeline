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
package com.compomics.pride_asa_pipeline.core.playground;

import com.compomics.pride_asa_pipeline.core.cache.ParserCache;
import com.compomics.pride_asa_pipeline.core.data.user.UserSuggestedModifications;
import com.compomics.pride_asa_pipeline.core.logic.inference.ParameterExtractor;
import com.compomics.pride_asa_pipeline.core.model.MGFExtractionException;
import com.compomics.pride_asa_pipeline.core.repository.impl.combo.WebServiceFileExperimentRepository;
import com.compomics.pride_asa_pipeline.core.repository.impl.file.FileExperimentRepository;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import java.io.File;
import java.io.IOException;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.geneontology.oboedit.dataadapter.GOBOParseException;
import org.xmlpull.v1.XmlPullParserException;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;

/**
 *
 * @author Kenneth Verheggen
 */
public class LocalProjectExtractor {

    /**
     * The output folder for the extraction
     */
    private final File outputFolder;
    /**
     * The Logger instance
     */
    private static final Logger LOGGER = Logger.getLogger(LocalProjectExtractor.class);

    public static void main(String[] args) throws IOException, ParseException, MGFExtractionException, MzXMLParsingException, JMzReaderException, XmlPullParserException, ClassNotFoundException, GOBOParseException, InterruptedException, Exception {
        File outputFolder = new File("C:\\Users\\Kenneth\\Desktop\\MzID_Test\\download");
        File inputFile = new File("C:\\Users\\davy\\Desktop\\Thesis\\PeptideShaker_Example_Dataset.xml");
        UserSuggestedModifications.getInstance().addModification("Phospho");
        SearchParameters analyze = new LocalProjectExtractor(outputFolder).analyze(inputFile);
        System.out.println(analyze);
    }

    public LocalProjectExtractor(File outputFolder) {
        this.outputFolder = outputFolder;
    }

    public SearchParameters analyze(File inputFile) throws IOException, MGFExtractionException, MzXMLParsingException, JMzReaderException, XmlPullParserException, ClassNotFoundException, GOBOParseException, Exception {
        LOGGER.info("Setting up experiment repository for assay " + inputFile.getName());
        FileExperimentRepository experimentRepository = new FileExperimentRepository();
        experimentRepository.addPrideXMLFile(inputFile.getName(), inputFile);
        LOGGER.info("Attempting to infer searchparameters");
        ParameterExtractor extractor = new ParameterExtractor(inputFile.getName());
        return extractor.getParameters();
    }
}
