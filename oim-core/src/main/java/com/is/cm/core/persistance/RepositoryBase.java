package com.is.cm.core.persistance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.is.cm.core.security.AuthenticationInterceptor.MyThreadLocal;

public class RepositoryBase {
	private static final Logger LOG = LoggerFactory
			.getLogger(RepositoryBase.class);

	protected Integer getVendorId() {
		LOG.debug("Accessing vendorId# {} thread locale...", MyThreadLocal.get());
		return MyThreadLocal.get();
	}
}
