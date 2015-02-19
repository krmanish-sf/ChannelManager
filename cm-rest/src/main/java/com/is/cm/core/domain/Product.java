package com.is.cm.core.domain;

import java.util.Date;

public class Product extends DomainBase implements java.io.Serializable {
	private static final long serialVersionUID = -8348365888779179169L;
	private Integer productId;
	private Integer dropshipperId;
	private String sku;
	private String title;
	private Double wholesalePrice;
	private Double retailPrice;
	private Integer productCategoryId;
	private Integer productStatusId;
	private String image;
	private String thumbnail;
	private String manufacturer;
	private Integer quantity;
	private String weight;
	private String length;
	private String width;
	private String height;
	private Integer productChangeStatusId;
	private String manufacturerId;
	private String infopiaCategoryId;
	private String categorySuggested1;
	private String categorySuggested2;
	private Double dropshipFee;
	private Double mapPrice;
	private Double msrp;
	private Integer categorySuggestedId;
	private Date insertDate;
	private Date deactivateDate;
	private Double wholesalePrice2;
	private String descriptionShort;
	private String upc;
	private String originalImageUrl;
	private Integer ebayCategoryId;
	private Integer amazonCategoryId;
	private Integer yahooCategoryId;
	private Integer hasValidImage;
	private String categorySuggested3;
	private String categorySuggested4;
	private String categorySuggested5;
	private String format;
	private String manufacturerImageUrl;
	private String image2Url;
	private String image3Url;
	private String asin;
	private Double customShippingRate;
	private Date reactivateDate;
	private String smallImageUrl;
	private String largeImageUrl;
	private String originalThumbnailUrl;
	private String yahooSku1;
	private String yahooSku2;
	private String oem;

	public Product() {
	}

