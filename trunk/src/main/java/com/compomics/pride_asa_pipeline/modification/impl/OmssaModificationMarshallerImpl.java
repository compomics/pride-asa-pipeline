package com.compomics.pride_asa_pipeline.modification.impl;

import com.compomics.omssa.xsd.LocationTypeEnum;
import com.compomics.omssa.xsd.UserMod;
import com.compomics.omssa.xsd.UserModCollection;
import com.compomics.pride_asa_pipeline.model.AminoAcid;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.modification.OmssaModiciationMarshaller;
import org.apache.log4j.Logger;

import java.util.Set;

/**
 * Created by IntelliJ IDEA. User: niels Date: 17/11/11 Time: 9:54 To change
 * this template use File | Settings | File Templates.
 */
public class OmssaModificationMarshallerImpl implements OmssaModiciationMarshaller {

    private static final Logger LOGGER = Logger.getLogger(OmssaModificationMarshallerImpl.class);

    @Override
    public UserModCollection marshallModifications(Set<Modification> modificationSet) {
        UserModCollection userModCollection = new UserModCollection();

        if (!modificationSet.isEmpty()) {
            UserMod userMod = null;
            for (Modification modification : modificationSet) {
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
                    userMod.setLocationType(getLocationAsLocationTypeEnum(modification.getLocation()));

                    userModCollection.add(userMod);
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
