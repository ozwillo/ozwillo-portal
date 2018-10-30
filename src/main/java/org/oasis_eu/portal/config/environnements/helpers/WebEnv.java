package org.oasis_eu.portal.config.environnements.helpers;

public class WebEnv {

    private String home;
    private SiteMapEnv sitemap;
    private String googleTag;

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public SiteMapEnv getSitemap() {
        return sitemap;
    }

    public void setSitemap(SiteMapEnv sitemap) {
        this.sitemap = sitemap;
    }

    public String getGoogleTag() {
        return googleTag;
    }

    public void setGoogleTag(String googleTag) {
        this.googleTag = googleTag;
    }
}
