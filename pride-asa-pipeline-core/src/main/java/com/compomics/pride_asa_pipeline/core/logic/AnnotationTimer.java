/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.logic;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class AnnotationTimer extends Thread implements Runnable {

    long totalSleepTime;
    long interval = 1000;
    long timeout = 30 * 60 * 1000;
    boolean cancel = false;
    boolean timedOut = false;
    private final Thread annotationThread;

    public AnnotationTimer(Thread annotationThread) {
        this.annotationThread = annotationThread;
    }

    @Override
    public void start() {
        cancel = false;
        super.start();
        annotationThread.start();
    }

    @Override
    public void run() {
        while (!cancel) {
            totalSleepTime += interval;
            if (totalSleepTime > timeout) {
                cancel = true;
                timedOut = true;
                break;
            } else {
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException ex) {
                    //ignore for now
                }
            }
        }
        if (annotationThread.isAlive() && !annotationThread.isInterrupted()) {
            annotationThread.interrupt();
        }
    }

    public boolean hasTimedOut() {
        return timedOut;
    }

    public void reset() {
        cancel = false;
        timedOut = false;
        totalSleepTime = 0;
    }

}
