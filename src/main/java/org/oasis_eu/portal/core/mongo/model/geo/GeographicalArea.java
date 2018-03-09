package org.oasis_eu.portal.core.mongo.model.geo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document(collection = "geographical_area")
@CompoundIndexes({
    @CompoundIndex(name = "lang_nametokens_country", def = "{'lang':1, 'nameTokens':1, country:1}"),
    @CompoundIndex(name = "lang_modelType", def = "{'lang':1, 'modelType':1}"),  // to get list of countries
    @CompoundIndex(name = "status", def = "{'status':1}"), //used on replication cron job
    @CompoundIndex(name = "replicationTime", def = "{'replicationTime':1}") //used on replication cron job
})
public class GeographicalArea {

    @Id
    private String id;

    /**
     * language used for the name
     */
    private String lang;

    /**
     * displayed ; in current locale
     */
    @JsonProperty
    private String name;

    @JsonProperty
    private String postalCode;

    /**
     * to constraint search within this mixin or model type
     * TODO fill, index, query criteria
     */
    private List<String> modelType;

    // LATER OPT to search within one type on several fields, either add their values to tokenization
    // (provides a single autocompletion form field), or (to have one autocompletion form field
    // per Datacore field) configure one separate fulltext index i.e. one mongo collection per
    // Datacore field to look up in.

    /**
     * to help the user discriminate, built using names of NUTS3 or else 2 parent with country
     *
     * @obsolete using name instead
     */
    @Field("detailed_name")
    @JsonProperty
    private String detailedName;

    @JsonProperty
    private List<String> ancestors;

    private List<String> nameTokens;

    @JsonProperty
    private String country;


    /**
     * URI in Datacore (required if ex. sending directly to store ajax)
     */
    @JsonProperty
    private String uri;

    @JsonIgnore
    @Indexed
    private GeographicalAreaReplicationStatus status = GeographicalAreaReplicationStatus.INCOMING;


    /*
        https://spring.io/blog/2015/03/26/what-s-new-in-spring-data-fowler

        This setup will make sure that both your application package and the Spring Data JPA one
        for the JSR-310 converters will be scanned and handed to the persistence provider.
        Find a complete example for that in our Spring Data Examples repository.
        Note, that due to the fact that the converter simply converts the JSR-310 types to legacy Date instances,
        only non-time-zoned (e.g. LocalDateTime etc.) are supported.
    */

    @JsonIgnore
    @Indexed // used by result sort
    private Instant replicationTime = Instant.now();

    public GeographicalArea() { }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public List<String> getModelType() {
        return modelType;
    }

    public void setModelType(List<String> modelType) {
        this.modelType = modelType;
    }

    public String getDetailedName() {
        return detailedName;
    }

    public void setDetailedName(String detailedName) {
        this.detailedName = detailedName;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
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

    public GeographicalAreaReplicationStatus getStatus() {
        return status;
    }

    public void setStatus(GeographicalAreaReplicationStatus status) {
        this.status = status;
    }

    public Instant getReplicationTime() {
        return replicationTime;
    }

    public void setReplicationTime(Instant replicationTime) {
        this.replicationTime = replicationTime;
    }

    public List<String> getNameTokens() {
        return nameTokens;
    }

    public void setNameTokens(List<String> nameTokens) {
        this.nameTokens = nameTokens;
    }

    public List<String> getAncestors() {
        return ancestors;
    }

    public void setAncestors(List<String> ancestors) {
        this.ancestors = ancestors;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

}