	public Integer getProductId() {
		return this.productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	public Integer getDropshipperId() {
		return this.dropshipperId;
	}

	public void setDropshipperId(Integer dropshipperId) {
		this.dropshipperId = dropshipperId;
	}

	public String getSku() {
		return this.sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Double getWholesalePrice() {
		return this.wholesalePrice;
	}

	public void setWholesalePrice(Double wholesalePrice) {
		this.wholesalePrice = wholesalePrice;
	}

	public Double getRetailPrice() {
		return this.retailPrice;
	}

	public void setRetailPrice(Double retailPrice) {
		this.retailPrice = retailPrice;
	}

	public Integer getProductCategoryId() {
		return this.productCategoryId;
	}

	public void setProductCategoryId(Integer productCategoryId) {
		this.productCategoryId = productCategoryId;
	}

	public Integer getProductStatusId() {
		return this.productStatusId;
	}

	public void setProductStatusId(Integer productStatusId) {
		this.productStatusId = productStatusId;
	}

	public String getImage() {
		return this.image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getThumbnail() {
		return this.thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public String getManufacturer() {
		return this.manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public Integer getQuantity() {
		return this.quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public String getWeight() {
		return this.weight;
	}

	public void setWeight(String weight) {
		this.weight = weight;
	}

	public String getLength() {
		return this.length;
	}

	public void setLength(String length) {
		this.length = length;
	}

	public String getWidth() {
		return this.width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public String getHeight() {
		return this.height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public Integer getProductChangeStatusId() {
		return this.productChangeStatusId;
	}

	public void setProductChangeStatusId(Integer productChangeStatusId) {
		this.productChangeStatusId = productChangeStatusId;
	}

	public String getManufacturerId() {
		return this.manufacturerId;
	}

	public void setManufacturerId(String manufacturerId) {
		this.manufacturerId = manufacturerId;
	}

	public String getInfopiaCategoryId() {
		return this.infopiaCategoryId;
	}

	public void setInfopiaCategoryId(String infopiaCategoryId) {
		this.infopiaCategoryId = infopiaCategoryId;
	}

	public String getCategorySuggested1() {
		return this.categorySuggested1;
	}

	public void setCategorySuggested1(String categorySuggested1) {
		this.categorySuggested1 = categorySuggested1;
	}

	public String getCategorySuggested2() {
		return this.categorySuggested2;
	}

	public void setCategorySuggested2(String categorySuggested2) {
		this.categorySuggested2 = categorySuggested2;
	}

	public Double getDropshipFee() {
		return this.dropshipFee;
	}

	public void setDropshipFee(Double dropshipFee) {
		this.dropshipFee = dropshipFee;
	}

	public Double getMapPrice() {
		return this.mapPrice;
	}

	public void setMapPrice(Double mapPrice) {
		this.mapPrice = mapPrice;
	}

	public Double getMsrp() {
		return this.msrp;
	}

	public void setMsrp(Double msrp) {
		this.msrp = msrp;
	}

	public Integer getCategorySuggestedId() {
		return this.categorySuggestedId;
	}

	public void setCategorySuggestedId(Integer categorySuggestedId) {
		this.categorySuggestedId = categorySuggestedId;
	}

	public Date getInsertDate() {
		return this.insertDate;
	}

	public void setInsertDate(Date insertDate) {
		this.insertDate = insertDate;
	}

	public Date getDeactivateDate() {
		return this.deactivateDate;
	}

	public void setDeactivateDate(Date deactivateDate) {
		this.deactivateDate = deactivateDate;
	}

	public Double getWholesalePrice2() {
		return this.wholesalePrice2;
	}

	public void setWholesalePrice2(Double wholesalePrice2) {
		this.wholesalePrice2 = wholesalePrice2;
	}

	public String getDescriptionShort() {
		return this.descriptionShort;
	}

	public void setDescriptionShort(String descriptionShort) {
		this.descriptionShort = descriptionShort;
	}

	public String getUpc() {
		return this.upc;
	}

	public void setUpc(String upc) {
		this.upc = upc;
	}

	public String getOriginalImageUrl() {
		return this.originalImageUrl;
	}

	public void setOriginalImageUrl(String originalImageUrl) {
		this.originalImageUrl = originalImageUrl;
	}

	public Integer getEbayCategoryId() {
		return this.ebayCategoryId;
	}

	public void setEbayCategoryId(Integer ebayCategoryId) {
		this.ebayCategoryId = ebayCategoryId;
	}

	public Integer getAmazonCategoryId() {
		return this.amazonCategoryId;
	}

	public void setAmazonCategoryId(Integer amazonCategoryId) {
		this.amazonCategoryId = amazonCategoryId;
	}

	public Integer getYahooCategoryId() {
		return this.yahooCategoryId;
	}

	public void setYahooCategoryId(Integer yahooCategoryId) {
		this.yahooCategoryId = yahooCategoryId;
	}

	public Integer getHasValidImage() {
		return this.hasValidImage;
	}

	public void setHasValidImage(Integer hasValidImage) {
		this.hasValidImage = hasValidImage;
	}

	public String getCategorySuggested3() {
		return this.categorySuggested3;
	}

	public void setCategorySuggested3(String categorySuggested3) {
		this.categorySuggested3 = categorySuggested3;
	}

	public String getCategorySuggested4() {
		return this.categorySuggested4;
	}

	public void setCategorySuggested4(String categorySuggested4) {
		this.categorySuggested4 = categorySuggested4;
	}

	public String getCategorySuggested5() {
		return this.categorySuggested5;
	}

	public void setCategorySuggested5(String categorySuggested5) {
		this.categorySuggested5 = categorySuggested5;
	}

	public String getFormat() {
		return this.format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getManufacturerImageUrl() {
		return this.manufacturerImageUrl;
	}

	public void setManufacturerImageUrl(String manufacturerImageUrl) {
		this.manufacturerImageUrl = manufacturerImageUrl;
	}

	public String getImage2Url() {
		return this.image2Url;
	}

	public void setImage2Url(String image2Url) {
		this.image2Url = image2Url;
	}

	public String getImage3Url() {
		return this.image3Url;
	}

	public void setImage3Url(String image3Url) {
		this.image3Url = image3Url;
	}

	public String getAsin() {
		return this.asin;
	}

	public void setAsin(String asin) {
		this.asin = asin;
	}

	public Double getCustomShippingRate() {
		return this.customShippingRate;
	}

	public void setCustomShippingRate(Double customShippingRate) {
		this.customShippingRate = customShippingRate;
	}

	public Date getReactivateDate() {
		return this.reactivateDate;
	}

	public void setReactivateDate(Date reactivateDate) {
		this.reactivateDate = reactivateDate;
	}

	public String getSmallImageUrl() {
		return this.smallImageUrl;
	}

	public void setSmallImageUrl(String smallImageUrl) {
		this.smallImageUrl = smallImageUrl;
	}

	public String getLargeImageUrl() {
		return this.largeImageUrl;
	}

	public void setLargeImageUrl(String largeImageUrl) {
		this.largeImageUrl = largeImageUrl;
	}

	public String getOriginalThumbnailUrl() {
		return this.originalThumbnailUrl;
	}

	public void setOriginalThumbnailUrl(String originalThumbnailUrl) {
		this.originalThumbnailUrl = originalThumbnailUrl;
	}

	public String getYahooSku1() {
		return this.yahooSku1;
	}

	public void setYahooSku1(String yahooSku1) {
		this.yahooSku1 = yahooSku1;
	}

	public String getYahooSku2() {
		return this.yahooSku2;
	}

	public void setYahooSku2(String yahooSku2) {
		this.yahooSku2 = yahooSku2;
	}

	public String getOem() {
		return this.oem;
	}

	public void setOem(String oem) {
		this.oem = oem;
	}

}
