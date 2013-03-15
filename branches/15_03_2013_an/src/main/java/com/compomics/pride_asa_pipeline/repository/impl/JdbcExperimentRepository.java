/*
 *

 */
package com.compomics.pride_asa_pipeline.repository.impl;

import com.compomics.pride_asa_pipeline.data.extractor.AnalyzerSourcesExctractor;
import com.compomics.pride_asa_pipeline.data.extractor.ExperimentAccessionResultExtractor;
import com.compomics.pride_asa_pipeline.data.extractor.IdentificationsExtractor;
import com.compomics.pride_asa_pipeline.data.mapper.AnalyzerDataMapper;
import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.Identification;
import com.compomics.pride_asa_pipeline.repository.ExperimentRepository;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 *
 * @author Niels Hulstaert
 */
public class JdbcExperimentRepository extends JdbcDaoSupport implements ExperimentRepository {

    private static final Logger LOGGER = Logger.getLogger(JdbcExperimentRepository.class);
    private static final String SELECT_EXPERIMENT_ACCESSIONS = new StringBuilder()
            .append("select exp.accession as accession, exp.title as title ")
            .append("from pride_experiment exp")
            .append(" order by exp.accession+0 asc").toString();
    private static final String SELECT_EXPERIMENT_ACCESSIONS_BY_TAXONOMY = new StringBuilder()
            .append("select exp.accession as accession, exp.title as title ")
            .append("from pride_experiment exp, mzdata_sample_param sample ")
            .append("where sample.parent_element_fk = exp.mz_data_id ")
            .append("and sample.accession = ? ")
            .append(" order by exp.accession+0 asc").toString();
    //PSI:1000040 is "Mass to Charge Ratio" and
    //PSI:1000041 is "Charge State"
    //MS:1000744 is "Mass to Charge Ratio" and
    //MS:1000041 is "Charge State"
    private static final String SELECT_EXPERIMENT_IDENTIFICATIONS = new StringBuilder()
            .append("select main.*, par1.value as mz, par2.value as charge_state from ( ")
            .append("select spec.spectrum_identifier, ")
            .append("spec.spectrum_id, ")
            .append("exp.accession, ")
            .append("mzdata.mz_data_id, ")
            .append("pep.sequence, ")
            .append("pep.peptide_id, ")
            .append("prec.precursor_id ")
            .append("from ")
            .append("pride_experiment exp, ")
            .append("mzdata_mz_data mzdata, ")
            .append("pride_identification iden, ")
            .append("pride_peptide pep, ")
            .append("mzdata_spectrum spec, ")
            .append("mzdata_precursor prec ")
            .append("where ")
            .append("exp.accession = ? and ")
            .append("exp.mz_data_id = mzdata.mz_data_id and ")
            .append("exp.experiment_id = iden.experiment_id and ")
            .append("iden.identification_id = pep.identification_id and ")
            .append("pep.spectrum_ref = spec.spectrum_identifier and ")
            .append("mzdata.mz_data_id = spec.mz_data_id and ")
            .append("spec.spectrum_id = prec.spectrum_id ")
            .append(") main ")
            .append("left join mzdata_ion_selection_param par1 on par1.parent_element_fk = main.precursor_id and par1.accession in ('PSI:1000040', 'MS:1000744') ")
            .append("left join mzdata_ion_selection_param par2 on par2.parent_element_fk = main.precursor_id and par2.accession in ('PSI:1000041', 'MS:1000041') ").toString();
    //this select is with a group by spectrum_identifier
    //private static final String SELECT_EXPERIMENT_IDENTIFICATIONS = new StringBuilder().append("select main.*, par1.value as mz, par2.value as charge_state from ( ").append("select spec.spectrum_identifier, ").append("spec.spectrum_id, ").append("exp.accession, ").append("mzdata.mz_data_id, ").append("pep.sequence, ").append("pep.peptide_id, ").append("prec.precursor_id ").append("from ").append("pride_experiment exp, ").append("mzdata_mz_data mzdata, ").append("pride_identification iden, ").append("pride_peptide pep, ").append("mzdata_spectrum spec, ").append("mzdata_precursor prec ").append("where ").append("exp.accession = ? and ").append("exp.mz_data_id = mzdata.mz_data_id and ").append("exp.experiment_id = iden.experiment_id and ").append("iden.identification_id = pep.identification_id and ").append("pep.spectrum_ref = spec.spectrum_identifier and ").append("mzdata.mz_data_id = spec.mz_data_id and ").append("spec.spectrum_id = prec.spectrum_id ").append("group by spec.spectrum_identifier ").append(") main ").append("left join mzdata_ion_selection_param par1 on par1.parent_element_fk = main.precursor_id and par1.accession in ('PSI:1000040', 'MS:1000744') ").append("left join mzdata_ion_selection_param par2 on par2.parent_element_fk = main.precursor_id and par2.accession in ('PSI:1000041', 'MS:1000041') ").toString();
    //PSI:1000008 Ionizer Type
    //PSI:1000075 MALDI
    private static final String SELECT_ANALYZER_SOURCE_FOR_MALDI = new StringBuilder()
            .append("select accession, value ")
            .append("from mzdata_mz_data d, mzdata_analyzer_param p ")
            .append("where p.parent_element_fk = d.mz_data_id ")
            .append("and d.accession_number = ? ")
            .append("and (accession = 'PSI:1000008' or accession = 'PSI:1000075') ").toString();
    private static final String SELECT_ANALYZER_SOURCES = new StringBuilder()
            .append("SELECT ")
            .append(" par.name, ")
            .append(" par.value ")
            .append(" FROM mzdata_mz_data mz")
            .append(" LEFT JOIN mzdata_analyzer ana using (mz_data_id) ")
            .append(" LEFT JOIN mzdata_analyzer_param par ON par.parent_element_fk = ana.analyzer_id and par.accession IN ('PSI:1000010','PSI:1000264','PSI:1000284','PSI:1000291',").append(buildAnalyzerCVlist()).append(") ").append(" WHERE mz.accession_number = ?").toString();
    private static final String NUMBER_OF_SPECTRA = new StringBuilder()
            .append("select count(exp.accession) ")
            .append("from pride_experiment exp, mzdata_mz_data mzdata, mzdata_spectrum spec ")
            .append("where exp.mz_data_id = mzdata.mz_data_id ")
            .append("and mzdata.mz_data_id = spec.mz_data_id ")
            .append("and exp.accession = ? ").toString();
    private static final String SELECT_PROTEIN_ACCESSIONS = new StringBuilder()
            .append("select ")
            .append("distinct iden.accession_number ")
            .append("from pride_experiment exp, ")
            .append("pride_identification iden ")
            .append("where exp.accession = ? ")
            .append("and exp.experiment_id = iden.experiment_id").toString();
    private static final String NUMBER_OF_PEPTIDES = new StringBuilder()
            .append("select count(exp.accession)")
            .append("from pride_experiment exp, ")
            .append("mzdata_mz_data mzdata, ")
            .append("pride_identification iden, ")
            .append("pride_peptide pep, ")
            .append("mzdata_spectrum spec, ")
            .append("mzdata_precursor prec ")
            .append("where exp.mz_data_id = mzdata.mz_data_id ")
            .append("and exp.experiment_id = iden.experiment_id ")
            .append("and iden.identification_id = pep.identification_id ")
            .append("and pep.spectrum_ref = spec.spectrum_identifier ")
            .append("and mzdata.mz_data_id = spec.mz_data_id ")
            .append("and spec.spectrum_id = prec.spectrum_id ")
            .append("and exp.accession = ? ").toString();
    private static final String SELECT_SPECTRUM_IDS = new StringBuilder()
            .append("select spec.spectrum_id ")
            .append("from pride_experiment exp, mzdata_mz_data mzdata, mzdata_spectrum spec ")
            .append("where exp.mz_data_id = mzdata.mz_data_id ")
            .append("and mzdata.mz_data_id = spec.mz_data_id ")
            .append("and exp.accession = ? ").toString();
    private static final String SELECT_SPECTRA_METADATA = new StringBuilder()
            .append("select ")
            .append("main.spectrum_id, par1.value as precursor_mz, par2.value as precursor_charge_state ")
            .append("from ")
            .append("( ")
            .append("   select ")
            .append("   spec.spectrum_id, prec.precursor_id ")
            .append("   from pride_experiment exp, ")
            .append("   mzdata_mz_data mzdata, ")
            .append("   mzdata_spectrum spec, ")
            .append("   mzdata_precursor prec ")
            .append("   where exp.mz_data_id = mzdata.mz_data_id ")
            .append("   and mzdata.mz_data_id = spec.mz_data_id ")
            .append("   and spec.spectrum_id = prec.spectrum_id ")
            .append("   and exp.accession = ? ")
            .append(") ")
            .append("as main ")
            .append("left join mzdata_ion_selection_param par1 on par1.parent_element_fk = main.precursor_id ")
            .append("and par1.accession = 'PSI:1000040' ")
            .append("left join mzdata_ion_selection_param par2 on par2.parent_element_fk = main.precursor_id ")
            .append("and par2.accession = 'PSI:1000041' ")
            .append("; ").toString();

