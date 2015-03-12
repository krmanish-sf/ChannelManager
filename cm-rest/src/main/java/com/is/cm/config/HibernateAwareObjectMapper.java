package com.is.cm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate3.Hibernate3Module;

public class HibernateAwareObjectMapper extends ObjectMapper {
	/**
	 * 
	 */
	private static final long serialVersionUID = 169923811440040264L;

	public HibernateAwareObjectMapper() {
		registerModule(new Hibernate3Module());
	}
}
