package org.oasis_eu.portal.controller;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.oasis_eu.portal.model.geo.GeographicalArea;
import org.oasis_eu.portal.services.SystemUserService;
import org.oasis_eu.portal.services.dc.GeographicalAreaService;
import org.oasis_eu.portal.model.dc.DCOrganization;
import org.oasis_eu.portal.services.OrganizationService;
import org.oasis_eu.portal.services.dc.DCOrganizationService;
import org.oasis_eu.spring.datacore.DatacoreClient;
import org.oasis_eu.spring.datacore.model.DCResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/organization/import")
class OrganizationImportController {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationImportController.class);

    @Autowired
    private DatacoreClient datacore;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private DCOrganizationService dcOrganizationService;

    @Autowired
    private GeographicalAreaService geographicalAreaService;

    @Autowired
    private SystemUserService systemUserService;

    @Value("${application.dcOrg.project: org_0}")
    private String dcOrgProjectName;

    @Value("${application.importPassword}")
    private String importPassword;

    @PostMapping
    public ResponseEntity<String> importOrganization(@RequestHeader String password, @RequestHeader String refreshToken,
                                                     @RequestParam("file") MultipartFile file) {
        StringBuilder log = new StringBuilder();

        log.append("Starting log for organisation import");
        if (!password.equals(importPassword)) {
            log.append("\nWrong password");
            return new ResponseEntity<>(log.toString(), HttpStatus.UNAUTHORIZED);
        }
        if (file == null) {
            log.append("\nNo file received");
            return new ResponseEntity<>(log.toString(), HttpStatus.PRECONDITION_FAILED);
        }
        try {
            CSVParser records = CSVFormat.EXCEL.withHeader().parse(new InputStreamReader(file.getInputStream()));
            int analysed = 0;
            int migrated = 0;
            for (CSVRecord record : records) {
                String name = record.get(0);
                String siren = record.get(1);
                String nic = record.get(2);
                String zip = record.get(3);
                String street = record.get(4);
                String cityCSV = record.get(5);

                StringBuilder city = new StringBuilder();
                StringBuilder city_uri = new StringBuilder();
                StringBuilder country = new StringBuilder();
                StringBuilder country_uri = new StringBuilder();
                systemUserService.runAs(() -> {
                    List<GeographicalArea> countries = geographicalAreaService.findCountries("France");
                    if (!countries.isEmpty()) {
                        country.append(countries.get(0).getName());
                        country_uri.append(countries.get(0).getUri());
                    }

                    if (StringUtils.hasLength(country_uri.toString())) {
                        List<GeographicalArea> cities = geographicalAreaService.findCities(cityCSV, country_uri.toString(), 0, 1);
                        if (!cities.isEmpty()) {
                            city.append(cities.get(0).getName());
                            city_uri.append(cities.get(0).getUri());
                        }
                    }
                });

                if (StringUtils.hasLength(name) && StringUtils.hasLength(siren) && StringUtils.hasLength(nic) &&
                    StringUtils.hasLength(cityCSV) && StringUtils.hasLength(city.toString()) &&
                    StringUtils.hasLength(city_uri.toString())) {
                    String siret = siren + String.join("", Collections.nCopies(5 - nic.length(), "0")) + nic;
                    DCOrganization dcOrganization = new DCOrganization();
                    dcOrganization.setId(dcOrganizationService.generateDcId(country_uri.toString(), siret));
                    dcOrganization.setLegal_name(name);
                    dcOrganization.setTax_reg_num(siret);
                    dcOrganization.setZip(zip);
                    dcOrganization.setStreet_and_number(street);
                    dcOrganization.setCity(city.toString());
                    dcOrganization.setCity_uri(city_uri.toString());
                    dcOrganization.setCountry(country.toString());
                    dcOrganization.setCountry_uri(country_uri.toString());
                    dcOrganization.setSector_type("PUBLIC_BODY");
                    dcOrganization.setJurisdiction(city.toString());
                    dcOrganization.setJurisdiction_uri(city_uri.toString());
                    systemUserService.runAs(() -> {
                        DCResource dcResource = datacore.getResourceFromURI(dcOrgProjectName.trim(), dcOrganization.getId()).getResource();
                        if (dcResource != null) dcOrganization.setVersion(String.valueOf(dcResource.getVersion()));
                        log.append("\nCreating / updating organization: ")
                            .append(dcOrganization.getLegal_name())
                            .append(" (").append(dcOrganization.getTax_reg_num()).append(")");
                        try {
                            organizationService.create(dcOrganization);
                        } catch (Exception e) {
                            log
                                .append("\nError while trying to create the organization ")
                                .append(dcOrganization.getLegal_name())
                                .append(": ").append(e);
                        }
                    });
                    migrated++;
                } else {
                    log.append("\nLocal organization not migrated due to uncomplete infos :");
                    log.append("\n---------------");
                    log.append("\nName : ").append(name);
                    log.append("\nSiren : ").append(siren);
                    log.append("\nNic : ").append(nic);
                    log.append("\nZip : ").append(zip);
                    log.append("\nStreet : ").append(street);
                    log.append("\nCityCSV : ").append(cityCSV);
                    log.append("\nCountry : ").append(country);
                    log.append("\nCountry_uri : ").append(country_uri);
                    log.append("\nCity : ").append(city);
                    log.append("\nCity_uri : ").append(city_uri);
                    log.append("\n---------------\n");
                }
                analysed++;
            }
            log.append("\n=> ").append(analysed).append(" local authorities analysed");
            log.append("\n=> ").append(migrated).append(" local authorities migrated");
            log.append("\nEnd of import");
        } catch (IOException e) {
            logger.error("Error while trying to read the CSV file");
            log.append("\nError while trying to read the CSV file");
            return new ResponseEntity<>(log.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return new ResponseEntity<>(log.toString(), HttpStatus.OK);
    }
}
