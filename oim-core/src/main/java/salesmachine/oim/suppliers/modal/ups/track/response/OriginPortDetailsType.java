//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.05.30 at 02:47:01 PM IST 
//


package salesmachine.oim.suppliers.modal.ups.track.response;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OriginPortDetailsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OriginPortDetailsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="OriginPort" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="EstimatedDeparture" type="{}DateTime" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OriginPortDetailsType", propOrder = {
    "originPort",
    "estimatedDeparture"
})
public class OriginPortDetailsType {

    @XmlElement(name = "OriginPort")
    protected String originPort;
    @XmlElement(name = "EstimatedDeparture")
    protected DateTime estimatedDeparture;

    /**
     * Gets the value of the originPort property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOriginPort() {
        return originPort;
    }

    /**
     * Sets the value of the originPort property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOriginPort(String value) {
        this.originPort = value;
    }

    /**
     * Gets the value of the estimatedDeparture property.
     * 
     * @return
     *     possible object is
     *     {@link DateTime }
     *     
     */
    public DateTime getEstimatedDeparture() {
        return estimatedDeparture;
    }

    /**
     * Sets the value of the estimatedDeparture property.
     * 
     * @param value
     *     allowed object is
     *     {@link DateTime }
     *     
     */
    public void setEstimatedDeparture(DateTime value) {
        this.estimatedDeparture = value;
    }

}
