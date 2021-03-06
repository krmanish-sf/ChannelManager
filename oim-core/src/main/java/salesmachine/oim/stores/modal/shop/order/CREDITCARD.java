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
 *         &lt;element ref="{}CC_TYPE"/>
 *         &lt;element ref="{}CC_NUMBER" minOccurs="0"/>
 *         &lt;element ref="{}CC_EXPIRATION" minOccurs="0"/>
 *         &lt;element ref="{}CC_NAMEONCARD" minOccurs="0"/>
 *         &lt;element ref="{}CC_SECURITY_NUMBER" minOccurs="0"/>
 *         &lt;element ref="{}CC_ISSUING_BANK" minOccurs="0"/>
 *         &lt;element ref="{}CC_ISSUE_NUMBER" minOccurs="0"/>
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
    "cctype",
    "ccnumber",
    "ccexpiration",
    "ccnameoncard",
    "ccsecuritynumber",
    "ccissuingbank",
    "ccissuenumber"
})
@XmlRootElement(name = "CREDIT_CARD")
public class CREDITCARD {

    @XmlElement(name = "CC_TYPE", required = true)
    protected String cctype;
    @XmlElement(name = "CC_NUMBER")
    protected String ccnumber;
    @XmlElement(name = "CC_EXPIRATION")
    protected String ccexpiration;
    @XmlElement(name = "CC_NAMEONCARD")
    protected String ccnameoncard;
    @XmlElement(name = "CC_SECURITY_NUMBER")
    protected String ccsecuritynumber;
    @XmlElement(name = "CC_ISSUING_BANK")
    protected String ccissuingbank;
    @XmlElement(name = "CC_ISSUE_NUMBER")
    protected String ccissuenumber;

    /**
     * Gets the value of the cctype property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCCTYPE() {
        return cctype;
    }

    /**
     * Sets the value of the cctype property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCCTYPE(String value) {
        this.cctype = value;
    }

    /**
     * Gets the value of the ccnumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCCNUMBER() {
        return ccnumber;
    }

    /**
     * Sets the value of the ccnumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCCNUMBER(String value) {
        this.ccnumber = value;
    }

    /**
     * Gets the value of the ccexpiration property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCCEXPIRATION() {
        return ccexpiration;
    }

    /**
     * Sets the value of the ccexpiration property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCCEXPIRATION(String value) {
        this.ccexpiration = value;
    }

    /**
     * Gets the value of the ccnameoncard property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCCNAMEONCARD() {
        return ccnameoncard;
    }

    /**
     * Sets the value of the ccnameoncard property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCCNAMEONCARD(String value) {
        this.ccnameoncard = value;
    }

    /**
     * Gets the value of the ccsecuritynumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCCSECURITYNUMBER() {
        return ccsecuritynumber;
    }

    /**
     * Sets the value of the ccsecuritynumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCCSECURITYNUMBER(String value) {
        this.ccsecuritynumber = value;
    }

    /**
     * Gets the value of the ccissuingbank property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCCISSUINGBANK() {
        return ccissuingbank;
    }

    /**
     * Sets the value of the ccissuingbank property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCCISSUINGBANK(String value) {
        this.ccissuingbank = value;
    }

    /**
     * Gets the value of the ccissuenumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCCISSUENUMBER() {
        return ccissuenumber;
    }

    /**
     * Sets the value of the ccissuenumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCCISSUENUMBER(String value) {
        this.ccissuenumber = value;
    }

}
