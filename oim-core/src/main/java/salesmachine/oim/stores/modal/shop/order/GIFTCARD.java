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
 *         &lt;element ref="{}GF_FROM" minOccurs="0"/>
 *         &lt;element ref="{}GF_TO" minOccurs="0"/>
 *         &lt;element ref="{}GF_DELIV_DATE" minOccurs="0"/>
 *         &lt;element ref="{}GF_MESSAGE" minOccurs="0"/>
 *         &lt;element ref="{}GF_GREETING" minOccurs="0"/>
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
    "gffrom",
    "gfto",
    "gfdelivdate",
    "gfmessage",
    "gfgreeting"
})
@XmlRootElement(name = "GIFT_CARD")
public class GIFTCARD {

    @XmlElement(name = "GF_FROM")
    protected String gffrom;
    @XmlElement(name = "GF_TO")
    protected String gfto;
    @XmlElement(name = "GF_DELIV_DATE")
    protected String gfdelivdate;
    @XmlElement(name = "GF_MESSAGE")
    protected String gfmessage;
    @XmlElement(name = "GF_GREETING")
    protected String gfgreeting;

    /**
     * Gets the value of the gffrom property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGFFROM() {
        return gffrom;
    }

    /**
     * Sets the value of the gffrom property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGFFROM(String value) {
        this.gffrom = value;
    }

    /**
     * Gets the value of the gfto property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGFTO() {
        return gfto;
    }

    /**
     * Sets the value of the gfto property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGFTO(String value) {
        this.gfto = value;
    }

    /**
     * Gets the value of the gfdelivdate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGFDELIVDATE() {
        return gfdelivdate;
    }

    /**
     * Sets the value of the gfdelivdate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGFDELIVDATE(String value) {
        this.gfdelivdate = value;
    }

    /**
     * Gets the value of the gfmessage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGFMESSAGE() {
        return gfmessage;
    }

    /**
     * Sets the value of the gfmessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGFMESSAGE(String value) {
        this.gfmessage = value;
    }

    /**
     * Gets the value of the gfgreeting property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGFGREETING() {
        return gfgreeting;
    }

    /**
     * Sets the value of the gfgreeting property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGFGREETING(String value) {
        this.gfgreeting = value;
    }

}
