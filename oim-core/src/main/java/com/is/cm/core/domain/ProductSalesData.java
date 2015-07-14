package com.is.cm.core.domain;

public interface ProductSalesData {
/*	private static final long serialVersionUID = 4096536108008465493L;
	private final String sku;
	private final Double totalSales;
	private final int totalQuantity;

	public ProductSalesData(final String sku, final Double totalSales,
			final int totalQuantity) {
		this.sku = sku;
		this.totalSales = totalSales;
		this.totalQuantity = totalQuantity;
	}*/

	String getSku(); /*{
		return sku;
	}*/

	Double getTotalSales();/* {
		return totalSales;
	}*/

	int getTotalQunatity() ;/*{
		return totalQuantity;
	}*/
}
