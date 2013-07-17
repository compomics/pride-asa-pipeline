package com.compomics.pride_asa_pipeline.logic.modification.impl;

import com.compomics.pride_asa_pipeline.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.logic.modification.ModificationMarshaller;
import com.compomics.pride_asa_pipeline.model.AminoAcid;
import com.compomics.pride_asa_pipeline.model.Modification;
import java.io.*;
import java.net.URL;
import java.util.*;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;
import org.jdom2.filter.Filter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderJDOMFactory;
import org.jdom2.input.sax.XMLReaderXSDFactory;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

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
    private Document document;
    private URL modificationsSchemaURL;

    public ModificationMarshallerImpl() {
        Resource resource = new ClassPathResource(PropertiesConfigurationHolder.getInstance().getString("modification.pipeline_modifications_schema_name"));
        try {
            modificationsSchemaURL = resource.getURL();
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    @Override
    public Set<Modification> unmarshall(Resource modificationsResource) throws JDOMException {
        XMLReaderJDOMFactory schemaFactory = new XMLReaderXSDFactory(modificationsSchemaURL);
        SAXBuilder builder = new SAXBuilder(schemaFactory);

        try {
            document = builder.build(modificationsResource.getInputStream());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        Set<Modification> result = new HashSet<>();

        if (document != null) {
            Element eRoot = document.getRootElement();

            Filter modFilter = new ElementFilter("modification");
            Iterator<Element> modIter = eRoot.getDescendants(modFilter);
            while (modIter.hasNext()) {
                Element modificationElement = modIter.next();
                Modification.Location location = readLocation(modificationElement);
                List<Element> affectedAAsElements = modificationElement.getChild("affectedAminoAcids").getChildren("affectedAminoAcid");
                Set<AminoAcid> affectedAminoAcids = new HashSet<>();
                for (Element eAffectedAA : affectedAAsElements) {
                    String letter = eAffectedAA.getValue();
                    if (letter.equals("*")) { // * is wildcard for all amino acids
                        affectedAminoAcids.addAll(Arrays.asList(AminoAcid.values()));
                    } else {
                        affectedAminoAcids.add(AminoAcid.getAA(letter));
                    }
                }

                //get all the values from the XML elements
                String name = modificationElement.getChild("name").getValue();
                double monoIsotopicMassShift = Double.parseDouble(modificationElement.getChild("monoIsotopicMassShift").getValue());
                double averageMassShift = Double.parseDouble(modificationElement.getChild("averageMassShift").getValue());
                Element accessionElement = modificationElement.getChild("accession");
                String modAccession = accessionElement.getValue();
                Element accessionValueElement = modificationElement.getChild("accessionValue");
                String modAccessionValue = accessionValueElement.getValue();

                Modification modification = new Modification(name, monoIsotopicMassShift, averageMassShift, location, affectedAminoAcids, modAccession, modAccessionValue);
                Modification.Type type = readType(modificationElement);
                if (type != null) {
                    modification.setType(type);
                }

                result.add(modification);
            }
        }

        return result;
    }

    @Override
    public void marshall(Resource modificationsResource, Collection<Modification> modifications) {
        try {
            //add root element
            Document doc = new Document();
            Element rootElement = new Element("modifications");
            for (Modification modification : modifications) {
                Element modificationElement = new Element("modification");

                Element nameElement = new Element("name");
                nameElement.setText(modification.getName());
                modificationElement.addContent(nameElement);

                Element monoIsotopicMassShiftElement = new Element("monoIsotopicMassShift");
                monoIsotopicMassShiftElement.setText(Double.toString(modification.getMonoIsotopicMassShift()));
                modificationElement.addContent(monoIsotopicMassShiftElement);

                Element averageMassShiftElement = new Element("averageMassShift");
                averageMassShiftElement.setText(Double.toString(modification.getAverageMassShift()));
                modificationElement.addContent(averageMassShiftElement);

                Element originElement = new Element("origin");
                originElement.setText(modification.getOrigin().toString().toLowerCase());
                modificationElement.addContent(originElement);

                Element locationElement = new Element("location");
                locationElement.setText(modification.getLocation().getUserFriendlyValue());
                modificationElement.addContent(locationElement);

                Element typeElement = new Element("type");
                typeElement.setText(modification.getType().toString());
                modificationElement.addContent(typeElement);

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
            OutputStream outputStream = new FileOutputStream(modificationsResource.getFile());
            xmlOutputter.output(doc, outputStream);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private Modification.Location readLocation(Element eModification) {
        Modification.Location position = null;
        String txtPosition = eModification.getChild("location").getValue();
        for (Modification.Location location : Modification.Location.values()) {
            if (txtPosition.equals(location.getUserFriendlyValue())) {
                position = location;
                break;
            }
        }

        return position;
    }

    private Modification.Type readType(Element modification) {
        Modification.Type type = null;
        if (modification.getChild("type") != null) {
            String txtType = modification.getChild("type").getValue();

            type = Modification.Type.valueOf(txtType);
        }

        return type;
    }
}
