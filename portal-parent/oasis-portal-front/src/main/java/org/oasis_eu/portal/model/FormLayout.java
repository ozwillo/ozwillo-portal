package org.oasis_eu.portal.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 
 * @author mkalamalami
 *
 */
public class FormLayout implements Comparable<FormLayout>, Serializable {

	private static final long serialVersionUID = 2282236324466594448L;
	private String label;
	
	private FormLayoutMode mode = FormLayoutMode.VIEW;
	
	private Map<String, FormWidget> widgets = new HashMap<>();

	private final String id;
	
	private final String actionUrl;

	private final String formClass;
	
	private int order = 999;

	private int lastWidgetOrder = 0;
	
	public FormLayout(String id, String label) {
		this.id = id;
		this.label = label;
		this.actionUrl = null;
		this.formClass = null;
	}
	
	public FormLayout(String id, String label, String actionUrl, String formClass) {
		this.id = id;
		this.label = label;
		this.actionUrl = actionUrl;
		this.formClass = formClass;
	}

	public String getId() {
		return id;
	}
	
	public String getLabel() {
		return label;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public FormLayoutMode getMode() {
		return mode;
	}
	
	public void setMode(FormLayoutMode mode) {
		this.mode = mode;
	}
	
	public String getActionUrl() {
		return actionUrl;
	}

	public String getFormClass() {
		return formClass;
	}
	
	public void appendWidget(FormWidget widget) {
		if (widget.getOrder() == FormWidget.DEFAULT_ORDER) {
			widget.setOrder(lastWidgetOrder++);
		}
		widgets.put(widget.getId(), widget);
	}
	
	public List<FormWidget> getWidgets() {
		return widgets.values().stream().sorted().collect(Collectors.toList());
	}

	public FormWidget getWidget(String widgetId) {
		return widgets.get(widgetId);
	}

	@Override
	public int compareTo(FormLayout o) {
		return getOrder() - o.getOrder();
	}
}
