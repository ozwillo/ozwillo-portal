package org.oasis_eu.portal.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.oasis_eu.portal.dao.dc.GeographicalAreaService;
import org.oasis_eu.portal.model.geo.GeographicalArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/geo")
public class GeoController {

    @Autowired
    private GeographicalAreaService geographicalAreaService;

    @GetMapping(value = "/areas")
    public GeographicalAreaResponse geographicalAreas(@RequestParam String country_uri, @RequestParam String q) {
        int areaLoadSize = 10;
        int areaDcLoadSize = areaLoadSize + 1;
        List<GeographicalArea> areas = geographicalAreaService.find(country_uri, null, q, 0, areaDcLoadSize);

        return new GeographicalAreaResponse(areas.stream()
                .limit(areaLoadSize).collect(Collectors.toList()), areas.size() == areaDcLoadSize);
    }

    @GetMapping(value = "/cities")
    public GeographicalAreaResponse dcCities(@RequestParam String country_uri, @RequestParam String q) {
        int loadSize = 10;
        List<GeographicalArea> cities = geographicalAreaService.findCities(q, country_uri, 0, loadSize + 1);

        return new GeographicalAreaResponse(
                cities.stream().limit(loadSize).collect(Collectors.toList()), (cities.size() == loadSize + 1));
    }

    @GetMapping(value = "/countries")
    public GeographicalAreaResponse dcCountries(@RequestParam String q) {
        int loadSize = 10;
        List<GeographicalArea> countries = geographicalAreaService.findCountries(q);

        return new GeographicalAreaResponse(
                countries.stream().limit(loadSize).collect(Collectors.toList()), (countries.size() == loadSize + 1));
    }

    public static class GeographicalAreaResponse {

        @JsonProperty("areas")
        private List<GeographicalArea> areas;
        @JsonProperty("maybeMore")
        private boolean maybeMore;

        GeographicalAreaResponse(List<GeographicalArea> areas, boolean maybeMore) {
            this.areas = areas;
            this.maybeMore = maybeMore;
        }

        public List<GeographicalArea> getAreas() {
            return areas;
        }

        public boolean isMaybeMore() {
            return maybeMore;
        }
    }
}
