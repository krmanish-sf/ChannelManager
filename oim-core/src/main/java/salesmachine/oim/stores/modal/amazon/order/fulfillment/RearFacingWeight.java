//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.26 at 07:46:49 PM IST 
//


package salesmachine.oim.stores.modal.amazon.order.fulfillment;

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
 *         &lt;element name="RearFacingMaximumWeight" type="{}WeightDimension" minOccurs="0"/>
 *         &lt;element name="RearFacingMinimumWeight" type="{}WeightDimension" minOccurs="0"/>
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
    "rearFacingMaximumWeight",
    "rearFacingMinimumWeight"
})
@XmlRootElement(name = "RearFacingWeight")
public class RearFacingWeight {

    @XmlElement(name = "RearFacingMaximumWeight")
    protected WeightDimension rearFacingMaximumWeight;
    @XmlElement(name = "RearFacingMinimumWeight")
    protected WeightDimension rearFacingMinimumWeight;

    /**
     * Gets the value of the rearFacingMaximumWeight property.
     * 
     * @return
     *     possible object is
     *     {@link WeightDimension }
     *     
     */
    public WeightDimension getRearFacingMaximumWeight() {
        return rearFacingMaximumWeight;
    }

    /**
     * Sets the value of the rearFacingMaximumWeight property.
     * 
     * @param value
     *     allowed object is
     *     {@link WeightDimension }
     *     
     */
    public void setRearFacingMaximumWeight(WeightDimension value) {
        this.rearFacingMaximumWeight = value;
    }

    /**
     * Gets the value of the rearFacingMinimumWeight property.
     * 
     * @return
     *     possible object is
     *     {@link WeightDimension }
     *     
     */
    public WeightDimension getRearFacingMinimumWeight() {
        return rearFacingMinimumWeight;
    }

    /**
     * Sets the value of the rearFacingMinimumWeight property.
     * 
     * @param value
     *     allowed object is
     *     {@link WeightDimension }
     *     
     */
    public void setRearFacingMinimumWeight(WeightDimension value) {
        this.rearFacingMinimumWeight = value;
    }

}
