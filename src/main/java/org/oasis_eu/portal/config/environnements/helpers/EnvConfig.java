package org.oasis_eu.portal.config.environnements.helpers;

public class EnvConfig {

    private String url;

    private String error_401;

    private WebEnv web;

    private KernelEnv kernel;

    private OpenDataEnv opendata;


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getError_401() {
        return error_401;
    }

    public void setError_401(String error_401) {
        this.error_401 = error_401;
    }

    public WebEnv getWeb() {
        return web;
    }

    public void setWeb(WebEnv web) {
        this.web = web;
    }

    public KernelEnv getKernel() {
        return kernel;
    }

    public void setKernel(KernelEnv kernel) {
        this.kernel = kernel;
    }

    public OpenDataEnv getOpendata() {
        return opendata;
    }

    public void setOpendata(OpenDataEnv opendata) {
        this.opendata = opendata;
    }
}