    @Override
    public Map<String, String> findAllExperimentAccessions() {
        return getJdbcTemplate().query(SELECT_EXPERIMENT_ACCESSIONS, new ExperimentAccessionResultExtractor());
    }

    @Override
    public Map<String, String> findExperimentAccessionsByTaxonomy(int taxonomyId) {
        return getJdbcTemplate().query(SELECT_EXPERIMENT_ACCESSIONS_BY_TAXONOMY, new ExperimentAccessionResultExtractor(), new Object[]{taxonomyId});
    }

    @Override
    public List<Identification> loadExperimentIdentifications(String experimentAccession) {
        LOGGER.debug("Start loading identifications for experiment " + experimentAccession);
        List<Identification> identifications = getJdbcTemplate().query(SELECT_EXPERIMENT_IDENTIFICATIONS, new IdentificationsExtractor(), new Object[]{experimentAccession});
        LOGGER.debug("Finished loading " + identifications.size() + " identifications for experiment " + experimentAccession);
        return identifications;
    }

    @Override
    public Map<String, String> getAnalyzerSources(String experimentAccession) {
        LOGGER.debug("Start loading analyzer sources for experiment " + experimentAccession);
        Map<String, String> analyzerSources = getJdbcTemplate().query(SELECT_ANALYZER_SOURCE_FOR_MALDI, new AnalyzerSourcesExctractor(), new Object[]{experimentAccession});
        LOGGER.debug("Finished loading analyzer sources identifications for experiment " + experimentAccession);
        return analyzerSources;
    }

