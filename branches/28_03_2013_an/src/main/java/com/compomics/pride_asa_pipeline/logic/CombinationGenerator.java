package com.compomics.pride_asa_pipeline.logic;

import java.math.BigInteger;

/**
 * @author Jonathan Rameseder Date: 15-Jan-2008
 * @since 0.1
 */
public class CombinationGenerator {

    private int[] indices;
    private int numIndices;
    private int sizeCombination;
    private long remaining;
    private long total;

    //Discrete Mathematics and Its Applications, 2nd edition (NY: McGraw-Hill, 1991), pp. 284-286
    public CombinationGenerator(int numIndices, int sizeCombination) {
        if (sizeCombination > numIndices) {
            throw new IllegalArgumentException();
        }
        if (numIndices < 1) {
            throw new IllegalArgumentException();
        }
        if (sizeCombination < 1) {
            throw new IllegalArgumentException();
        }
        this.numIndices = numIndices;
        this.sizeCombination = sizeCombination;
        indices = new int[sizeCombination];

        BigInteger indFac = calculateFactorial(numIndices);
        BigInteger comFac = calculateFactorial(sizeCombination);
        BigInteger iMinCfac = calculateFactorial(numIndices - sizeCombination);
        this.total = indFac.divide(comFac.multiply(iMinCfac)).longValue();
        reset();
    }

    private BigInteger calculateFactorial(int n) { // factorial without recursion due to big integer objects
        BigInteger fact = BigInteger.ONE;
        for (int i = n; i > 1; --i) {
            fact = fact.multiply(new BigInteger(Integer.toString(i)));
        }
        return fact;
    }

    public void reset() {
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }
        remaining = total;
    }

    public long getRemaining() {
        return remaining;
    }

    public boolean hasMore() {
        return remaining != 0;
    }

    public long getTotal() {
        return total;
    }

    public int[] getNext() {
        if (remaining == total) {
            --remaining;
            return indices;
        }
        int i = sizeCombination - 1;
        while (indices[i] == numIndices - sizeCombination + i) {
            --i;
        }
        ++indices[i];
        for (int j = i + 1; j < sizeCombination; ++j) {
            indices[j] = indices[i] + j - i;
        }
        --remaining;
        return indices;
    }
}