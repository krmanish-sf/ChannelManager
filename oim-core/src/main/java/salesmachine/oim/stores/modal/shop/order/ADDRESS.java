//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.01.22 at 11:59:38 PM IST 
//


package salesmachine.oim.stores.modal.shop.order;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}AD_ADDRESS1"/>
 *         &lt;element ref="{}AD_CITY"/>
 *         &lt;element ref="{}AD_STATE"/>
 *         &lt;element ref="{}AD_COUNTRY" minOccurs="0"/>
 *         &lt;element ref="{}AD_PROVINCE" minOccurs="0"/>
 *         &lt;element ref="{}AD_ZIP" minOccurs="0"/>
 *         &lt;element ref="{}AD_FLAT" minOccurs="0"/>
 *         &lt;element ref="{}AD_ADDRESS2" minOccurs="0"/>
 *         &lt;element ref="{}AD_COUNTRY_CODE" minOccurs="0"/>
 *         &lt;element ref="{}AD_COMPANY" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="IS_INTERNATIONAL" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "adaddress1",
    "adcity",
    "adstate",
    "adcountry",
    "adprovince",
    "adzip",
    "adflat",
    "adaddress2",
    "adcountrycode",
    "adcompany"
})
@XmlRootElement(name = "ADDRESS")
public class ADDRESS {

    @XmlElement(name = "AD_ADDRESS1", required = true)
    protected String adaddress1;
    @XmlElement(name = "AD_CITY", required = true)
    protected String adcity;
    @XmlElement(name = "AD_STATE", required = true)
    protected String adstate;
    @XmlElement(name = "AD_COUNTRY")
    protected String adcountry;
    @XmlElement(name = "AD_PROVINCE")
    protected String adprovince;
    @XmlElement(name = "AD_ZIP")
    protected String adzip;
    @XmlElement(name = "AD_FLAT")
    protected String adflat;
    @XmlElement(name = "AD_ADDRESS2")
    protected String adaddress2;
    @XmlElement(name = "AD_COUNTRY_CODE")
    protected String adcountrycode;
    @XmlElement(name = "AD_COMPANY")
    protected String adcompany;
    @XmlAttribute(name = "IS_INTERNATIONAL", required = true)
    protected String isinternational;

    /**
     * Gets the value of the adaddress1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getADADDRESS1() {
        return adaddress1;
    }

    /**
     * Sets the value of the adaddress1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setADADDRESS1(String value) {
        this.adaddress1 = value;
    }

    /**
     * Gets the value of the adcity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getADCITY() {
        return adcity;
    }

    /**
     * Sets the value of the adcity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setADCITY(String value) {
        this.adcity = value;
    }

    /**
     * Gets the value of the adstate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getADSTATE() {
        return adstate;
    }

    /**
     * Sets the value of the adstate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setADSTATE(String value) {
        this.adstate = value;
    }

    /**
     * Gets the value of the adcountry property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getADCOUNTRY() {
        return adcountry;
    }

    /**
     * Sets the value of the adcountry property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setADCOUNTRY(String value) {
        this.adcountry = value;
    }

    /**
     * Gets the value of the adprovince property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getADPROVINCE() {
        return adprovince;
    }

    /**
     * Sets the value of the adprovince property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setADPROVINCE(String value) {
        this.adprovince = value;
    }

    /**
     * Gets the value of the adzip property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getADZIP() {
        return adzip;
    }

    /**
     * Sets the value of the adzip property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setADZIP(String value) {
        this.adzip = value;
    }

    /**
     * Gets the value of the adflat property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getADFLAT() {
        return adflat;
    }

    /**
     * Sets the value of the adflat property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setADFLAT(String value) {
        this.adflat = value;
    }

    /**
     * Gets the value of the adaddress2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getADADDRESS2() {
        return adaddress2;
    }

    /**
     * Sets the value of the adaddress2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setADADDRESS2(String value) {
        this.adaddress2 = value;
    }

    /**
     * Gets the value of the adcountrycode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getADCOUNTRYCODE() {
        return adcountrycode;
    }

    /**
     * Sets the value of the adcountrycode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setADCOUNTRYCODE(String value) {
        this.adcountrycode = value;
    }

    /**
     * Gets the value of the adcompany property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getADCOMPANY() {
        return adcompany;
    }

    /**
     * Sets the value of the adcompany property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setADCOMPANY(String value) {
        this.adcompany = value;
    }

    /**
     * Gets the value of the isinternational property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getISINTERNATIONAL() {
        return isinternational;
    }

    /**
     * Sets the value of the isinternational property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setISINTERNATIONAL(String value) {
        this.isinternational = value;
    }

}