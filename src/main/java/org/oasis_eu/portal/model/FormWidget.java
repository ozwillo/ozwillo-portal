package org.oasis_eu.portal.model;

public abstract class FormWidget implements Comparable<FormWidget> {

    public static final int DEFAULT_ORDER = 999;

    private final String id;

    private String value;

    private String label;

    private boolean readOnly;

    private int order = DEFAULT_ORDER;

    public FormWidget(String id, String label) {
        this(id, label, null);
    }

    public FormWidget(String id, String label, String value) {
        this(id, label, value, false);
    }

    public FormWidget(String id, String label, String value, boolean readOnly) {
        this.id = id;
        this.label = label;
        this.value = value;
        this.readOnly = readOnly;
    }

    public abstract String getType();

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public int compareTo(FormWidget o) {
        return getOrder() - o.getOrder();
    }
}
