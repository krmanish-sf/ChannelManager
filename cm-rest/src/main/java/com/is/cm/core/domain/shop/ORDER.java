//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.01.23 at 12:14:25 AM IST 
//


package com.is.cm.core.domain.shop;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *       &lt;attribute name="ALTURA_CATALOG_ID" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="ALTURA_INVOICE_NO" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="ALTURA_PURCHASE_NO" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "ORDER")
public class ORDER {

    @XmlAttribute(name = "ALTURA_CATALOG_ID")
    protected String alturacatalogid;
    @XmlAttribute(name = "ALTURA_INVOICE_NO")
    protected String alturainvoiceno;
    @XmlAttribute(name = "ALTURA_PURCHASE_NO")
    protected String alturapurchaseno;

    /**
     * Gets the value of the alturacatalogid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getALTURACATALOGID() {
        return alturacatalogid;
    }

    /**
     * Sets the value of the alturacatalogid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setALTURACATALOGID(String value) {
        this.alturacatalogid = value;
    }

    /**
     * Gets the value of the alturainvoiceno property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getALTURAINVOICENO() {
        return alturainvoiceno;
    }

    /**
     * Sets the value of the alturainvoiceno property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setALTURAINVOICENO(String value) {
        this.alturainvoiceno = value;
    }

    /**
     * Gets the value of the alturapurchaseno property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getALTURAPURCHASENO() {
        return alturapurchaseno;
    }

    /**
     * Sets the value of the alturapurchaseno property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setALTURAPURCHASENO(String value) {
        this.alturapurchaseno = value;
    }

}
