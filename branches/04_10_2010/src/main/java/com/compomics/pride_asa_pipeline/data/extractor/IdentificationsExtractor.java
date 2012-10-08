/*
 */
package com.compomics.pride_asa_pipeline.data.extractor;

import com.compomics.pride_asa_pipeline.model.AminoAcidSequence;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.model.Peptide;
import com.compomics.pride_asa_pipeline.model.UnknownAAException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 *
 * @author Niels Hulstaert
 */
public class IdentificationsExtractor implements ResultSetExtractor<List<Identification>> {

    private static final Logger LOGGER = Logger.getLogger(IdentificationsExtractor.class);

    @Override
    public List<Identification> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<Identification> identifications = new ArrayList<Identification>();
        while (rs.next()) {
            try {
                //sequence
                String sequence = rs.getString("sequence");
                //charge state
                int chargeState = -1;
                if (rs.getString("charge_state") != null && !"".equals(rs.getString("charge_state"))) {
                    chargeState = rs.getInt("charge_state");
                }
                //mz ratio
                double mz = rs.getDouble("mz");
                //accession
                String accession = rs.getString("accession");
                //spectrum ID
                long spectrumId = rs.getLong("spectrum_id");
                //spectrum ref
                long spectrumRef = rs.getLong("spectrum_identifier");
                //peptide ID
                long peptideId = rs.getLong("peptide_id");

                Peptide peptide = new Peptide(chargeState, mz, new AminoAcidSequence(sequence), peptideId);
                
                identifications.add(new Identification(peptide, accession, spectrumId, spectrumRef));
            } catch (UnknownAAException ex) {
                LOGGER.info("Got peptide with unknown amino acid!");
            }
        }

        return identifications;
    }
}
