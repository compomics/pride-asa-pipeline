/*
 *

 */
package com.compomics.pride_asa_pipeline.core.repository.impl.jdbc;

import com.compomics.pride_asa_pipeline.core.data.mapper.ExperimentModificationMapper;
import com.compomics.pride_asa_pipeline.core.data.mapper.PrecursorModificationMapper;
import com.compomics.pride_asa_pipeline.core.repository.ModificationRepository;
import com.compomics.pride_asa_pipeline.model.Modification;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * @author Niels Hulstaert
 */
public class JdbcModificationRepository extends JdbcDaoSupport implements ModificationRepository {

    private static final Logger LOGGER = Logger.getLogger(JdbcModificationRepository.class);
    private static final String SELECT_MODIFICATION_BY_PEPTIDE_ID = new StringBuilder()
            .append("select ")
            .append("modif.accession, modif.location, modif_param.name, pep.sequence, mass_del.mass_delta_value ")
            .append("from ")
            .append("pride_peptide pep, ")
            .append("pride_modification modif, ")
            .append("pride_modification_param modif_param, ")
            .append("pride_mass_delta mass_del ")
            .append("where modif.peptide_id = pep.peptide_id ")
            .append("and pep.peptide_id = ? ")
            .append("and modif.modification_id = modif_param.parent_element_fk ")
            .append("and mass_del.modification_id = modif.modification_id; ").toString();
    private static final String SELECT_MODIFICATION_BY_EXPERIMENT_ID = new StringBuilder()
            .append("select ")
            .append("modif.accession, modif.location, modif_param.name, mass_del.mass_delta_value, pep.sequence, CONCAT(SUBSTR(pep.sequence, modif.location,1), \"_\", name) as gr ")
            .append("from ")
            .append("pride_modification modif, ")
            .append("pride_modification_param modif_param, ")
            .append("pride_mass_delta mass_del, ")
            .append("pride_peptide pep, ")
            .append("pride_identification iden, ")
            .append("pride_experiment exp ")
            .append("where ")
            .append("modif.peptide_id = pep.peptide_id ")
            .append("and iden.identification_id = pep.identification_id ")
            .append("and exp.experiment_id = iden.experiment_id ")
            .append("and modif.modification_id = modif_param.parent_element_fk ")
            .append("and mass_del.modification_id = modif.modification_id ")
            .append("and exp.accession = ? ")
            .append("group by gr ").toString();

    @Override
    public List<Modification> getModificationsByPeptideId(long peptideId) {
        LOGGER.debug("Loading modifications for precursor with peptide id " + peptideId);
        List<Modification> modifications = getJdbcTemplate().query(SELECT_MODIFICATION_BY_PEPTIDE_ID, new PrecursorModificationMapper(), new Object[]{peptideId});
        LOGGER.debug("Finished loading modifications for precursor with peptide id " + peptideId);
        return modifications;
    }

    @Override
    public List<Modification> getModificationsByExperimentId(String experimentId) {
        LOGGER.debug("Loading modifications for experimentid " + experimentId);
        List<Modification> modifications = getJdbcTemplate().query(SELECT_MODIFICATION_BY_EXPERIMENT_ID, new ExperimentModificationMapper(), new Object[]{experimentId});
        LOGGER.debug("Finished loading modifications for pride experiment with id " + experimentId);
        return modifications;
    }
}
