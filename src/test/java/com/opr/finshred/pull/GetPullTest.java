package com.opr.finshred.pull;

import com.google.common.io.Files;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * This test assumes the present of a web server running on localhost:8080. In fact, it is
 * geared toward the default application set coming with the Tomcat 7.0 install.
 *
 * @author mark
 */
public class GetPullTest extends TestCase {

    /**
     * Test of getResponse method, of class GetPull.
     */
    public void testGetResponseWithPassword() {
        System.out.println("testGetResponseWithPassword");
        GetPull instance = new GetPull();
        instance.setUsername("tomcat");
        instance.setPassword("opr");
        instance.setUrl("http://localhost:8080/manager/status");
        String expResult = "";
        String result = instance.getResponse();
    }

    /**
     * Test of getResponse method, using Google
     */
    public void testGetResponse() throws IOException {
        System.out.println("testGetResponse");
        GetPull instance = new GetPull();
        instance.setUrl("http://google.com");
//GET / HTTP/1.1
//Accept: text/html, application/xhtml+xml, */*
//Accept-Language: en-US
//User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko
//Accept-Encoding: gzip, deflate
//Host: shmsoft.com
//If-Modified-Since: Tue, 01 Oct 2013 13:22:18 GMT
//If-None-Match: "1f55a1d-c0c-4e7add784b680"
//DNT: 1
//Proxy-Connection: Keep-Alive
//Pragma: no-cache
        instance.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko");
        instance.addHeader("Accept", "text/html, application/xhtml+xml, */*");
        String result = instance.getResponse();
        Files.write(result, new File("output/out.html"), Charset.defaultCharset());
        assertTrue(!result.isEmpty());
    }

}


