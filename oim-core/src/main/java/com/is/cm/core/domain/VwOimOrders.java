package com.is.cm.core.domain;

public class VwOimOrders extends DomainBase {
	private static final long serialVersionUID = -3556596317466851639L;
	private VwOimOrdersId id;

	public VwOimOrders() {
	}

	public VwOimOrdersId getId() {
		return this.id;
	}

	public void setId(VwOimOrdersId id) {
		this.id = id;
	}

}
