//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.26 at 07:46:49 PM IST 
//


package salesmachine.oim.stores.modal.amazon.order.fulfillment;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CycleLengthUnitOfMeasure.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="CycleLengthUnitOfMeasure">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="CM"/>
 *     &lt;enumeration value="IN"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "CycleLengthUnitOfMeasure")
@XmlEnum
public enum CycleLengthUnitOfMeasure {

    CM,
    IN;

    public String value() {
        return name();
    }

    public static CycleLengthUnitOfMeasure fromValue(String v) {
        return valueOf(v);
    }

}