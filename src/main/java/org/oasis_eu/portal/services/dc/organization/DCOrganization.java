package org.oasis_eu.portal.services.dc.organization;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;


public class DCOrganization {

    @Id
    private String id;
    @JsonProperty
    private String lang;
    @JsonProperty
    private String legal_name;
    @JsonProperty
    private String sector_type;
    @JsonProperty
    private boolean in_activity = true;
    @JsonProperty
    private String alt_name;
    @JsonProperty
    private String org_type;
    @JsonProperty
    private String tax_reg_num;
    @JsonProperty
    private String tax_reg_official_id;
    @JsonProperty
    private String tax_reg_activity;
    @JsonProperty
    private String tax_reg_activity_uri;
    @JsonProperty
    private String jurisdiction;
    @JsonProperty
    private String jurisdiction_uri;
    @JsonProperty
    private String phone_number;
    @JsonProperty
    private String web_site;
    @JsonProperty
    private String email;
    @JsonProperty
    private String street_and_number;
    @JsonProperty
    private String po_box;
    @JsonProperty
    private String city;
    @JsonProperty
    private String city_uri;
    @JsonProperty
    private String zip;
    @JsonProperty
    private String cedex;
    @JsonProperty
    private String country;
    @JsonProperty
    private String country_uri;
    //@JsonProperty
    private String longitude;
    //@JsonProperty
    private String latitude;

    @JsonProperty
    private String contact_name;
    @JsonProperty
    private String contact_lastName;
    @JsonProperty
    private String contact_email;

    @JsonProperty
    private String iconUrl;

    @JsonProperty
    private boolean exist = false;

    /*
    * defaultValue property that may be used to document expected default value for the property.
    * https://fasterxml.github.io/jackson-annotations/javadoc/2.6/com/fasterxml/jackson/annotation/JsonProperty.html#defaultValue()
    */
    @JsonProperty(defaultValue = "0")
    @NotNull
    @NotEmpty
    private String version;


    public DCOrganization() {
        // defaultValue
        this.version = "0";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getLegal_name() {
        return legal_name;
    }

    public void setLegal_name(String legal_name) {
        this.legal_name = legal_name;
    }

    public String getSector_type() {
        return sector_type;
    }

    public void setSector_type(String sector_type) {
        this.sector_type = sector_type;
    }

    public boolean isIn_activity() {
        return in_activity;
    }

    public void setIn_activity(boolean in_activity) {
        this.in_activity = in_activity;
    }

    public String getAlt_name() {
        return alt_name;
    }

    public void setAlt_name(String alt_name) {
        this.alt_name = alt_name;
    }

    public String getOrg_type() {
        return org_type;
    }

    public void setOrg_type(String org_type) {
        this.org_type = org_type;
    }

    public String getTax_reg_num() {
        return tax_reg_num;
    }

    public void setTax_reg_num(String tax_reg_num) {
        this.tax_reg_num = tax_reg_num;
    }

    public String getTax_reg_official_id() {
        return tax_reg_official_id;
    }

    public void setTax_reg_official_id(String tax_reg_official_id) {
        this.tax_reg_official_id = tax_reg_official_id;
    }

    public String getTax_reg_activity() {
        return tax_reg_activity;
    }

    public void setTax_reg_activity(String tax_reg_activity) {
        this.tax_reg_activity = tax_reg_activity;
    }

    public String getTax_reg_activity_uri() {
        return tax_reg_activity_uri;
    }

    public void setTax_reg_activity_uri(String tax_reg_activity_uri) {
        this.tax_reg_activity_uri = tax_reg_activity_uri;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public String getJurisdiction_uri() {
        return jurisdiction_uri;
    }

    public void setJurisdiction_uri(String jurisdiction_uri) {
        this.jurisdiction_uri = jurisdiction_uri;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getWeb_site() {
        return web_site;
    }

    public void setWeb_site(String web_site) {
        this.web_site = web_site;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStreet_and_number() {
        return street_and_number;
    }

    public void setStreet_and_number(String street_and_number) {
        this.street_and_number = street_and_number;
    }

    public String getPo_box() {
        return po_box;
    }

    public void setPo_box(String po_box) {
        this.po_box = po_box;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCity_uri() {
        return city_uri;
    }

    public void setCity_uri(String city_uri) {
        this.city_uri = city_uri;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCedex() {
        return cedex;
    }

    public void setCedex(String cedex) {
        this.cedex = cedex;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountry_uri() {
        return country_uri;
    }

    public void setCountry_uri(String country_uri) {
        this.country_uri = country_uri;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getContact_name() {
        return contact_name;
    }

    public void setContact_name(String contact_name) {
        this.contact_name = contact_name;
    }

    public String getContact_lastName() {
        return contact_lastName;
    }

    public void setContact_lastName(String contact_lastName) {
        this.contact_lastName = contact_lastName;
    }

    public String getContact_email() {
        return contact_email;
    }

    public void setContact_email(String contact_email) {
        this.contact_email = contact_email;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public boolean isExist() {
        return exist;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }


}
