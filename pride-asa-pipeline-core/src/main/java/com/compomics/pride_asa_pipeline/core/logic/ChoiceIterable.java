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
package com.compomics.pride_asa_pipeline.core.logic;

import com.compomics.pride_asa_pipeline.core.util.MathUtils;
import java.util.Iterator;

/**
 * A class providing an iterator over all choices of a certain number
 * of elements from a given set. For a set S with n = |S|, there are
 * n!/(k!*(n-k)!) ways of choosing k elements from the set. This is
 * the number of possible samples when doing sampling without
 * replacement. Example:<br />
 * <pre>
 * S = { A,B,C,D }, n = |S| = 4
 * k = 2
 * m = n!/(k!*(n-k)!) = 6
 *
 * Choices:
 * [A, B]
 * [A, C]
 * [A, D]
 * [B, C]
 * [B, D]
 * [C, D]
 * </pre>
 *
 * from: http://www.java-forum.org/codeschnipsel-u-projekte/81973-combinatorics.html
 * @author http://www.java-forum.org/members/Marco13.html
 *
 * related info: http://answers.google.com/answers/threadview/id/734129.html
 */
public class ChoiceIterable<T> implements Iterable<T[]> {
    private T input[];
    private int sampleSize;
    private long numElements;

    /**
     * Creates an iterable over all choices of 'sampleSize'
     * elements taken from the given array.
     *
     * @param sampleSize
     * @param input
     */
    @SafeVarargs
    public ChoiceIterable(int sampleSize, T... input) {
        this.sampleSize = sampleSize;
        this.input = input.clone();

        numElements = MathUtils.factorial(input.length) /
                (MathUtils.factorial(sampleSize) * MathUtils.factorial(input.length - sampleSize));
    }

    @Override
    public Iterator<T[]> iterator() {
        return new Iterator<T[]>() {
            private int current = 0;
            private int chosen[] = new int[sampleSize];

            // Initialization of first choice
            { // initializer block
                for (int i = 0; i < sampleSize; i++){
                    chosen[i] = i;
                }
            }

            @Override
            public boolean hasNext() {
                return current < numElements;
            }

            @Override
            public T[] next() {
                @SuppressWarnings("unchecked")
                T result[] = (T[]) java.lang.reflect.Array.newInstance(
                    input.getClass().getComponentType(), sampleSize);
                for (int i = 0; i < sampleSize; i++) {
                    result[i] = input[chosen[i]];
                }
                current++;
                if (current < numElements) {
                    increase(sampleSize - 1, input.length - 1);
                }
                return result;
            }

            private void increase(int n, int max) {
                // The fist choice when choosing 3 of 5 elements consists
                // of 0,1,2. Subsequent choices are created by increasing
                // the last element of this sequence:
                // 0,1,3
                // 0,1,4
                // until the last element of the choice has reached the
                // maximum value. Then, the earlier elements of the
                // sequence are increased recursively, while obeying the
                // maximum value each element may have so that there may
                // still be values assigned to the subsequent elements.
                // For the example:
                // - The element with index 2 may have maximum value 4.
                // - The element with index 1 may have maximum value 3.
                // - The element with index 0 may have maximum value 2.
                // Each time that the value of one of these elements is
                // increased, the subsequent elements will simply receive
                // the subsequent values.
                if (chosen[n] < max) {
                    chosen[n]++;
                    for (int i = n + 1; i < sampleSize; i++) {
                        chosen[i] = chosen[i - 1] + 1;
                    }
                } else {
                    increase(n - 1, max - 1);
                }
            }

            @Override
            public void remove(){
                throw new UnsupportedOperationException("May not remove elements from a choice");
            }
        };
    }


}