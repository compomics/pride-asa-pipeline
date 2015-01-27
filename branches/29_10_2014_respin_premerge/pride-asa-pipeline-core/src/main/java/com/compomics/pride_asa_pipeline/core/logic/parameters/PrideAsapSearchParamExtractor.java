/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.logic.parameters;

import com.compomics.util.experiment.identification.SearchParameters;
import com.compomics.util.experiment.massspectrometry.Charge;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.geneontology.oboedit.dataadapter.GOBOParseException;
import org.xmlpull.v1.XmlPullParserException;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;

/**
 *
 * @author Kenneth
 */
public class PrideAsapSearchParamExtractor extends PrideAsapInterpreter {

    private File searchparametersfile;
    private static final Logger LOGGER = Logger.getLogger(PrideAsapSearchParamExtractor.class);
    private SearchParameters parameters;
    private File fastaFile;

    public PrideAsapSearchParamExtractor(File identificationsFile,File peakFile) throws Exception, IOException, ClassNotFoundException, MzXMLParsingException, JMzReaderException, XmlPullParserException, Exception {
        super(identificationsFile,peakFile);
        String ext = FilenameUtils.getExtension(identificationsFile.getAbsolutePath());
        String fileName = identificationsFile.getName().substring(0,identificationsFile.getName().lastIndexOf(ext));
        searchparametersfile = new File(identificationsFile.getParentFile(), fileName+"parameters");
        LOGGER.info("Spectrumannotator delivered was initialized");
    }

    public SearchParameters getParameters(){
        return parameters;
    }
    
    private void initialize() throws XmlPullParserException, IOException, GOBOParseException {
        //INITIATE THE required FACTORIES
        parameters = new SearchParameters();
        LOGGER.debug("Initializing searchparameter extraction");
        LOGGER.debug("Setting up parametersfile");
        if (searchparametersfile.exists()) {
            LOGGER.info("SearchGUI.parameters already exists, refreshing !");
            try {
                SearchParameters.saveIdentificationParameters(parameters, searchparametersfile);
            } catch (IOException | ClassNotFoundException ex) {
                LOGGER.error(ex);
            }
        }
    }

    private void setModifications() {
        LOGGER.info("Generating new modification-profile");
        modProfile = getModProfile();
        modProfile.removeFixedModification("unknown");
        modProfile.removeVariableModification("unknown");
        parameters.setModificationProfile(modProfile);
    }

    private void setMachineParameters() {
        LOGGER.info("Retrieving machine parameters ");
        parameters.setPrecursorAccuracyType(SearchParameters.PrecursorAccuracyType.DA);
        parameters.setPrecursorAccuracy(getPrecursorAccuraccy());
        parameters.setFragmentIonAccuracy(getFragmentIonAccuraccy());
    }

    private void setConsideredCharges() {
        LOGGER.info("Setting considered charges");
        parameters.setMaxChargeSearched(new Charge(1, getMaxCharge()));
        parameters.setMinChargeSearched(new Charge(1, getMinCharge()));
    }

    private void setEnzyme() {
        LOGGER.info("Setting enzyme...");
        parameters.setEnzyme(getMainEnzyme());
        parameters.setnMissedCleavages(getMostLikelyMissedCleavages());
    }

    private void setFasta() {
        LOGGER.info("Setting fasta file...");
        parameters.setFastaFile(fastaFile);
    }

    public void setFastaFile(File fastaFile) {
        this.fastaFile = fastaFile;
    }

    public SearchParameters getSearchParametersFileForProject() {
        try {
            initialize();
        } catch (XmlPullParserException | IOException | GOBOParseException ex) {
            LOGGER.error(ex);
        }
        try {
            setMachineParameters();
        } catch (Exception ex) {
            LOGGER.error(ex);
        }
        //set enzyme AND misscleavage
        setEnzyme();
        setConsideredCharges();
        setFasta();
        try {
            setModifications();
        } catch (Exception ex) {
            LOGGER.error(ex);
            LOGGER.error("Could not find modifications, searching modless");
        }
        LOGGER.info("Finished extraction...enjoy the search");
        return parameters;
    }

    public void save(File outputFile, boolean override) throws IOException {
        LOGGER.info("Saving parameters to " + outputFile);
        //CREATE AN EMPTY FILE
        if (outputFile.exists()) {
            if (override) {
                outputFile.delete();
            } else {
                throw new IOException("File already exists !");
            }
        }
        outputFile.getParentFile().mkdirs();
        outputFile.createNewFile();
        searchparametersfile = outputFile;
        try {
            SearchParameters.saveIdentificationParameters(parameters, searchparametersfile);
        } catch (IOException | ClassNotFoundException ex) {
            LOGGER.error(ex);
        }
    }
}
