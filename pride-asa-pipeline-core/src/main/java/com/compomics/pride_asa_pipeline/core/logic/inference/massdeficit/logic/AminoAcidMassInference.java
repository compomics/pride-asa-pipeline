package com.compomics.pride_asa_pipeline.core.logic.inference.massdeficit.logic;

import com.compomics.pride_asa_pipeline.core.logic.inference.massdeficit.model.AccurateAminoAcid;
import com.compomics.pride_asa_pipeline.core.logic.inference.massdeficit.model.MassDeficitResult;
import com.compomics.pride_asa_pipeline.core.logic.inference.massdeficit.model.MassMatch;
import com.compomics.pride_asa_pipeline.model.Peak;
import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.IonFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Kenneth
 */
public class AminoAcidMassInference {

    //the identified peptide including modifications
    private final Peptide peptide;
    //the charge of the precursorpeptide
    private final BigDecimal precursorCharge;
    //the used mass tolerance
    private final BigDecimal massTolerance;
    private final double[] mzValues;

    public AminoAcidMassInference(double[] mzValues, Peptide peptide, BigDecimal precursorCharge, BigDecimal massTolerance) {
        this.massTolerance = massTolerance;
        this.mzValues = mzValues;
        this.peptide = peptide;
        this.precursorCharge = precursorCharge;
    }

    public Peptide getPeptide() {
        return peptide;
    }

    public BigDecimal getMassTolerance() {
        return massTolerance;
    }

    public LinkedList<MassDeficitResult> getMassDeficits() throws IOException {
//init the linkedList
        LinkedList<MassDeficitResult> resultList = new LinkedList<>();

        //get the list of measured MZ values
        LinkedList<BigDecimal> measuredMZList = new LinkedList<>();
        for (double mz : mzValues) {
            measuredMZList.add(new BigDecimal(mz));
        }

        //get the annotated matches per charge state
        TreeMap<BigDecimal, MassMatch> annotateSpectrum = annotateSpectrum(precursorCharge, measuredMZList);
        //iterate the annotated y-fragments
        Iterator<Map.Entry<BigDecimal, MassMatch>> iterator = annotateSpectrum.entrySet().iterator();
        //first match
        if (iterator.hasNext()) {
            MassMatch firstMatch = iterator.next().getValue();
            BigDecimal massDeficit = BigDecimal.ZERO;
            while (iterator.hasNext()) {
                MassMatch secondMatch = iterator.next().getValue();
                BigDecimal gapToNext = secondMatch.getObservedMz().subtract(firstMatch.getObservedMz());
                BigDecimal secondMassDeficit = getMassDeficit(gapToNext);
                BigDecimal deficitToMassRatio = secondMassDeficit.divide(new BigDecimal(Math.max(firstMatch.getObservedMz().doubleValue(),1.0)), 10, RoundingMode.HALF_UP).multiply(new BigDecimal(10000));
                BigDecimal massRatioGap = secondMassDeficit.subtract(massDeficit);
                // write the specific fragment ion information
                MassDeficitResult result = new MassDeficitResult(getCorrespondingAminoAcid(gapToNext, massTolerance),
                        firstMatch.getObservedMz(),
                        gapToNext,
                        secondMassDeficit,
                        deficitToMassRatio,
                        massRatioGap);
                resultList.add(result);
                firstMatch = secondMatch;
                massDeficit = secondMassDeficit;
            };
        }
        return resultList;
    }

    /**
     *
     * @param mzValue a measured mzValue
     * @param tolerance a tolerance in which the aminoacid may deviate
     * @return a corresponding aminoacid string representation. This method will
     * render * in case no simple match could be found and I/L in case of the
     * indistinguishable (iso)leucine
     */
    public String getCorrespondingAminoAcid(BigDecimal mzValue, BigDecimal tolerance) {
        String matchedAminoAcid = "*";
        for (AccurateAminoAcid acid : AccurateAminoAcid.values()) {
            if (canBeMatched(acid.getAccurateMass(), mzValue, tolerance, mzValue)) {
                matchedAminoAcid = acid.toString();
                if (matchedAminoAcid.equals("I") | matchedAminoAcid.equals("L")) {
                    matchedAminoAcid = "I/L";
                }
                break;
            }
        }
        return matchedAminoAcid;
    }

    private ArrayList<Ion> getFragmentIons(int ionIndex) {
        IonFactory fragmentFactory = IonFactory.getInstance();
        HashMap<Integer, ArrayList<Ion>> product_ions_peptideA = fragmentFactory.getFragmentIons(peptide).get(0);
        return product_ions_peptideA.get(ionIndex);
    }

