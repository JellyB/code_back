package com.huatu.ztk.search;

import org.junit.Test;

import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

/**
 * @author zhengyi
 * @date 2019-03-01 11:37
 **/
public class HystrixTest {
    @Test
    public void testSynchronous() {
        assertEquals("Hello World!", new CommandHelloWorld("World").execute());
        assertEquals("Hello Bob!", new CommandHelloWorld("Bob").execute());
    }

    @Test
    public void testAsynchronous2() throws Exception {

        Future<String> fWorld = new CommandHelloWorld("World").queue();
        Future<String> fBob = new CommandHelloWorld("Bob").queue();

        assertEquals("Hello World!", fWorld.get());
        assertEquals("Hello Bob!", fBob.get());
    }
}