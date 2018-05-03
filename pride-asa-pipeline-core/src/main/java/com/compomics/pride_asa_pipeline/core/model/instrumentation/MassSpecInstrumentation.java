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
package com.compomics.pride_asa_pipeline.core.model.instrumentation;

import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Kenneth Verheggen
 */
public class MassSpecInstrumentation {

    private double precAcc;
    private double fragAcc;
    private AnalyzerData data;
    private TreeSet<Integer> possibleCharges;

    public MassSpecInstrumentation() {
        this.possibleCharges = new TreeSet<>();
    }

    public AnalyzerData getData() {
        return data;
    }

    public void setData(AnalyzerData data) {
        this.data = data;
    }

    public TreeSet<Integer> getPossibleCharges() {
        return possibleCharges;
    }

    public void setPossibleCharges(TreeSet<Integer> possibleCharges) {
        this.possibleCharges = possibleCharges;
    }

    public double getPrecAcc() {
        return precAcc;
    }

    public void setPrecAcc(double precAcc) {
        this.precAcc = precAcc;
    }

    public double getFragAcc() {
        return fragAcc;
    }

    public void setFragAcc(double fragAcc) {
        this.fragAcc = fragAcc;
    }

}
