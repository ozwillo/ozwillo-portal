package org.oasis_eu.portal.core.mongo.model.sitemap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * User: lucaterori
 * Date: 08/06/2015
 */
public class SiteMapMenuItem implements Serializable {
	private static final long serialVersionUID = 5598242680668173525L;

	@JacksonXmlProperty(localName = "type", isAttribute = true)
	private String type = "";

	@JacksonXmlProperty(localName = "img_src", isAttribute = true)
	private String imgUrl = "";

	@JacksonXmlProperty(localName = "href", isAttribute = true)
	private String url = "";

	@JacksonXmlProperty(localName = "label", isAttribute = true)
	private String label = "";

	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "item")
	private List<SiteMapMenuItem> items = new ArrayList<>();

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isSubmenu() { return "submenu".equals(this.type); }

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public List<SiteMapMenuItem> getItems() {
		return items;
	}

	public void setItems(List<SiteMapMenuItem> items) {
		this.items = items;
	}

	@Override
	public String toString() {
		return "SiteMapMenuItem{" +
			"type='" + type + '\'' +
			", imgUrl='" + imgUrl + '\'' +
			", url='" + url + '\'' +
			", label='" + label + '\'' +
			", items=" + items +
			'}';
	}

}
