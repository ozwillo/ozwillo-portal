package org.oasis_eu.portal.model;

import java.io.Serializable;

public class FormWidgetAutocomplete extends FormWidget implements Serializable {
    private static final long serialVersionUID = -8526993287004041816L;

    public FormWidgetAutocomplete(String id, String label) {
        super(id, label);
    }

    @Override
    public String getType() {
        return "autocomplete";
    }
}
