package org.oasis_eu.portal.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 
 * @author mkalamalami
 *
 */
public class FormLayout implements Comparable<FormLayout> {

	private String label;
	
	private FormLayoutMode mode = FormLayoutMode.VIEW;
	
	private Map<String, FormWidgetText> widgets = new HashMap<String, FormWidgetText>();

	private final String id;
	
	private int order = 999;

	private int lastWidgetOrder = 0;
	
	public FormLayout(String id, String label) {
		this.id = id;
		this.label = label;
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
	
	public void appendWidget(FormWidgetText widget) {
		if (widget.getOrder() == FormWidget.DEFAULT_ORDER) {
			widget.setOrder(lastWidgetOrder++);
		}
		widgets.put(widget.getId(), widget);
	}
	
	public List<FormWidgetText> getWidgets() {
		return widgets.values().stream().sorted().collect(Collectors.toList());
	}

	public FormWidgetText getWidget(String widgetId) {
		return widgets.get(widgetId);
	}

	@Override
	public int compareTo(FormLayout o) {
		return getOrder() - o.getOrder();
	}
}
