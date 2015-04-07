package com.hyperiongray.pull;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for pulling data from web sites.
 * @author mark
 */
public abstract class Pull {
    private String url;
    private String username;
    private String password;
    private Map <String, String> headers = new HashMap<> ();
    private String postBody;

    /**
     *
     * @return response from the GET pull
     */
    public abstract String getResponse();

    /**
     *
     * @return response from the GET pull, bytes
     */
    public abstract byte[] getResponseBytes();

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
    public void addHeader(String name, String value) {
        getHeaders().put(name, value);
    }

    /**
     * @return the headers
     */
    public Map <String, String> getHeaders() {
        return headers;
    }

    /**
     * @return the postBody
     */
    public String getPostBody() {
        return postBody;
    }

    /**
     * @param postBody the postBody to set
     */
    public void setPostBody(String postBody) {
        this.postBody = postBody;
    }
}
