package com.compomics.pride_asa_pipeline.modification.impl;

import com.compomics.pride_asa_pipeline.model.AminoAcid;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.modification.ModificationMarshaller;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;
import org.jdom2.filter.Filter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 * Simple DOM parser to read modification definitions from a provided input
 * file.
 *
 * @author Jonathan Rameseder
 * @author Florian Reisinger Date: 08-Sep-2009
 * @since 0.1
 */
public class ModificationMarshallerImpl implements ModificationMarshaller {

    private static final Logger LOGGER = Logger.getLogger(ModificationMarshallerImpl.class);
    private static final String N_TERMINAL_LOCATION_STRING = "N-terminus";
    private static final String NON_TERMINAL_LOCATION_STRING = "any";
    private static final String C_TERMINAL_LOCATION_STRING = "C-terminus";
    private Document document = null;

    @Override
    public Set<Modification> unmarshall(File modificationFile) {
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
                Modification.Location location = readLocation(eModification);
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

                //get all the values from the XML elements
                String name = eModification.getChild("name").getValue();
                double monoIsotopicMassShift = Double.parseDouble(eModification.getChild("monoIsotopicMassShift").getValue());
                double averageMassShift = Double.parseDouble(eModification.getChild("averageMassShift").getValue());
                Element modAccEle = eModification.getChild("accession");
                String modAccession = null;
                if (modAccEle != null) {
                    modAccession = modAccEle.getValue();
                }
                Element modAccessionValueEle = eModification.getChild("accessionValue");
                String modAccessionValue = null;
                if (modAccessionValueEle != null) {
                    modAccessionValue = modAccessionValueEle.getValue();
                }

                result.add(new Modification(name, monoIsotopicMassShift, averageMassShift, location, affectedAminoAcids, modAccession, modAccessionValue));
            }
        }

        return result;
    }

    @Override
    public void marshall(String modificationFilePath, Collection<Modification> modifications) {
        File modificationsFile = new File(modificationFilePath);        

        try {
            //add root element
            Document doc = new Document();
            Element rootElement = new Element("modifications");
            for (Modification modification : modifications) {
                Element modificationElement = new Element("modification");

                Element nameElement = new Element("name");
                nameElement.setText(modification.getAccessionValue());
                modificationElement.addContent(nameElement);

                Element monoIsotopicMassShiftElement = new Element("monoIsotopicMassShift");
                monoIsotopicMassShiftElement.setText(Double.toString(modification.getMonoIsotopicMassShift()));
                modificationElement.addContent(monoIsotopicMassShiftElement);

                Element averageMassShiftElement = new Element("averageMassShift");
                averageMassShiftElement.setText(Double.toString(modification.getAverageMassShift()));
                modificationElement.addContent(averageMassShiftElement);

                Element locationElement = new Element("location");
                locationElement.setText(getPositionAsString(modification.getLocation()));
                modificationElement.addContent(locationElement);

                Element affectedAminoAcidsElement = new Element("affectedAminoAcids");

                if (modification.getAffectedAminoAcids().size() == AminoAcid.values().length) {
                    Element affectedAminoAcidElement = new Element("affectedAminoAcid");
                    affectedAminoAcidElement.setText("*");
                    affectedAminoAcidsElement.addContent(affectedAminoAcidElement);
                } else {
                    for (AminoAcid aminoAcid : modification.getAffectedAminoAcids()) {
                        Element affectedAminoAcidElement = new Element("affectedAminoAcid");
                        affectedAminoAcidElement.setText(String.valueOf(aminoAcid.letter()));
                        affectedAminoAcidsElement.addContent(affectedAminoAcidElement);
                    }
                }
                modificationElement.addContent(affectedAminoAcidsElement);

                Element accessionElement = new Element("accession");
                accessionElement.setText(modification.getAccession());
                modificationElement.addContent(accessionElement);

                Element accessionValueElement = new Element("accessionValue");
                accessionValueElement.setText(modification.getAccessionValue());
                modificationElement.addContent(accessionValueElement);

                //add to root element
                rootElement.addContent(modificationElement);
            }

            doc.addContent(rootElement);

            XMLOutputter xmlOutputter = new XMLOutputter();
            xmlOutputter.setFormat(Format.getPrettyFormat());
            FileOutputStream fileOutputStream = new FileOutputStream(modificationsFile);
            xmlOutputter.output(doc, fileOutputStream);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private Modification.Location readLocation(Element eModification) {
        Modification.Location position;
        String txtPosition = eModification.getChild("location").getValue();
        if (txtPosition.equals(NON_TERMINAL_LOCATION_STRING)) {
            position = Modification.Location.NON_TERMINAL;
        } else if (txtPosition.equals(N_TERMINAL_LOCATION_STRING)) {
            position = Modification.Location.N_TERMINAL;
        } else if (txtPosition.equals(C_TERMINAL_LOCATION_STRING)) {
            position = Modification.Location.C_TERMINAL;
        } else {
            throw new IllegalArgumentException("Modification position '"
                    + txtPosition + "' is not recognised.");
        }
        return position;
    }

    private String getPositionAsString(Modification.Location location) {
        String locationString = "";
        if (location.equals(Modification.Location.N_TERMINAL)) {
            locationString = N_TERMINAL_LOCATION_STRING;
        } else if (location.equals(Modification.Location.NON_TERMINAL)) {
            locationString = NON_TERMINAL_LOCATION_STRING;
        } else {
            locationString = C_TERMINAL_LOCATION_STRING;
        }

        return locationString;
    }
}
