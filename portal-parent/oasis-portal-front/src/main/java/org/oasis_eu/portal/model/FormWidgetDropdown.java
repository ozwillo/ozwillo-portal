package org.oasis_eu.portal.model;

import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;


public class FormWidgetDropdown extends FormWidget implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(FormWidgetDropdown.class);
	private static final long serialVersionUID = 3937276983783616794L;

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
		if(Strings.isNullOrEmpty(key) || "null".equals(key)) return "ui.default_value";
		Locale keyLocale = Locale.forLanguageTag(key); // including "en-GB fr" http://docs.oracle.com/javase/tutorial/i18n/locale/create.html
		if (keyLocale != null) {
		    return keyLocale.getLanguage();
		}
        return key;
	}

}