    public TreeMap<BigDecimal, MassMatch> annotateSpectrum(BigDecimal precursorCharge, LinkedList<BigDecimal> mzList) {
        //hashmap of the best matches for an MZ value over all charges
        TreeMap<BigDecimal, MassMatch> bestMatches = new TreeMap<>();
        //put empty MassMatches in the sequence...
        for (BigDecimal anMZvalue : mzList) {
            bestMatches.put(anMZvalue, new MassMatch(anMZvalue, anMZvalue, BigDecimal.ONE));
        }
        ArrayList<Ion> potentialIons = getFragmentIons(PeptideFragmentIon.Y_ION);
        HashMap<Integer, ArrayList<BigDecimal>> chargedIons = getChargedIons(potentialIons, precursorCharge);
        for (int aCharge : chargedIons.keySet()) {
            for (BigDecimal anTheoreticalIonMz : chargedIons.get(aCharge)) {
                //iterate the actual measured
                for (BigDecimal aMeasuredMz : mzList) {
                    BigDecimal charge = new BigDecimal(aCharge);
                    if (canBeMatched(anTheoreticalIonMz, aMeasuredMz, massTolerance, charge)) {
                        MassMatch newermatch = new MassMatch(anTheoreticalIonMz, aMeasuredMz, charge);
                        MassMatch olderMatch = bestMatches.get(aMeasuredMz);
                        if (olderMatch != null) {
                            if (newermatch.isBetterMatch(olderMatch)) {
                                bestMatches.put(aMeasuredMz, newermatch);
                            }
                        } else {
                            bestMatches.put(aMeasuredMz, newermatch);
                        }
                    }
                }
            }
        }
        return bestMatches;
    }

    /**
     *
     * @param theoreticalMz the calculated MZ
     * @param actualMz the observed MZ
     * @param tolerance the tolerance used
     * @param charge the charge of the precursor
     * @return a boolean indicating if the actual and theoretical ions match up
     */
    public boolean canBeMatched(BigDecimal theoreticalMz, BigDecimal actualMz, BigDecimal tolerance, BigDecimal charge) {
        boolean canBeMatched = true;
        BigDecimal chargeTolerance = tolerance.divide(charge, 6, RoundingMode.HALF_UP);
        BigDecimal lowerBoundary = theoreticalMz.subtract(chargeTolerance);
        BigDecimal upperBoundary = theoreticalMz.add(chargeTolerance);
        if (lowerBoundary.compareTo(actualMz) > -1) {
            canBeMatched = false;
        } else if (upperBoundary.compareTo(actualMz) < 1) {
            canBeMatched = false;
        }
        return canBeMatched;
    }

    /**
     *
     * @param potentialIons calculated ions
     * @param precursorCharge the precusor charge
     * @return an hashmap of all possible ionmasses per charge state
     */
    public HashMap<Integer, ArrayList<BigDecimal>> getChargedIons(ArrayList<Ion> potentialIons, BigDecimal precursorCharge) {
        HashMap<Integer, ArrayList<BigDecimal>> possibleChargedMzValues = new HashMap<>();
        for (int i = 1; i <= (precursorCharge.intValue() - 1); i++) {
            ArrayList<BigDecimal> chargedIonMzList = new ArrayList<>();
            for (Ion aPotentialIon : potentialIons) {
                chargedIonMzList.add(new BigDecimal(aPotentialIon.getTheoreticMz(i)).setScale(4, BigDecimal.ROUND_HALF_UP));
            }
            possibleChargedMzValues.put(i, chargedIonMzList);
        }
        return possibleChargedMzValues;
    }

    /**
     *
     * @param anMzValue
     * @return the decimal portion of a number
     */
    public BigDecimal getMassDeficit(BigDecimal anMzValue) {
        return anMzValue.remainder(BigDecimal.ONE);
    }

    /**
     *
     * @param peakList the input peaklist including intensities
     * @return a linked list of mz-values
     */
    public LinkedList<BigDecimal> getMZList(Collection<Peak> peakList) {
        LinkedList<BigDecimal> mzList = new LinkedList<>();
        for (Peak aPeak : peakList) {
            mzList.add(new BigDecimal(aPeak.getMzRatio()));
        }
        Collections.sort(mzList);
        return mzList;
    }

    /**
     *
     * @param peakList the input peaklist including intensities
     * @return a linked list of mz-values
     */
    public LinkedList<BigDecimal> getIntensityList(Collection<Peak> peakList) {
        LinkedList<BigDecimal> intensityList = new LinkedList<>();
        for (Peak aPeak : peakList) {
            intensityList.add(new BigDecimal(aPeak.getIntensity()));
        }
        return intensityList;
    }
}
