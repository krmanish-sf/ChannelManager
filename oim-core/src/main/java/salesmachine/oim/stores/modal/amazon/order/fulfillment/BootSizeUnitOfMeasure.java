//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.26 at 07:46:49 PM IST 
//


package salesmachine.oim.stores.modal.amazon.order.fulfillment;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BootSizeUnitOfMeasure.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="BootSizeUnitOfMeasure">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="adult_us"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "BootSizeUnitOfMeasure")
@XmlEnum
public enum BootSizeUnitOfMeasure {

    @XmlEnumValue("adult_us")
    ADULT_US("adult_us");
    private final String value;

    BootSizeUnitOfMeasure(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BootSizeUnitOfMeasure fromValue(String v) {
        for (BootSizeUnitOfMeasure c: BootSizeUnitOfMeasure.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
