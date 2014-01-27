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
public class PrecursorModificationMapper implements RowMapper<Modification> {

    @Override
    public Modification mapRow(ResultSet rs, int i) throws SQLException {
        String modificationAccession = rs.getString("accession");
        String modificationName = rs.getString("name");
        String peptideSequence = rs.getString("sequence");
        int locationInt = rs.getInt("location");
        double massShift = rs.getDouble("mass_delta_value");

        Location location = null;
        int sequenceIndex = 0;
        if (locationInt == 0) {
            location = Location.N_TERMINAL;
            sequenceIndex = 0;
        } else if (locationInt == (peptideSequence.length() + 1)) {
            location = Location.C_TERMINAL;
            sequenceIndex = peptideSequence.length() + 1;
        } else {
            location = Location.NON_TERMINAL;
            sequenceIndex = locationInt - 1;
        }

        Modification modification = new Modification(massShift, location, modificationAccession, modificationName);
        
        modification.getAffectedAminoAcids().add(AminoAcid.getAA(peptideSequence.substring(sequenceIndex, sequenceIndex + 1)));
        modification.setOrigin(Modification.Origin.PRIDE);

        return modification;
    }
}
