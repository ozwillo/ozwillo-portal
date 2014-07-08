package org.oasis_eu.portal.model;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;


public class FormWidgetDropdown extends FormWidget {

	private Map<String, String> options = new HashMap<String, String>();
	
	public FormWidgetDropdown(String id, String label) {
		super(id, label);
	}
	
	public String getType() {
		return "dropdown";
	}
	
	public FormWidgetDropdown addOption(String key, String value) {
		options.put(key, value);
		return this;
	}
	
	public List<Entry<String, String>> getOptions() {
		return options.entrySet().stream().sorted(new Comparator<Entry<String, String>>() {
			public int compare(Entry<String, String> o1, Entry<String, String> o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		}).collect(Collectors.toList());
	}
	
	public String getOptionLabel(String key) {
		return options.containsKey(key) ? options.get(key) : key;
	}

}
