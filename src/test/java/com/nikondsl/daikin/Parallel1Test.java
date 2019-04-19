package com.nikondsl.daikin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public class Parallel1Test {
    @BeforeAll
    public static void beforeAll() {
        System.err.println("=====================1.0=====================");
    }

    @AfterAll
    public static void afterAll() {
        System.err.println("---------------------1.0---------------------");
    }

    @Test
    public void testClass1Method1() throws InterruptedException {
        System.err.println("=====================1.1=====================");
        Thread.sleep(2_000);
        System.err.println("---------------------1.1---------------------");
    }
    @Test
    public void testClass1Method2() throws InterruptedException {
        System.err.println("=====================1.2=====================");
        Thread.sleep(2_000);
        System.err.println("---------------------1.2---------------------");
    }
    @Test
    public void testClass1Method3() throws InterruptedException {
        System.err.println("=====================1.3=====================");
        Thread.sleep(2_000);
        System.err.println("---------------------1.3---------------------");
    }
}
