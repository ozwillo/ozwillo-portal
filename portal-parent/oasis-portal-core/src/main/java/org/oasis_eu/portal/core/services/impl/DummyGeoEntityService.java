package org.oasis_eu.portal.core.services.impl;

import org.oasis_eu.portal.core.model.appstore.GeoEntity;
import org.oasis_eu.portal.core.services.GeoEntityService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 6/16/14
 */
@Service
public class DummyGeoEntityService implements GeoEntityService {

    private List<GeoEntity> entities = new ArrayList<>();


    @Override
    public GeoEntity find(String id) {
        return entities.stream().filter(e -> e.getId().equals(id)).findAny().orElse(null);
    }

    // Competing for the most un-classical-Java-ish idiom possible
    @Override
    public Set<GeoEntity> getAllSuperEntities(GeoEntity root) {
        if (root.getIsContainedIn().isEmpty()) return new HashSet<GeoEntity>() {{add(root);}};
        else {
            return new HashSet<GeoEntity>(){{
                add(root);
                addAll(root.getIsContainedIn().stream().flatMap(id -> getAllSuperEntities(find(id)).stream()).collect(Collectors.toSet()));
            }};
        }

    }

    @Override
    public Set<GeoEntity> getEntitiesByName(String name) {
        return entities.stream().filter(e -> e.getKeywords().contains(name)).collect(Collectors.toSet());
    }

    @Override
    public Set<String> getAllEntityNames() {
        return entities.stream().flatMap(e -> e.getKeywords().stream()).collect(Collectors.toSet());
    }


    @PostConstruct
    private void initialize() {
        GeoEntity drome = new GeoEntity();
        drome.setId("2a1af3b2-288f-4c85-8cb4-d3d26778edef");
        drome.addKeywords("drome", "26");

        GeoEntity aggloSudRhoneAlpes = new GeoEntity();
        aggloSudRhoneAlpes.setId("221554cc-56da-4d12-8eaa-0c36d204029c");
        aggloSudRhoneAlpes.addKeywords("agglo sud rhone alpes", "sud rha");
        aggloSudRhoneAlpes.addContainingEntities(drome);


        GeoEntity valence = new GeoEntity();
        valence.setId("728dfa79-a399-495b-9265-ed949b82ea8c");
        valence.addKeywords("valence", "26000");
        valence.addContainingEntities(aggloSudRhoneAlpes);

        GeoEntity vienne = new GeoEntity();
        vienne.setId("18f6cd0e-270a-4532-bafd-d4aa1727e0a4");
        vienne.addKeywords("vienne");

        GeoEntity lyon = new GeoEntity();
        lyon.setId("430557df-bbe5-484d-ad48-7f70117a7663");
        lyon.addKeywords("lyon", "69000", "69001", "69002", "69003", "69004", "69005", "69006","69007","69008","69009");

        entities.add(drome);
        entities.add(aggloSudRhoneAlpes);
        entities.add(valence);
        entities.add(vienne);
        entities.add(lyon);

    }
}
