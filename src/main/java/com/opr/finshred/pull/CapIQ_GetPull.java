package com.opr.finshred.pull;

/**
 *
 * @author mark
 */
public class CapIQ_GetPull extends GetPull {

    @Override
    public String getResponse() {
        setHeaders();
        return super.getResponse();
    }

    private void setHeaders() {
        setUrl("https://www.capitaliq.com/CIQDotNet/Transcripts/Detail.aspx?keyDevId=254173774");
        addHeader("Host", "www.capitaliq.com");
        addHeader("Connection", "keep-alive");
        addHeader("Accept", "text/html, application/xhtml+xml, */*");
        addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko");
        addHeader("Accept-Language", "en-US");
        addHeader("Accept-Encoding", "gzip, deflate");;
        addHeader("Cookie", "__utma=21616681.538195146.1391629607.1391750736.1391753886.6; __utmb=21616681; __utmz=21616681.1391629607.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); machineIdCookie=407133472; uoid=254233246; __CG=u%3A650072285642920000%2Cs%3A695197188%2Ct%3A1391754063183%2Cc%3A5%2Ck%3Awww.capitaliq.com/59/78/1557%2Cf%3A1%2Ci%3A1; __utmc=21616681; ASP.NET_SessionId=yutzcbebn50ezptpucwffjn1; ObSSOCookie=YZv%2fBJjP0M%2b581QihoYsRr%2fmBRJN8yrlSWEdUK%2f8yZBPTiqnlnYU4%2byZgl3jrKj%2fRaUn09Yp7pvBFE5O04lyIsf9GsZPrjiXG4iD3KrPMh4yKPG%2fvZkYORU1Gvl3Dagkpb2dAZA1Z0di%2fCMH5MNbndDRq8UXNPpJHRJpmI1oTyugrIt%2bIg2IJ%2fsjbJnWuaBSUa6ynEGFPHu6khpQG6BVHKz%2f5pf7quhRmZbRHlx4pPfbhH%2b1uk6hnZ6Y6x3BRUB2NOKfpNQCnR1VsjfUv2UyxrLtfc89iTxB%2bghoPWsYJrEG0tdk%2b%2fGnKNcfKdSTHWKck323prCAvsAX10aO7lsM5Q%3d%3d; userLoggedIn=yutzcbebn50ezptpucwffjn1|2/7/2014 1:39:18 AM|561136; BIGipServercapitaliq-ssl=2SRXRM/aKhx/D9vsIHIR335bxlsf6ZlnonkzykvxELA6G8P1wojrfKV4293/dY1Dk6JXe0Ny5yRHJ24=; ASP.NET_SessionId=; ASPSESSIONIDQCTRRTDC=OGABNHEDGAJKPJJGGLECJBDA; fileDownloaded=true");
    }
}