    @Override
    public List<AnalyzerData> getAnalyzerData(String experimentAccession) {
        LOGGER.debug("Start loading analyzer data for experiment " + experimentAccession);
        List<AnalyzerData> analyzerDataList = getJdbcTemplate().query(SELECT_ANALYZER_SOURCES, new AnalyzerDataMapper(), new Object[]{experimentAccession});
        LOGGER.debug("Finished loading analyzer data for experiment " + experimentAccession);
        return analyzerDataList;
    }

    @Override
    public long getNumberOfSpectra(String experimentAccession) {
        LOGGER.debug("Start counting number of spectra for experiment " + experimentAccession);
        long numberOfSpectra = getJdbcTemplate().queryForLong(NUMBER_OF_SPECTRA, experimentAccession);
        LOGGER.debug("Finished counting number of spectra for experiment " + experimentAccession);
        return numberOfSpectra;
    }

    @Override
    public List<String> getProteinAccessions(String experimentAccession) {
        LOGGER.debug("Start retrieving protein accessions for experiment " + experimentAccession);
        List<String> proteinAccesions = getJdbcTemplate().queryForList(SELECT_PROTEIN_ACCESSIONS, new Object[]{experimentAccession}, String.class);
        LOGGER.debug("Start retrieving protein accessions for experiment " + experimentAccession);
        return proteinAccesions;
    }

    @Override
    public long getNumberOfPeptides(String experimentAccession) {
        LOGGER.debug("Start counting number of peptides for experiment " + experimentAccession);
        long numberOfPeptides = getJdbcTemplate().queryForLong(NUMBER_OF_PEPTIDES, experimentAccession);
        LOGGER.debug("Finished counting number of peptides for experiment " + experimentAccession);
        return numberOfPeptides;
    }

    @Override
    public List<Map<String, Object>> getSpectraMetadata(String experimentAccession) {
        LOGGER.debug("Start retrieving spectra metadata for experiment " + experimentAccession);
        List<Map<String, Object>> spectraMetadata = getJdbcTemplate().queryForList(SELECT_SPECTRA_METADATA, experimentAccession);
        LOGGER.debug("Finished retrieving spectrum metadata for experiment for experiment " + experimentAccession);
        return spectraMetadata;
    }

    /**
     * All these parameters correspond to Analyzer types defined in the PSI
     * ontology.
     *
     * @return a String listing (comma separated and put in single quotes) PSI
     * accessions of all known analyzer types.
     */
    private static String buildAnalyzerCVlist() {

        StringBuilder retval = new StringBuilder();
        //build proper analyser PSI term string for SQL
        for (int i = 78; i <= 84; i++) {
            retval.append("'").append("PSI:10000").append(i).append("',");
        }
        //build proper analyser PSI term string for SQL
        for (int i = 139; i <= 204; i++) {
            retval.append("'").append("PSI:1000").append(i).append("',");
        }
        //build proper analyser PSI term string for SQL
        for (int i = 447; i <= 450; i++) {
            retval.append("'").append("PSI:1000").append(i).append("'");
            if (i < 450) {
                retval.append(",");
            }
        }
        return retval.toString();
    }
}
