package salesmachine.hibernatedb;

// Generated 30 Mar, 2010 7:26:10 PM by Hibernate Tools 3.2.4.GA

import java.util.Date;

/**
 * Product generated by hbm2java
 */
public class Product implements java.io.Serializable {

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

	public Product(Integer productId) {
		this.productId = productId;
	}

	public Product(Integer productId, Integer dropshipperId, String sku,
			String title, Double wholesalePrice, Double retailPrice,
			Integer productCategoryId, Integer productStatusId, String image,
			String thumbnail, String manufacturer, Integer quantity,
			String weight, String length, String width, String height,
			Integer productChangeStatusId, String manufacturerId,
			String infopiaCategoryId, String categorySuggested1,
			String categorySuggested2, Double dropshipFee, Double mapPrice,
			Double msrp, Integer categorySuggestedId, Date insertDate,
			Date deactivateDate, Double wholesalePrice2,
			String descriptionShort, String upc, String originalImageUrl,
			Integer ebayCategoryId, Integer amazonCategoryId,
			Integer yahooCategoryId, Integer hasValidImage,
			String categorySuggested3, String categorySuggested4,
			String categorySuggested5, String format,
			String manufacturerImageUrl, String image2Url, String image3Url,
			String asin, Double customShippingRate, Date reactivateDate,
			String smallImageUrl, String largeImageUrl,
			String originalThumbnailUrl, String yahooSku1, String yahooSku2,
			String oem) {
		this.productId = productId;
		this.dropshipperId = dropshipperId;
		this.sku = sku;
		this.title = title;
		this.wholesalePrice = wholesalePrice;
		this.retailPrice = retailPrice;
		this.productCategoryId = productCategoryId;
		this.productStatusId = productStatusId;
		this.image = image;
		this.thumbnail = thumbnail;
		this.manufacturer = manufacturer;
		this.quantity = quantity;
		this.weight = weight;
		this.length = length;
		this.width = width;
		this.height = height;
		this.productChangeStatusId = productChangeStatusId;
		this.manufacturerId = manufacturerId;
		this.infopiaCategoryId = infopiaCategoryId;
		this.categorySuggested1 = categorySuggested1;
		this.categorySuggested2 = categorySuggested2;
		this.dropshipFee = dropshipFee;
		this.mapPrice = mapPrice;
		this.msrp = msrp;
		this.categorySuggestedId = categorySuggestedId;
		this.insertDate = insertDate;
		this.deactivateDate = deactivateDate;
		this.wholesalePrice2 = wholesalePrice2;
		this.descriptionShort = descriptionShort;
		this.upc = upc;
		this.originalImageUrl = originalImageUrl;
		this.ebayCategoryId = ebayCategoryId;
		this.amazonCategoryId = amazonCategoryId;
		this.yahooCategoryId = yahooCategoryId;
		this.hasValidImage = hasValidImage;
		this.categorySuggested3 = categorySuggested3;
		this.categorySuggested4 = categorySuggested4;
		this.categorySuggested5 = categorySuggested5;
		this.format = format;
		this.manufacturerImageUrl = manufacturerImageUrl;
		this.image2Url = image2Url;
		this.image3Url = image3Url;
		this.asin = asin;
		this.customShippingRate = customShippingRate;
		this.reactivateDate = reactivateDate;
		this.smallImageUrl = smallImageUrl;
		this.largeImageUrl = largeImageUrl;
		this.originalThumbnailUrl = originalThumbnailUrl;
		this.yahooSku1 = yahooSku1;
		this.yahooSku2 = yahooSku2;
		this.oem = oem;
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
