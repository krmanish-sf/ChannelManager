package com.is.cm.core.domain;

public class VendorContext {
	private VendorContext() {

	}

	private static final ThreadLocal<Integer> userThreadLocal = new ThreadLocal<Integer>();

	public static void set(Integer user) {
		userThreadLocal.set(user);
	}

	public static void unset() {
		userThreadLocal.remove();
	}

	public static Integer get() {
		return userThreadLocal.get();
	}
}