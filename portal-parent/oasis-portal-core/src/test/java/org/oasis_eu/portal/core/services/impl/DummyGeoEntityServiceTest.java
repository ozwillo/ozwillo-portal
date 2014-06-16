package org.oasis_eu.portal.core.services.impl;

import org.junit.Test;
import org.oasis_eu.portal.core.model.appstore.GeoEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class DummyGeoEntityServiceTest {

    @Test
    public void testGetAllSuperEntities() throws Exception {
        DummyGeoEntityService service = new DummyGeoEntityService();
        ReflectionTestUtils.invokeMethod(service, "initialize"); // normally done by the IoC container

        Set<GeoEntity> sup = new HashSet<>();
        GeoEntity valence = service.find("728dfa79-a399-495b-9265-ed949b82ea8c");
        sup.add(valence); // valence
        sup.add(service.find("221554cc-56da-4d12-8eaa-0c36d204029c")); // agglo
        sup.add(service.find("2a1af3b2-288f-4c85-8cb4-d3d26778edef")); // drome

        assertNotNull(valence);


        assertEquals(sup, service.getAllSuperEntities(valence));
    }
}