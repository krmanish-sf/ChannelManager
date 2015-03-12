//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.01.22 at 11:59:38 PM IST 
//


package com.is.cm.core.domain.shop;

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
 *       &lt;choice>
 *         &lt;element ref="{}CREDIT_CARD" minOccurs="0"/>
 *         &lt;element ref="{}BANK_TRANSFER" minOccurs="0"/>
 *         &lt;element ref="{}BANK_ACCOUNT" minOccurs="0"/>
 *         &lt;element ref="{}COD" minOccurs="0"/>
 *         &lt;element ref="{}COD_WITH_DELIVERY_DATE" minOccurs="0"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "creditcard",
    "banktransfer",
    "bankaccount",
    "cod",
    "codwithdeliverydate"
})
@XmlRootElement(name = "PAYMENT_METHOD")
public class PAYMENTMETHOD {

    @XmlElement(name = "CREDIT_CARD")
    protected CREDITCARD creditcard;
    @XmlElement(name = "BANK_TRANSFER")
    protected BANKTRANSFER banktransfer;
    @XmlElement(name = "BANK_ACCOUNT")
    protected BANKACCOUNT bankaccount;
    @XmlElement(name = "COD")
    protected COD cod;
    @XmlElement(name = "COD_WITH_DELIVERY_DATE")
    protected CODWITHDELIVERYDATE codwithdeliverydate;

    /**
     * Gets the value of the creditcard property.
     * 
     * @return
     *     possible object is
     *     {@link CREDITCARD }
     *     
     */
    public CREDITCARD getCREDITCARD() {
        return creditcard;
    }

    /**
     * Sets the value of the creditcard property.
     * 
     * @param value
     *     allowed object is
     *     {@link CREDITCARD }
     *     
     */
    public void setCREDITCARD(CREDITCARD value) {
        this.creditcard = value;
    }

    /**
     * Gets the value of the banktransfer property.
     * 
     * @return
     *     possible object is
     *     {@link BANKTRANSFER }
     *     
     */
    public BANKTRANSFER getBANKTRANSFER() {
        return banktransfer;
    }

    /**
     * Sets the value of the banktransfer property.
     * 
     * @param value
     *     allowed object is
     *     {@link BANKTRANSFER }
     *     
     */
    public void setBANKTRANSFER(BANKTRANSFER value) {
        this.banktransfer = value;
    }

    /**
     * Gets the value of the bankaccount property.
     * 
     * @return
     *     possible object is
     *     {@link BANKACCOUNT }
     *     
     */
    public BANKACCOUNT getBANKACCOUNT() {
        return bankaccount;
    }

    /**
     * Sets the value of the bankaccount property.
     * 
     * @param value
     *     allowed object is
     *     {@link BANKACCOUNT }
     *     
     */
    public void setBANKACCOUNT(BANKACCOUNT value) {
        this.bankaccount = value;
    }

    /**
     * Gets the value of the cod property.
     * 
     * @return
     *     possible object is
     *     {@link COD }
     *     
     */
    public COD getCOD() {
        return cod;
    }

    /**
     * Sets the value of the cod property.
     * 
     * @param value
     *     allowed object is
     *     {@link COD }
     *     
     */
    public void setCOD(COD value) {
        this.cod = value;
    }

    /**
     * Gets the value of the codwithdeliverydate property.
     * 
     * @return
     *     possible object is
     *     {@link CODWITHDELIVERYDATE }
     *     
     */
    public CODWITHDELIVERYDATE getCODWITHDELIVERYDATE() {
        return codwithdeliverydate;
    }

    /**
     * Sets the value of the codwithdeliverydate property.
     * 
     * @param value
     *     allowed object is
     *     {@link CODWITHDELIVERYDATE }
     *     
     */
    public void setCODWITHDELIVERYDATE(CODWITHDELIVERYDATE value) {
        this.codwithdeliverydate = value;
    }

}
