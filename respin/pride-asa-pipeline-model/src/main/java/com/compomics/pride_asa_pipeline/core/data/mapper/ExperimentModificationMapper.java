/*
 *

 */
package com.compomics.pride_asa_pipeline.core.data.mapper;

import com.compomics.pride_asa_pipeline.model.AminoAcid;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.model.Modification.Location;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author Niels Hulstaert
 */
public class ExperimentModificationMapper implements RowMapper<Modification> {

    @Override
    public Modification mapRow(ResultSet rs, int i) throws SQLException {        
        String peptide = rs.getString("sequence");
        String modificationAccession = rs.getString("accession");
        Integer modificationLocation = rs.getInt("location");


        String modificationName = rs.getString("name");
        double modificationMassDelta = rs.getDouble("mass_delta_value");
        String modificationConcatLocationName = rs.getString("gr");

        if(modificationConcatLocationName == null){
            return null;
        }

        Location location = null;
        int sequenceIndex = 0;
        if (modificationLocation == 0) {
            location = Location.N_TERMINAL;
            sequenceIndex = 0;
        } else if (modificationLocation == (peptide.length() + 1)) {
            location = Location.C_TERMINAL;
            sequenceIndex = peptide.length() + 1;
        } else {
            location = Location.NON_TERMINAL;
            sequenceIndex = modificationLocation - 1;
        }

        Modification modification = new Modification(modificationMassDelta, location, modificationAccession, modificationName);
        
        if(location == Location.NON_TERMINAL){
            modification.getAffectedAminoAcids().add(AminoAcid.getAA(peptide.substring(sequenceIndex, sequenceIndex + 1)));
        }        
        modification.setOrigin(Modification.Origin.PRIDE);
        
        return modification;
    }
}
