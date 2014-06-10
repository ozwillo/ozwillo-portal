package org.oasis_eu.portal.core.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Draft version. Shall be expanded when it shall...
 *
 *
 * User: schambon
 * Date: 5/14/14
 */
public class GeoEntity extends GenericEntity {

    private Set<String> keywords = new HashSet<>();

    private Set<String> isContainedIn = new HashSet<>();

    public Set<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(Set<String> keywords) {
        this.keywords = keywords;
    }

    public Set<String> getIsContainedIn() {
        return isContainedIn;
    }

    public void setIsContainedIn(Set<String> isContainedIn) {
        this.isContainedIn = isContainedIn;
    }

    public void addKeywords(String... keywordsToAdd) {
        for (String s : keywordsToAdd) {
            keywords.add(s);
        }
    }

    public void addContainingEntities(String... containers) {
        for (String c : containers) {
            this.isContainedIn.add(c);
        }
    }

    public void addContainingEntities(GeoEntity... entities) {
        for (GeoEntity e:entities) {
            this.isContainedIn.add(e.getId());
        }
    }
}
