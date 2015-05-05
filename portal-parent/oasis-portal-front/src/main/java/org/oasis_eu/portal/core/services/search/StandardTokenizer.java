package org.oasis_eu.portal.core.services.search;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 5/5/15
 */
@Service
public class StandardTokenizer implements Tokenizer {

    @Override
    public List<String> tokenize(String input) {
        if (input == null || "".equals(input)) {
            return Collections.emptyList();
        }

        return Arrays.asList(input.split("[\\p{P}\\s]+"))
                .stream()
                .map(s -> s.toLowerCase())
                .map(s -> Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{InCOMBINING_DIACRITICAL_MARKS}+", ""))
                .collect(Collectors.toList());

    }
}
