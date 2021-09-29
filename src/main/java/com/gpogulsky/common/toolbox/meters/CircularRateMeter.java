package com.gpogulsky.common.toolbox.meters;

import java.util.concurrent.atomic.AtomicInteger;

public class CircularRateMeter {

    private final static int smask = 0b11111;
    private final static int[] smap = new int[32];
    static {
        smap[0] = 1;
        for (int i = 1; i < 32; i++) {
            smap[i] = smap[i-1] << 1;
        }
    }

    private final int size;
    private final int depth;
    private final double depthDiv;
    private final int[] stor;

    private final AtomicInteger nextAvailable = new AtomicInteger(0);
    private final AtomicInteger numSuccess = new AtomicInteger(0);


    public CircularRateMeter(int depth) {
        this.size = depth / 32 + (depth % 32 > 0 ? 1 : 0); // round up to the nearest int
        this.depth = this.size * 32;
        this.depthDiv = (double)this.depth;
        this.stor = new int[this.size];
    }

    public double record(boolean flag) {

        int pos = this.nextAvailable.getAndIncrement();
        //System.out.println(Thread.currentThread().getName() + " pos: " + pos);

        if (pos >= this.depth) {
            int expect = pos + 1;
            while (pos >= this.depth) {
                if (this.nextAvailable.compareAndSet(expect, 0)) {
                    System.out.println("Swap success " + Thread.currentThread().getName() + " pos: " + pos + " expect: " + expect + " next: " + nextAvailable.get());
                }
                else {
                    System.out.println("Swap fail " + Thread.currentThread().getName() + " pos: " + pos + " expect: " + expect + " next: " + nextAvailable.get());
                }

                pos = this.nextAvailable.getAndIncrement();
                expect = pos + 1;

                System.out.println(Thread.currentThread().getName() + " pos: " + pos + " expect: " + expect + " next: " + nextAvailable.get());
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
        int slot = pos / 32;
        int shift = pos & smask;
        if ((stor[slot] & smap[shift]) == 0 == flag) {
            if (flag) {
                this.numSuccess.incrementAndGet();
                this.stor[slot] |= smap[shift];
            }
            else {
                this.numSuccess.decrementAndGet();
                this.stor[slot] ^= smap[shift];
            }
        }
    }

    private boolean getStor(int pos) {
        int slot = pos / 32;
        int shift = pos & smask;
        return (this.stor[slot] & smap[shift]) > 0;
    }

    public int getCount() {
        int sum = 0;
        for(int i = 0; i< this.size; i++) {
            sum += this.countBits(this.stor[i]);
        }

        return sum;
    }

    private int countBits(int a) {
        int count = 0;
        while (a != 0)
        {
            count++;
            a = a & (a - 1);    // clear the least significant bit set
        }

        return count;
    }


    @Override
    public String toString() {
        return "CircularRateMeter{nextAvailable=" + this.nextAvailable.get() +
                ", numSuccess=" + this.numSuccess.get() +
                '}';
    }
}
