package com.compomics.pride_asa_pipeline.modification.impl;

import com.compomics.pride_asa_pipeline.model.AminoAcid;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.modification.ModificationParser;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;
import org.jdom.filter.Filter;
import org.jdom.input.SAXBuilder;

/**
 * Simple DOM parser to read modification definitions from a provided input
 * file.
 *
 * @author Jonathan Rameseder
 * @author Florian Reisinger Date: 08-Sep-2009
 * @since 0.1
 */
public class ModificationParserImpl implements ModificationParser {

    private static final Logger LOGGER = Logger.getLogger(ModificationParserImpl.class);
    private Document document = null;
    private boolean useMonoIsotopicMasses = true;

    /**
     * Boolean flag that specifies whether or not the monoisotopic or average
     * mass shift is used. Note: default value for this flag is 'true'.
     *
     * @return the boolean defining the mass shift usage.
     */
    public boolean isUseMonoIsotopicMasses() {
        return useMonoIsotopicMasses;
    }

    /**
     * Boolean flag to specifiy whether or not to use the monoisotopic or
     * average mass shift. Note: default value for this flag is 'true'.
     *
     * @param useMonoIsotopicMasses the boolean to set the mass shift usage.
     */
    public void setUseMonoIsotopicMasses(boolean useMonoIsotopicMasses) {
        this.useMonoIsotopicMasses = useMonoIsotopicMasses;
    }

    @Override
    public Set<Modification> parse(File modificationFile) {
        SAXBuilder builder = new SAXBuilder();

        try {
            document = builder.build(modificationFile);
        } catch (JDOMException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        Set<Modification> result = new HashSet<Modification>();

        if (document != null) {
            Element eRoot = document.getRootElement();


            Filter modFilter = new ElementFilter("modification");
            Iterator<Element> modIter = eRoot.getDescendants(modFilter);
            while (modIter.hasNext()) {
                Element eModification = modIter.next();
                Modification.Location position = readPosition(eModification);
                List<Element> eAffectedAAs = eModification.getChild("affectedAminoAcids").getChildren("affectedAminoAcid");
                Set<AminoAcid> affectedAminoAcids = new HashSet<AminoAcid>();
                for (Element eAffectedAA : eAffectedAAs) {
                    String letter = eAffectedAA.getValue();
                    if (letter.equals("*")) { // * is wildcard for all amino acids
                        affectedAminoAcids.addAll(Arrays.asList(AminoAcid.values()));
                    } else {
                        affectedAminoAcids.add(AminoAcid.getAA(letter));
                    }
                }
                String massElement;
                if (useMonoIsotopicMasses) {
                    massElement = "monoIsotopicMassShift";
                } else {
                    massElement = "averageMassShift";
                }

                //get all the values from the XML elements
                String name = eModification.getChild("name").getValue();
                double mass = Double.parseDouble(eModification.getChild(massElement).getValue());
                Element modAccEle = eModification.getChild("psimodAccession");
                String modAccession = null;
                if (modAccEle != null) {
                    modAccession = modAccEle.getValue();
                }
                Element modNameEle = eModification.getChild("psimodName");
                String modName = null;
                if (modNameEle != null) {
                    modName = modNameEle.getValue();
                }

                result.add(new Modification(name, mass, position, affectedAminoAcids, modAccession, modName));
            }



        }

        return result;
    }

    private Modification.Location readPosition(Element eModification) {
        Modification.Location position;
        String txtPosition = eModification.getChild("position").getValue();
        if (txtPosition.equals("any")) {
            position = Modification.Location.NON_TERMINAL;
        } else if (txtPosition.equals("N-terminus")) {
            position = Modification.Location.N_TERMINAL;
        } else if (txtPosition.equals("C-terminus")) {
            position = Modification.Location.C_TERMINAL;
        } else {
            throw new IllegalArgumentException("Modification position '"
                    + txtPosition + "' is not recognised.");
        }
        return position;
    }
}