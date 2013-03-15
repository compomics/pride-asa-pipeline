package com.compomics.pride_asa_pipeline.model;

import com.compomics.pride_asa_pipeline.config.PropertiesConfigurationHolder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Florian Reisinger Date: 11-Aug-2009
 * @since 0.1
 */
public class MassRecalibrationResult {

    //key:    charge state
    //value:  mass error for the charge state
    private Map<Integer, Double> massErrors = new HashMap<Integer, Double>();
    //key:    charge state
    //value:  mass error window for the mass at a given charge charge state
    private Map<Integer, Double> massErrorWindows = new HashMap<Integer, Double>();

    /**
     * No-arg contructor
     */
    public MassRecalibrationResult() {
        this.massErrors = new HashMap<Integer, Double>();
        this.massErrorWindows = new HashMap<Integer, Double>();
    }

    /**
     * Adds an mass error the recalibration result
     *
     * @param charge the charge value
     * @param error the mass error
     * @param errorWindow the mass error window
     */
    public void addMassError(int charge, double error, double errorWindow) {
        massErrors.put(charge, error);
        massErrorWindows.put(charge, errorWindow);
    }

    /**
     * Gets the available charges
     *
     * @return the charges
     */
    public Set<Integer> getCharges() {
        return massErrors.keySet();
    }

    /**
     * Gets the mass error for the given charge state
     *
     * @param chargeState the charge state to get the error for.
     * @return the Double value for the recorded error at the given charge. Note
     * that this can be null if there is no mass error for the given charge!
     */
    public Double getError(int chargeState) {
        return massErrors.get(chargeState);
    }

    /**
     * Gets the mass error window for the given charge state
     *
     * @param chargeState the charge state to get the error window for.
     * @return the Double value for the recorded error window at the given
     * charge. Note that this can be null if there is no mass error for the
     * given charge!
     */
    public Double getErrorWindow(int chargeState) {
        return massErrorWindows.get(chargeState);
    }

    /**
     * Checks if the maximum systematic mass error is exceeded for one or more
     * charge states.
     *
     * @return the exceeds boolean
     */
    public boolean exceedsMaximumSystematicMassError() {
        boolean exceedsMaxError = Boolean.FALSE;
        for (int charge : getCharges()) {
            if (getError(charge) > PropertiesConfigurationHolder.getInstance().getDouble("massrecalibrator.maximum_systematic_mass_error")) {
                exceedsMaxError = true;
                break;
            }
        }

        return exceedsMaxError;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Integer charge : massErrors.keySet()) {
            stringBuilder.append("charge: " + charge + ", " + massErrors.get(charge) + ", error window: " + massErrorWindows.get(charge) + "\n");
        }
        return stringBuilder.toString();
    }
}
