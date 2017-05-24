package org.oasis_eu.portal.front;

import org.oasis_eu.portal.model.OasisLocales;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/locale")
public class LocaleController {

    @RequestMapping(value = "")
    public List<String> languages() {
        return OasisLocales.locales().stream().map(Locale::getLanguage).collect(Collectors.toList());
    }
}
