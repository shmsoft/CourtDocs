package com.opr.finshred.pull;

import com.opr.finshred.db.FinShredDB;

/**
 *
 * @author mark
 */
public class JPM_PostPull extends PostPull {

    @Override
    public String getResponse() {
        setJpmHeadersAndBody();
        return super.getResponse();
    }
    private void setJpmHeadersAndBody() {
        // These don't change - haha! they changed the next day
        setUrl("https://mm.jpmorgan.com/MorganMarkets?page=advanced_search");
        addHeader("Host", "mm.jpmorgan.com");
        //addHeader("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:26.0) Gecko/20100101 Firefox/26.0");
        addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko");        
        // addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        addHeader("Accept", "text/html, application/xhtml+xml, */*");        
        // addHeader("Accept-Language", "en-US,en;q=0.5");
        addHeader("Accept-Language", "en-US");        
        addHeader("Accept-Encoding", "gzip, deflate");
        addHeader("Referer", "https://mm.jpmorgan.com/MorganMarkets?page=advanced_search");
        addHeader("Connection", "keep-alive");
        addHeader("Content-Type", "application/x-www-form-urlencoded");  
        FinShredDB db = new FinShredDB();                
        addHeader("Cookie", db.getJpmCookie());        
        String postBody = "pageNumber=1&sortOrder=RELEVANCE_DESC&export=null&hiddenText=&text=&texttype=any&daterange=6months&advSearchGo.x=16&advSearchGo.y=11&selectedAnalyst=-1&selectedCompany=-1&selectedSector=-1&selectedRegion=-1&startDate=&endDate=";
        setPostBody(postBody);
    }
}
