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
 *         &lt;element ref="{}DISCOUNT_CODE"/>
 *         &lt;element ref="{}DISCOUNT_AMOUNT"/>
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
    "discountcode",
    "discountamount"
})
@XmlRootElement(name = "CC_DISCOUNT")
public class CCDISCOUNT {

    @XmlElement(name = "DISCOUNT_CODE", required = true)
    protected String discountcode;
    @XmlElement(name = "DISCOUNT_AMOUNT", required = true)
    protected String discountamount;

    /**
     * Gets the value of the discountcode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDISCOUNTCODE() {
        return discountcode;
    }

    /**
     * Sets the value of the discountcode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDISCOUNTCODE(String value) {
        this.discountcode = value;
    }

    /**
     * Gets the value of the discountamount property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDISCOUNTAMOUNT() {
        return discountamount;
    }

    /**
     * Sets the value of the discountamount property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDISCOUNTAMOUNT(String value) {
        this.discountamount = value;
    }

}
