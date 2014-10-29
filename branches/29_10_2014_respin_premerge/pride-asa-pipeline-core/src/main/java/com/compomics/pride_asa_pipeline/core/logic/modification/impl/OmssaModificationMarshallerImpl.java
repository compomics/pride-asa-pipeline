package com.compomics.pride_asa_pipeline.core.logic.modification.impl;

import com.compomics.omssa.xsd.LocationTypeEnum;
import com.compomics.omssa.xsd.UserMod;
import com.compomics.omssa.xsd.UserModCollection;
import com.compomics.pride_asa_pipeline.core.logic.modification.OmssaModificationMarshaller;
import com.compomics.pride_asa_pipeline.model.AminoAcid;
import static com.compomics.pride_asa_pipeline.model.Modification.Location.C_TERMINAL;
import static com.compomics.pride_asa_pipeline.model.Modification.Location.NON_TERMINAL;
import static com.compomics.pride_asa_pipeline.model.Modification.Location.N_TERMINAL;
import com.compomics.pride_asa_pipeline.model.Modification;
import java.util.Set;
import org.apache.log4j.Logger;

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
}
