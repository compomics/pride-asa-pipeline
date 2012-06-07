package com.compomics.pride_asa_pipeline.modification.impl;

import com.compomics.pride_asa_pipeline.config.PropertiesConfigurationHolder;
import com.compomics.pride_asa_pipeline.model.AminoAcid;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.modification.ModificationMarshaller;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;
import org.jdom.filter.Filter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

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
    private Document document = null;
    private boolean useMonoIsotopicMasses = PropertiesConfigurationHolder.getInstance().getBoolean("modification.use_monoisotopic_mass");

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
    
    @Override
    public File marshall(Set<Modification> modifications) {
        File modificationsFile = new File(PropertiesConfigurationHolder.getInstance().getString("modification.pipeline_modifications_file_name" + "_new"));
        
        try {
            //add root element
            Document doc = new Document(new Element("modifications"));
            for (Modification modification : modifications) {
                Element modificationElement = new Element("modification");
                Element nameElement = new Element("name");
                if (modification.getName() != null) {
                    nameElement.setText(modification.getModificationName());
                }
                Element monoIsotopicMassShiftElement = new Element("monoIsotopicMassShift");  
                Element averageMassShiftElement = new Element("averageMassShift");  
                if (useMonoIsotopicMasses) {
                }
                doc.getRootElement().
                        addContent(new Element("Stanza").addContent(new Element("Line").setText("Once, upon a midnight dreary")).
                        addContent(new Element("Line").setText("While I pondered, weak and weary")));
            }
            
            
            new XMLOutputter().output(doc, System.out);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        
        return modificationsFile;
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
