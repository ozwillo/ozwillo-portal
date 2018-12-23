package org.oasis_eu.portal.config.environnements.helpers;

public class EnvConfig {

    private String baseUrl;

    private WebEnv web;

    private KernelEnv kernel;

    private DatacoreEnv datacore;

    private OpenDataEnv opendata;

    private String baseImageUrl;

    private String defaultIconUrl;

    private boolean isDefaultConf;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
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

    public DatacoreEnv getDatacore() {
        return datacore;
    }

    public void setDatacore(DatacoreEnv datacore) {
        this.datacore = datacore;
    }

    public OpenDataEnv getOpendata() {
        return opendata;
    }

    public void setOpendata(OpenDataEnv opendata) {
        this.opendata = opendata;
    }

    public String getBaseImageUrl() {
        return baseImageUrl;
    }

    public void setBaseImageUrl(String baseImageUrl) {
        this.baseImageUrl = baseImageUrl;
    }

    public String getDefaultIconUrl() {
        return defaultIconUrl;
    }

    public void setDefaultIconUrl(String defaultIconUrl) {
        this.defaultIconUrl = defaultIconUrl;
    }

    public boolean getIsDefaultConf() {
        return isDefaultConf;
    }

    public void setIsDefaultConf(boolean defaultConf) {
        isDefaultConf = defaultConf;
    }
}


