package com.gpogulsky.common.toolbox.meters;

import java.util.Random;

import org.junit.Test;

import org.junit.Assert;

public class TestRateMeter {
	
	@Test
    public void testSingleThread() {

        int limit = 2000;

        CircularRateMeter crm = new CircularRateMeter(1024);
        Random random = new Random();

        System.out.println("Start");

        int i = 0;
        boolean flag = false;
        int presuccess = 0;
        for (; i<limit - 1024 && i<1024; i++) {
            flag = random.nextBoolean();
            if (flag) presuccess++;
            crm.record(flag);
            if (!checkMeter(crm, i, flag,false)) {
                return;
            }
        }

        checkMeter(crm, i, flag, true);

        System.out.println("Compare");

        int success = 0;
        for (; i<limit && i<1024; i++) {
            flag = random.nextBoolean();
            if (flag) success++;
            crm.record(flag);
            if (!checkMeter(crm, i, flag,false)) {
                return;
            }
        }

        checkMeter(crm, i, flag, true);

        System.out.println("Generated: " + (success + presuccess) + " reported: " + crm.getSuccess());

        for (; i<limit; i++) {
            flag = random.nextBoolean();
            if (flag) success++;
            crm.record(flag);
            //System.out.println("Generated: " + success + " reported: " + srm.getSuccess());
            if (!checkMeter(crm, i, flag,false)) {
                return;
            }
        }

        checkMeter(crm, i, flag, true);

        System.out.println("Generated: " + success + " reported: " + crm.getSuccess());
        
        Assert.assertEquals(success, crm.getSuccess());
    }

    private static boolean checkMeter(CircularRateMeter crm, int i, boolean flag, boolean alwaysPrint) {
        int c = crm.getCount();
        int s = crm.getSuccess();
        if (c != s || alwaysPrint) System.out.println("Meter stats: i = " + i + " " + flag + " c = " + c + " s = " + s + " " + crm);
        return c == s;
    }

    
    
}
