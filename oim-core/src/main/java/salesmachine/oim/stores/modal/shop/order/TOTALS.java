//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.01.22 at 11:59:38 PM IST 
//


package salesmachine.oim.stores.modal.shop.order;

import java.util.ArrayList;
import java.util.List;
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
 *         &lt;element ref="{}TL_ORDER_DATE"/>
 *         &lt;element ref="{}TL_SUBTOTAL"/>
 *         &lt;element ref="{}TL_TAX" minOccurs="0"/>
 *         &lt;element ref="{}TL_SHIPPING" minOccurs="0"/>
 *         &lt;element ref="{}TL_EXCISETAX" minOccurs="0"/>
 *         &lt;element ref="{}TL_TOTAL"/>
 *         &lt;element ref="{}TL_TAX_RATE" minOccurs="0"/>
 *         &lt;element ref="{}CC_DISCOUNTS" maxOccurs="unbounded" minOccurs="0"/>
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
    "tlorderdate",
    "tlsubtotal",
    "tltax",
    "tlshipping",
    "tlexcisetax",
    "tltotal",
    "tltaxrate",
    "ccdiscounts"
})
@XmlRootElement(name = "TOTALS")
public class TOTALS {

    @XmlElement(name = "TL_ORDER_DATE", required = true)
    protected String tlorderdate;
    @XmlElement(name = "TL_SUBTOTAL", required = true)
    protected String tlsubtotal;
    @XmlElement(name = "TL_TAX")
    protected String tltax;
    @XmlElement(name = "TL_SHIPPING")
    protected String tlshipping;
    @XmlElement(name = "TL_EXCISETAX")
    protected String tlexcisetax;
    @XmlElement(name = "TL_TOTAL", required = true)
    protected String tltotal;
    @XmlElement(name = "TL_TAX_RATE")
    protected String tltaxrate;
    @XmlElement(name = "CC_DISCOUNTS")
    protected List<CCDISCOUNTS> ccdiscounts;

    /**
     * Gets the value of the tlorderdate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTLORDERDATE() {
        return tlorderdate;
    }

    /**
     * Sets the value of the tlorderdate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTLORDERDATE(String value) {
        this.tlorderdate = value;
    }

    /**
     * Gets the value of the tlsubtotal property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTLSUBTOTAL() {
        return tlsubtotal;
    }

    /**
     * Sets the value of the tlsubtotal property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTLSUBTOTAL(String value) {
        this.tlsubtotal = value;
    }

    /**
     * Gets the value of the tltax property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTLTAX() {
        return tltax;
    }

    /**
     * Sets the value of the tltax property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTLTAX(String value) {
        this.tltax = value;
    }

    /**
     * Gets the value of the tlshipping property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTLSHIPPING() {
        return tlshipping;
    }

    /**
     * Sets the value of the tlshipping property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTLSHIPPING(String value) {
        this.tlshipping = value;
    }

    /**
     * Gets the value of the tlexcisetax property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTLEXCISETAX() {
        return tlexcisetax;
    }

    /**
     * Sets the value of the tlexcisetax property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTLEXCISETAX(String value) {
        this.tlexcisetax = value;
    }

    /**
     * Gets the value of the tltotal property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTLTOTAL() {
        return tltotal;
    }

    /**
     * Sets the value of the tltotal property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTLTOTAL(String value) {
        this.tltotal = value;
    }

    /**
     * Gets the value of the tltaxrate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTLTAXRATE() {
        return tltaxrate;
    }

    /**
     * Sets the value of the tltaxrate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTLTAXRATE(String value) {
        this.tltaxrate = value;
    }

    /**
     * Gets the value of the ccdiscounts property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ccdiscounts property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCCDISCOUNTS().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CCDISCOUNTS }
     * 
     * 
     */
    public List<CCDISCOUNTS> getCCDISCOUNTS() {
        if (ccdiscounts == null) {
            ccdiscounts = new ArrayList<CCDISCOUNTS>();
        }
        return this.ccdiscounts;
    }

}
