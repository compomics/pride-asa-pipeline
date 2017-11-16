/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.util;

import com.compomics.pride_asa_pipeline.model.Identification;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class IdentificationQueue implements Iterable<Identification> {

    private Identification[] q;
    private int N;
    private int first;
    private int last;

    private static IdentificationQueue INSTANCE;
    private static final Random RAND = new Random();

    public static IdentificationQueue getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new IdentificationQueue();
        }
        return INSTANCE;
    }

    private IdentificationQueue() // construct an empty randomized
    // queue
    {
        q = (Identification[]) new Identification[4];
    }

    // resize the underlying array
    private void resize(int max) {
        assert max >= N;
        Identification[] temp = new Identification[max];
        for (int i = 0; i < N; i++) {
            temp[i] = q[(first + i) % q.length];
        }
        q = temp;
        first = 0;
        last = N;
    }

    public boolean isEmpty() // is the queue empty?
    {
        return N == 0;
    }

    public int size() // return the number of items on
    // the queue
    {
        return N;
    }

    private void swap(int i, int j) {
        Identification t = q[j];
        q[j] = q[i];
        q[i] = t;
    }

    public void enqueue(Identification item) // add the item
    {
        // double size of array if necessary and recopy to front of array

        if (item == null) {
            throw new NullPointerException();
        }

        if (N == q.length) {
            resize(2 * q.length);   // double size of array if necessary
        }
        q[last++] = item;                        // add item
        if (last == q.length) {
            last = 0;          // wrap-around
        }
        N++;

        // swap the choosen on with the last one.
        if (N > 2) {
            int choosen = RAND.nextInt(N);

            int index = (first + choosen) % q.length;
            if (last == 0) {
                swap(q.length - 1, index);
            } else {
                swap(last - 1, index);
            }
        }
    }

    public Identification dequeue() // delete and return a random item
    {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }

        Identification item = q[first];
        q[first] = null;                            // to avoid loitering
        N--;
        first++;
        if (first == q.length) {
            first = 0;           // wrap-around
        }        // shrink size of array if necessary
        if (N > 0 && N == q.length / 4) {
            resize(q.length / 2);
        }
        return item;
    }

    public Identification sample() // return (but do not delete) a
    // random item
    {
        int choosen = RAND.nextInt(N);
        int index = (first + choosen) % q.length;
        return q[index];
    }

    public HashSet<Identification> sample(int sampleSize) // return (but do not delete) a
    // random item
    {
        HashSet<Identification> sample = new HashSet<>();
        for (int i = 0; i < sampleSize; i++) {
            Identification sampledIdent = dequeue();
            sample.add(sampledIdent);
            if (isEmpty()) {
                break;
            }
        }
        return sample;
    }

    @Override
    public Iterator<Identification> iterator() {
        return new RandomQueueIterator();
    }

    public void clear() {
        q = new Identification[4];
    }

    // an iterator, doesn't implement remove() since it's optional
    private class RandomQueueIterator implements Iterator<Identification> {

        private int i;

        @Override
        public boolean hasNext() {
            return i < N;
        }

        @Override
        public Identification next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Identification item = q[(i + first) % q.length];
            i++;
            return item;
        }
    }

}
