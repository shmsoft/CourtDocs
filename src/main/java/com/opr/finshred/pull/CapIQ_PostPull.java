package com.opr.finshred.pull;

import com.opr.finshred.db.FinShredDB;

/**
 *
 * @author mark
 */
public class CapIQ_PostPull extends PostPull {

    @Override
    public String getResponse() {
        setHeadersAndBody();
        return super.getResponse();
    }

    private void setHeadersAndBody() {
        // These don't change - haha! they changed the next day
        setUrl("https://www.capitaliq.com/CIQDotNet/Transcripts/Summary.aspx");
        addHeader("Host", "www.capitaliq.com");
        addHeader("Connection", "keep-alive");
        addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko");
        addHeader("Accept-Language", "en-US");
        addHeader("Accept-Encoding", "gzip, deflate");
        addHeader("Referer", "https://www.capitaliq.com/CIQDotNet/Transcripts/Summary.aspx");
        addHeader("Origin", "https://www.capitaliq.com");
        addHeader("Content-Type", "application/x-www-form-urlencoded");
        addHeader("Accept-Encoding", "gzip,deflate,sdch");
        addHeader("Cookie", "BIGipServercapitaliq-ssl=URKBZU4NtqlBAPHsIHIR335bxlsf6c6e7DJV1HcR40FgGJ7LUr2wlONq5UTNU8SpCIaE9LYJciAvsB0=; machineIdCookie=733483456; uoid=254233246; ASP.NET_SessionId=0qcpw4p4nlk0hlqfveepqt5b; ObSSOCookie=XRzdwy6kdOPOZ%2bScWX%2bVhyAOFlOI%2fXAvrslotch2xH%2f5btErfcKjtgbU98cTQQbDQy01zCTHD4SKFZ1Z%2bStJT0Qq7zeHuHAjWHE0V%2buuDE1J1pMXV2Q9%2feN2nQEcfTuPpXD7hfKboUNexAL3f1seBoNFNkY7DD5%2fFyXA9ytuT2fJ6%2fgs0TdX3P19kAQ2QLXz7Gen95%2fJ2mybcV%2bln2ssBFt9NE753tY%2f2hRsEQVqDrrmMXmGWLrOBhnblEgiVeXRku%2bPk%2bsVK4D%2bc7q%2fNQEcdRXr1PCloZKfO%2f5CY8f%2bwJAGja6Y6no%2biuLLj84Cd%2b9vBxz67bT3XLOIfo%2fao9A5tg%3d%3d; ASPSESSIONIDASRASRDA=PHKEPJDDJGKADOPDJHDHPKFK; userLoggedIn=0qcpw4p4nlk0hlqfveepqt5b%7C2%2F5%2F2014+6%3A47%3A00+PM%7C561136; __utma=21616681.1936216450.1391637176.1391655053.1391657718.4; __utmc=21616681; __utmz=21616681.1391637176.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); userLoggedIn=0qcpw4p4nlk0hlqfveepqt5b|2/5/2014 10:55:21 PM|561136; ASP.NET_SessionId=0qcpw4p4nlk0hlqfveepqt5b; __utmb=21616681; __CG=u%3A1144184278406398000%2Cs%3A649109678%2Ct%3A1391658264053%2Cc%3A4%2Ck%3Awww.capitaliq.com/60/60/781%2Cf%3A1%2Ci%3A1");
//        FinShredDB db = new FinShredDB();
//        addHeader("Cookie", db.getJpmCookie());
        String postBody = "stateKey=167b59b006b94c0cbe41a539b03950b4&myContentType=1&myDocumentType=0&myActTypeId=0&myNumberRows=-1&myStreamToBinder=false&myDefaultBinderAction=false&__EVENTTARGET=&__EVENTARGUMENT=&__VIEWSTATE=HHbXTEk701DUYPxe0CwbQ0AELgXywNtcTXN6cFZLmLAnQgCes6C6JHmrpbfKVM%2FrF&__EVENTVALIDATION=%2FwEWOgLFnPTxAwLGqe6cAQKnmobQBQLXsqSuCwKbp%2F3nAwL2iMtiAoSzpe4JAonLpf0OAsyBxfQGAsXigfYDAryYlMkFAuDB%2BY8FAp7D1NULAtGeo9cHAtq2ovAIAqnGzbkJAonC9roOAoWurPUJAoWuwNACAoWu1KsLAoWu6IYEAs%2BHlb0BAp3e0tkEAv3B0rwLApS8ir8EAsSigO4MAq%2F41MoKApCJk9MOApu4rbAHAvCvpcQCAvvev%2FkKAqCo0ZIBArCsqu0EAv%2BlmJ4HAv7W9c0MAt%2BW%2BNQMAt7HtdsFAqv96rgCAsqHjsECAouknasCAqquoIoGArucqfgEAsuggtMIAq6%2FkOsGApmV5e8EAo6w8JwMAvmFpf0NAtqW44ACAuXF%2Fd0KArq9lfMBApWYkksClZjuSwKVmPpLApWY9ksCnaS87AMCiYH3jgYC%2FKDimgwCsr2%2F%2FgFbDgmLjGrb4xXnkI3d4dOpTYHoLw%3D%3D&_criteria%24_searchSection%24_searchToggle%24state=True&_criteria%24_searchSection%24_searchToggle%24_criteria__searchSection__searchToggle__entitySearch_searchbox=&_criteria%24_searchSection%24_searchToggle%24ctl21%24Toggle%24state=True&_criteria%24_searchSection%24_searchToggle%24ctl21%24Toggle%24_criteria__searchSection__searchToggle__entitySearch_selList%24ctl02%24_criteria__searchSection__searchToggle__entitySearch_selList_sortHi=&_criteria%24_searchSection%24_searchToggle%24ctl21%24Toggle%24_criteria__searchSection__searchToggle__entitySearch_selList%24ctl02%24_criteria__searchSection__searchToggle__entitySearch_selList_deletedItems=&_criteria%24_searchSection%24_searchToggle%24ctl21%24Toggle%24_criteria__searchSection__searchToggle__entitySearch_selList%24ctl02%24_criteria__searchSection__searchToggle__entitySearch_selList_postBackOnReorderHi=&_criteria%24_searchSection%24_searchToggle%24ctl21%24Toggle%24_criteria__searchSection__searchToggle__entitySearch_selList%24ctl02%24_criteria__searchSection__searchToggle__entitySearch_selList_checkedItems=&_criteria%24_searchSection%24_searchToggle%24_dateRange%24dateRangeSelectedSection=5&_criteria%24_searchSection%24_searchToggle%24_dateRange%24myPeriodOffsetAmount=2&_criteria%24_searchSection%24_searchToggle%24_dateRange%24PeriodMenu=70&_criteria%24_searchSection%24_searchToggle%24_dateRange%24myFromBox=&_criteria%24_searchSection%24_searchToggle%24_dateRange%24myToBox=&_criteria%24_searchSection%24_searchToggle%24_keyword=&_criteria%24_searchSection%24_searchToggle%24_searchScope=1&_criteria%24_searchSection%24_searchToggle%24_btGo%24_saveBtn=Search&_transcriptsGrid%24_dataGrid%24__optionsColumn_huffmeister=&_transcriptsGrid%24_dataGrid%24__optionsColumn_hiddenRowValues=";
        setPostBody(postBody);
    }
}
