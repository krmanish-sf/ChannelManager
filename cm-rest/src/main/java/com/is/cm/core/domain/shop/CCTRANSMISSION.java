//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.01.22 at 11:59:38 PM IST 
//


package com.is.cm.core.domain.shop;

import java.util.ArrayList;
import java.util.List;
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
 *         &lt;element ref="{}CC_ORDER" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="CATALOG_ID" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;maxLength value="10"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="NUMBER_OF_ORDERS" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;maxLength value="10"/>
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
    "ccorder"
})
@XmlRootElement(name = "CC_TRANSMISSION")
public class CCTRANSMISSION {

    @XmlElement(name = "CC_ORDER", required = true)
    protected List<CCORDER> ccorder;
    @XmlAttribute(name = "CATALOG_ID", required = true)
    protected String catalogid;
    @XmlAttribute(name = "NUMBER_OF_ORDERS", required = true)
    protected String numberoforders;

    /**
     * Gets the value of the ccorder property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ccorder property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCCORDER().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CCORDER }
     * 
     * 
     */
    public List<CCORDER> getCCORDER() {
        if (ccorder == null) {
            ccorder = new ArrayList<CCORDER>();
        }
        return this.ccorder;
    }

    /**
     * Gets the value of the catalogid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCATALOGID() {
        return catalogid;
    }

    /**
     * Sets the value of the catalogid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCATALOGID(String value) {
        this.catalogid = value;
    }

    /**
     * Gets the value of the numberoforders property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNUMBEROFORDERS() {
        return numberoforders;
    }

    /**
     * Sets the value of the numberoforders property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNUMBEROFORDERS(String value) {
        this.numberoforders = value;
    }

}
