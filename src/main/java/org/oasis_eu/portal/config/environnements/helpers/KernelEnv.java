package org.oasis_eu.portal.config.environnements.helpers;

public class KernelEnv {

    private String client_id;
    private String client_secret;
    private String callback_uri;
    private String home_uri;

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getClient_secret() {
        return client_secret;
    }

    public void setClient_secret(String client_secret) {
        this.client_secret = client_secret;
    }

    public String getCallback_uri() {
        return callback_uri;
    }

    public void setCallback_uri(String callback_uri) {
        this.callback_uri = callback_uri;
    }

    public String getHome_uri() {
        return home_uri;
    }

    public void setHome_uri(String home_uri) {
        this.home_uri = home_uri;
    }
}
