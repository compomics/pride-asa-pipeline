package com.compomics.pride_asa_pipeline.core.logic.parameters;

import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.massspectrometry.Charge;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.geneontology.oboedit.dataadapter.GOBOParseException;
import org.xmlpull.v1.XmlPullParserException;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;

/**
 *
 * @author Kenneth Verheggen
 */
public class PrideAsapExtractor extends PrideAsapInterpreter {

    /**
     * The output parameters file
     */
    private File searchparametersfile;

    /**
     * A logger
     */
    private static final Logger LOGGER = Logger.getLogger(PrideAsapExtractor.class);
    /**
     * The searchparameter object
     */
    private SearchParameters parameters;
    /**
     * The outputfolder for the parameter file and the MGF file
     */
    private final File outputFolder;

    public PrideAsapExtractor(String assay, File outputFolder) throws IOException, ClassNotFoundException, MzXMLParsingException, JMzReaderException, XmlPullParserException {
        super(assay);
        this.outputFolder = outputFolder;
        searchparametersfile = new File(outputFolder, assay + ".asap.par");
    }

    public SearchParameters getParameters() {
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
            } catch (FileNotFoundException ex) {
                LOGGER.error(ex);
            }
        }
    }

    private void setModifications() {
        LOGGER.info("Generating new modification-profile");
        ptmSettings = getPtmSettings();
        ptmSettings.removeFixedModification("unknown");
        ptmSettings.removeVariableModification("unknown");
        parameters.setPtmSettings(ptmSettings);
    }

    private void setMachineParameters() {
        LOGGER.info("Retrieving machine parameters ");
        parameters.setPrecursorAccuracyType(SearchParameters.MassAccuracyType.DA);
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

    private void setFasta(File fastaFile) {
        LOGGER.info("Setting fasta file...");
        parameters.setFastaFile(fastaFile);
    }

    /*
     * Returns the fully inferred parameters for a given assay
     */
    public SearchParameters inferSearchParameters() throws XmlPullParserException, IOException, GOBOParseException {
        initialize();
        try {
            System.out.print("Determining mass error settings...");
            setMachineParameters();
            System.out.println("--Done");
        } catch (Exception ex) {
            System.out.println("--Failed");
            LOGGER.error(ex);
        }
        //set enzyme AND misscleavage
        System.out.print("Determining cleaving settings...");
        setEnzyme();
        System.out.println("--Done");
        System.out.print("Determining charge settings...");
        setConsideredCharges();
        System.out.println("--Done");
        try {
            System.out.print("Determining Modification profile settings...");
            setModifications();
            System.out.println("--Done");
        } catch (Exception ex) {
            System.out.println("--Failed");
            LOGGER.error(ex);
            LOGGER.error("Could not find modifications, searching modless");
        }
        LOGGER.info("Finished extraction...enjoy the search");
        return parameters;
    }

    /**
     * Saves the searchparameters and the related spectra to the specified
     * outputfolder
     *
     * @param outputFolder the output folder
     * @param savemgf boolean indicating if the mgf file should be saved
     * @param override boolean indicating if the parameters should be overridden
     * @throws IOException
     */
    public void save(File outputFolder,boolean override) throws IOException, FileNotFoundException, ClassNotFoundException {
        File parameterFile = new File(outputFolder, searchparametersfile.getName());
        outputFolder.mkdirs();
        if (searchparametersfile != parameterFile) {
            LOGGER.info("Saving parameters to " + parameterFile);
            //CREATE AN EMPTY FILE
            if (parameterFile.exists()) {
                if (override) {
                    parameterFile.delete();
                } else {
                    throw new IOException("File already exists !");
                }
            }
            SearchParameters.saveIdentificationParameters(parameters, searchparametersfile);
        }
    }
}
