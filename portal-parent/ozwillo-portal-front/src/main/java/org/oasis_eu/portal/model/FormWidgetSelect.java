package org.oasis_eu.portal.model;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;


public class FormWidgetSelect extends FormWidget implements Serializable {

	private static final Logger logger = LoggerFactory.getLogger(FormWidgetSelect.class);
	private static final long serialVersionUID = 3937276983783616794L;

	private Map<String, String> options = new HashMap<>();
	private Function<String, String> normalizeKey = null;

	public FormWidgetSelect(String id, String label) {
		super(id, label);
	}

	public FormWidgetSelect(String id, String label, Function<String, String> normalizeKey) {
		this(id, label);
		this.normalizeKey = normalizeKey;
	}

	@Override
	public String getType() {
		return "select";
	}

	public Function<String, String> getNormalizeKey() {
		return normalizeKey;
	}
	
	public FormWidgetSelect addOption(String key, String value) {
		options.put(key, value);
		return this;
	}
	
	public List<Entry<String, String>> getOptions() {
		return options.entrySet().stream().sorted(new Comparator<Entry<String, String>>() {
			@Override
			public int compare(Entry<String, String> o1, Entry<String, String> o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		}).collect(Collectors.toList());
	}
	
	public String getOptionLabel(String key) {
		if(Strings.isNullOrEmpty(key) || "null".equals(key)) return "ui.default_value";
		if (normalizeKey != null) {
			key = normalizeKey.apply(key);
			if(Strings.isNullOrEmpty(key) || "null".equals(key)) return "ui.default_value";
		}
		return options.containsKey(key) ? options.get(key) : key;
	}

}
