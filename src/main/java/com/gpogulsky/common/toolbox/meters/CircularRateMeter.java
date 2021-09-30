package com.gpogulsky.common.toolbox.meters;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple non-locking rate meter.
 * The size should be picked according to the expected number of threads.
 * Too small buffer can wrap to fast and collide on stor updates.
 * 
 * @author gpogulsky
 *
 */
public class CircularRateMeter {

    private final int size;
    private final double depthDiv;
    private final boolean[] stor;

    private final AtomicInteger nextAvailable = new AtomicInteger(0);
    private final AtomicInteger numSuccess = new AtomicInteger(0);


    public CircularRateMeter(int size) {
        this.size = size;
        this.depthDiv = (double)this.size;
        this.stor = new boolean[this.size];
    }

    public double record(boolean flag) {

        int pos = this.nextAvailable.getAndIncrement();
        
        if (pos >= this.size) {
            int expect = pos + 1;
            while (pos >= this.size) {
                this.nextAvailable.compareAndSet(expect, 0);
                pos = this.nextAvailable.getAndIncrement();
                expect = pos + 1;
            }
        }

        this.setStor(pos, flag);
        
        return this.numSuccess.get() / this.depthDiv;
    }

    public double getRate() {
        return this.numSuccess.get() / this.depthDiv;
    }

    public int getSuccess() {
        return this.numSuccess.get();
    }

    private void setStor(int pos, boolean flag) {
    	
        if (stor[pos] != flag) {
            if (flag) {
                this.numSuccess.incrementAndGet();
            }
            else {
                this.numSuccess.decrementAndGet();
            }

            this.stor[pos] = flag;
        }        
    }

    public int getCount() {
        int sum = 0;
        for(int i = 0; i< this.size; i++) {
        	if (stor[i]) sum++;
        }

        return sum;
    }

    @Override
    public String toString() {
        return "CircularRateMeter{nextAvailable=" + this.nextAvailable.get() +
                ", numSuccess=" + this.numSuccess.get() +
                '}';
    }
}
