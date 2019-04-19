package com.nikondsl.daikin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class Parallel2Test {
    @BeforeAll
    public static void beforeAll() {
        System.err.println("=====================2.0=====================");
    }

    @AfterAll
    public static void afterAll() {
        System.err.println("---------------------2.0---------------------");
    }

    @Test
    public void testClass1Method1() throws InterruptedException {
        System.err.println("=====================2.1=====================");
        Thread.sleep(2_000);
        System.err.println("---------------------2.1---------------------");
    }
    @Test
    public void testClass1Method2() throws InterruptedException {
        System.err.println("=====================2.2=====================");
        Thread.sleep(2_000);
        System.err.println("---------------------2.2---------------------");
    }
    @Test
    public void testClass1Method3() throws InterruptedException {
        System.err.println("=====================2.3=====================");
        Thread.sleep(2_000);
        System.err.println("---------------------2.3---------------------");
    }
}
