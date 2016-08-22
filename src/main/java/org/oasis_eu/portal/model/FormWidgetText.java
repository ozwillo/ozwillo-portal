package org.oasis_eu.portal.model;


import java.io.Serializable;

public class FormWidgetText extends FormWidget implements Serializable {


    private static final long serialVersionUID = 1355368723908843961L;

    public FormWidgetText(String id, String label) {
        super(id, label);
    }

    public FormWidgetText(String id, String label, boolean readOnly) {
        super(id, label, null, readOnly);
    }

    @Override
    public String getType() {
        return "text";
    }
}
