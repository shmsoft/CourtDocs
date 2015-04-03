package com.opr.finshred.pull;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mark
 */
public class PostPullJPMTest {    

    /**
     * Test of getResponse method, of class PutPull.
     */
    @Test
    public void testGetResponse() {
        System.out.println("getResponse");
        PostPull instance = new PostPull();
        instance.setUrl("https://mm.jpmorgan.com/MorganMarkets?page=advanced_search");

        instance.addHeader("Host", "mm.jpmorgan.com");
        instance.addHeader("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:26.0) Gecko/20100101 Firefox/26.0");
        instance.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        instance.addHeader("Accept-Language", "en-US,en;q=0.5");
        instance.addHeader("Accept-Encoding", "gzip, deflate");
        instance.addHeader("Referer", "https://mm.jpmorgan.com/MorganMarkets?page=advanced_search");
        instance.addHeader("Cookie", "pajpm3=7acLANIQ59RDAQAAAAAAAAAAAADSEOfUQwEAAAAAQiUiOE5MuLOIuj0x542YLRi+N6c=");
        instance.addHeader("Connection", "keep-alive");
        instance.addHeader("Content-Type", "application/x-www-form-urlencoded");
        //instance.addHeader("Content-Length", "226");                
        String postBody = "pageNumber=1&sortOrder=RELEVANCE_DESC&export=null&hiddenText=&text=&texttype=any&daterange=6months&advSearchGo.x=16&advSearchGo.y=11&selectedAnalyst=-1&selectedCompany=-1&selectedSector=-1&selectedRegion=-1&startDate=&endDate=";
        instance.setPostBody(postBody);
        String expResult = "";
        String result = instance.getResponse();
        // without the right cookie, the result will not be valid, so let's only check that we got some result
        assertTrue(result.length() > 0);
    }
}