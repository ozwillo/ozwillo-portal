package org.oasis_eu.portal.config.environnements.helpers;

public class DatacoreEnv {

    private String nonce;

    private String adminUserRefreshToken;

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getAdminUserRefreshToken() {
        return adminUserRefreshToken;
    }

    public void setAdminUserRefreshToken(String adminUserRefreshToken) {
        this.adminUserRefreshToken = adminUserRefreshToken;
    }
}
