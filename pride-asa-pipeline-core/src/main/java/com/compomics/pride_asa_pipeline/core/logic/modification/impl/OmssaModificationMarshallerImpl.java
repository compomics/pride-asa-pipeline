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
package com.compomics.pride_asa_pipeline.core.logic.modification.impl;

import com.compomics.omssa.xsd.LocationTypeEnum;
import com.compomics.omssa.xsd.UserMod;
import com.compomics.omssa.xsd.UserModCollection;
import com.compomics.pride_asa_pipeline.core.logic.modification.OmssaModificationMarshaller;
import com.compomics.pride_asa_pipeline.core.util.ResourceUtils;
import com.compomics.pride_asa_pipeline.model.AminoAcid;
import com.compomics.pride_asa_pipeline.model.Modification;
import static com.compomics.pride_asa_pipeline.model.Modification.Location.C_TERMINAL;
import static com.compomics.pride_asa_pipeline.model.Modification.Location.NON_TERMINAL;
import static com.compomics.pride_asa_pipeline.model.Modification.Location.N_TERMINAL;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.springframework.core.io.Resource;

/**
 * Created by IntelliJ IDEA. User: niels Date: 17/11/11 Time: 9:54 To change
 * this template use File | Settings | File Templates.
 */
public class OmssaModificationMarshallerImpl implements OmssaModificationMarshaller {

    private static final Logger LOGGER = Logger.getLogger(OmssaModificationMarshallerImpl.class);

    @Override
    public UserModCollection marshallModifications(Set<Modification> modificationSet) {
        UserModCollection userModCollection = new UserModCollection();

        if (!modificationSet.isEmpty()) {
            UserMod userMod = null;
            for (Modification modification : modificationSet) {

                if (modification != null) {
                    //add a separate modification for each affected amino acid
                    if (!modification.getAffectedAminoAcids().isEmpty()) {
                        for (AminoAcid aminoAcid : modification.getAffectedAminoAcids()) {
                            userMod = new UserMod();
                            userMod.setFixed(false);
                            userMod.setMass(modification.getMassShift());
                            userMod.setModificationName(modification.getName());
                            userMod.setLocationType(getLocationAsLocationTypeEnum(modification.getLocation()));
                            userMod.setLocation(String.valueOf(aminoAcid.letter()));

                            userModCollection.add(userMod);
                        }
                    } else {
                        userMod = new UserMod();
                        userMod.setFixed(false);
                        userMod.setMass(modification.getMassShift());
                        userMod.setModificationName(modification.getName());
                        LocationTypeEnum lLocationType = getLocationAsLocationTypeEnum(modification.getLocation());
                        userMod.setLocationType(lLocationType);
                        if (lLocationType.equals(LocationTypeEnum.MODNP)) {
                            userMod.setLocation("[");
                        } else if (lLocationType.equals(LocationTypeEnum.MODCP)) {
                            userMod.setLocation("]");
                        }

                        userModCollection.add(userMod);
                    }
                }
            }
        }

        return userModCollection;
    }

    private LocationTypeEnum getLocationAsLocationTypeEnum(Modification.Location location) {
        LocationTypeEnum locationType = null;
        switch (location) {
            case N_TERMINAL:
                locationType = LocationTypeEnum.MODNP;
                break;
            case NON_TERMINAL:
                locationType = LocationTypeEnum.MODAA;
                break;
            case C_TERMINAL:
                locationType = LocationTypeEnum.MODCP;
                break;
            default:
                break;
        }

        return locationType;
    }

    public static void main(String[] args) throws JDOMException {
        OmssaModificationMarshallerImpl omssaModificationMarshallerImpl = new OmssaModificationMarshallerImpl();
        Set<Modification> unmarshall = omssaModificationMarshallerImpl.unmarshall(ResourceUtils.getResourceByRelativePath("/resources/searchGUI_to_pride_mods_common.xml"));
        for (Modification aMod : unmarshall) {
            System.out.println(aMod.getName());
        }
    }

