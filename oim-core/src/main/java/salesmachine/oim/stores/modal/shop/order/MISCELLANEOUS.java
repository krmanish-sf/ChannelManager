//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.01.22 at 11:59:38 PM IST 
//


package salesmachine.oim.stores.modal.shop.order;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *         &lt;element ref="{}DISTRIBUTION_ID" minOccurs="0"/>
 *         &lt;element ref="{}MERCHANT_SHOPPER_ID" minOccurs="0"/>
 *         &lt;element ref="{}WEB_ID" minOccurs="0"/>
 *         &lt;element ref="{}PC_ID" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "distributionid",
    "merchantshopperid",
    "webid",
    "pcid"
})
@XmlRootElement(name = "MISCELLANEOUS")
public class MISCELLANEOUS {

    @XmlElement(name = "DISTRIBUTION_ID")
    protected String distributionid;
    @XmlElement(name = "MERCHANT_SHOPPER_ID")
    protected String merchantshopperid;
    @XmlElement(name = "WEB_ID")
    protected String webid;
    @XmlElement(name = "PC_ID")
    protected String pcid;

    /**
     * Gets the value of the distributionid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDISTRIBUTIONID() {
        return distributionid;
    }

    /**
     * Sets the value of the distributionid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDISTRIBUTIONID(String value) {
        this.distributionid = value;
    }

    /**
     * Gets the value of the merchantshopperid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMERCHANTSHOPPERID() {
        return merchantshopperid;
    }

    /**
     * Sets the value of the merchantshopperid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMERCHANTSHOPPERID(String value) {
        this.merchantshopperid = value;
    }

    /**
     * Gets the value of the webid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWEBID() {
        return webid;
    }

    /**
     * Sets the value of the webid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWEBID(String value) {
        this.webid = value;
    }

    /**
     * Gets the value of the pcid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPCID() {
        return pcid;
    }

    /**
     * Sets the value of the pcid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPCID(String value) {
        this.pcid = value;
    }

}
