package com.gpogulsky.common.toolbox.meters;

import java.util.concurrent.atomic.LongAdder;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.junit.Test;

public class TestRateMeterMulti {

	@Test
    public void test() {

        final int nt = 8;
        final int limit = 4000;

        final CountDownLatch latch = new CountDownLatch(nt);
        final LongAdder adder = new LongAdder();

        final CircularRateMeter crm = new CircularRateMeter(4000);
        final Random random = new Random();

        System.out.println("Start");

        for (int i=0;i<nt;i++) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    int i = 0;
                    while (i++ < limit) {
                    	boolean b = random.nextBoolean();
                    	if (b) adder.increment();
                        crm.record(b);
                    }

                    System.out.println(Thread.currentThread().getName() + " done");
                    latch.countDown();
                }
            });

            System.out.println("start " + i);
            t.start();
        }


        try {
            latch.await();
        }
        catch (InterruptedException e) {
        }
        
        checkMeter(crm);

        System.out.println("Added: " + adder.longValue());
//        System.out.println("Generated: " + success + " reported: " + srm.getSuccess());
        
        Assert.assertEquals(crm.getCount(), crm.getSuccess());
    }

    private static boolean checkMeter(CircularRateMeter crm) {
        int c = crm.getCount();
        int s = crm.getSuccess();
        System.out.println("Meter stats: c = " + c + " s = " + s + " " + crm);
        return c == s;
    }

	
}