    /**
     * Unmarshalls the given modifications XML resource and returns a set of
     * modifications
     *
     * @param modificationsResource the OMSSA (or searchGUI) modifications XML resource to be parsed
     * @return the set of modifications
     * @exception JDOMException
     */
    @Override
    public Set<Modification> unmarshall(Resource searchGuiModificationsResource) throws JDOMException {
        SAXBuilder builder = new SAXBuilder();
        Document document = null;
        try {
            document = builder.build(searchGuiModificationsResource.getInputStream());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        HashMap<Modification, Integer> result = new HashMap<>();

        if (document != null) {
            Element eRoot = document.getRootElement();
            List<Element> list = eRoot.getChildren("MSModSpec");
            for (Element modificationElement : list) {
                Element residues = modificationElement.getChild("MSModSpec_residues");
                Set<AminoAcid> affectedAminoAcids = new HashSet<>();
                if (residues != null) {
                    for (Element affectedAminoAcidClass : residues.getChildren("MSModSpec_residues_E")) {
                        String letter = affectedAminoAcidClass.getText();
                        if (letter.equals("*")) { // * is wildcard for all amino acids
                            affectedAminoAcids.addAll(Arrays.asList(AminoAcid.values()));
                        } else {
                            affectedAminoAcids.add(AminoAcid.getAA(letter));
                        }
                    }
                } else {
                    affectedAminoAcids.addAll(Arrays.asList(AminoAcid.values()));
                }
                //get all the values from the XML elements
                Element modNameElement = modificationElement.getChild("MSModSpec_name");
                if (modNameElement != null) {
                    String name = modNameElement.getValue();
                    Modification.Location location = null;

                    Element locationElement = modificationElement.getChild("MSModSpec_type");
                    if (locationElement != null) {
                        Element child = locationElement.getChild("MSModType");
                        if (child != null) {
                            String omssaLocation = child.getAttributeValue("value");
                            if (omssaLocation.toLowerCase().contains("n")) {
                                location = Modification.Location.N_TERMINAL;
                            } else if (omssaLocation.toLowerCase().contains("c")) {
                                location = Modification.Location.C_TERMINAL;
                            } else {
                                location = Modification.Location.NON_TERMINAL;
                            }
                        }
                    }
                    //contingency plan
                    if (location == null) {
                        location = Modification.Location.NON_TERMINAL;
                    }

                    double monoIsotopicMassShift = Double.parseDouble(modificationElement.getChild("MSModSpec_monomass").getValue());
                    double averageMassShift = Double.parseDouble(modificationElement.getChild("MSModSpec_averagemass").getValue());
                    Element accessionValueElement = modificationElement.getChild("MSModSpec_unimod");
                    //todo format niceer..
                    String modAccessionValue;
                    if (accessionValueElement != null) {
                        modAccessionValue = "UNIMOD:" + String.format("%05d", Integer.parseInt(accessionValueElement.getValue()));
                    } else {
                        modAccessionValue = "OMSSA:UNKNOWN_ID";
                    }
                    Modification modification = new Modification(name, monoIsotopicMassShift, averageMassShift, location, affectedAminoAcids, modAccessionValue, modAccessionValue);
                    Modification.Type type = readType(modificationElement);
                    if (type != null) {
                        modification.setType(type);
                    }
                    LOGGER.info("Unmarshalled " + modification.getName());
                    Element rarity = modificationElement.getChild("MSModSpec_occurence");
                    int occurencesInPride = 0;
                    if (rarity != null) {
                        occurencesInPride = Integer.parseInt(rarity.getValue());
                    }
                    result.put(modification, occurencesInPride);
                }
            }
        }
        //order the result set
        Map sortedMap = new TreeMap(new ValueComparator(result));
        sortedMap.putAll(result);
        return sortedMap.keySet();
    }

    private Modification.Type readType(Element modification) {
        Modification.Type type = null;
        if (modification.getChild("type") != null) {
            String txtType = modification.getChild("type").getValue();

            type = Modification.Type.valueOf(txtType);
        }

        return type;
    }

    private class ValueComparator implements Comparator {

        Map map;

        public ValueComparator(Map map) {
            this.map = map;
        }

        @Override
        public int compare(Object keyA, Object keyB) {
            int valueA = (Integer) map.get(keyA);
            int valueB = (Integer) map.get(keyB);
            //we want to sort from high value to low !
            int equality = -1;
            if (valueA < valueB) {
                equality = 1;
            }
            return equality;
        }

    }
}
