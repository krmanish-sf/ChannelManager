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
 * <p>Java class for EnergyLabelEfficiencyClass.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="EnergyLabelEfficiencyClass">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="a"/>
 *     &lt;enumeration value="b"/>
 *     &lt;enumeration value="c"/>
 *     &lt;enumeration value="d"/>
 *     &lt;enumeration value="e"/>
 *     &lt;enumeration value="f"/>
 *     &lt;enumeration value="g"/>
 *     &lt;enumeration value="a_plus"/>
 *     &lt;enumeration value="a_plus_plus"/>
 *     &lt;enumeration value="a_plus_plus_plus"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "EnergyLabelEfficiencyClass")
@XmlEnum
public enum EnergyLabelEfficiencyClass {

    @XmlEnumValue("a")
    A("a"),
    @XmlEnumValue("b")
    B("b"),
    @XmlEnumValue("c")
    C("c"),
    @XmlEnumValue("d")
    D("d"),
    @XmlEnumValue("e")
    E("e"),
    @XmlEnumValue("f")
    F("f"),
    @XmlEnumValue("g")
    G("g"),
    @XmlEnumValue("a_plus")
    A_PLUS("a_plus"),
    @XmlEnumValue("a_plus_plus")
    A_PLUS_PLUS("a_plus_plus"),
    @XmlEnumValue("a_plus_plus_plus")
    A_PLUS_PLUS_PLUS("a_plus_plus_plus");
    private final String value;

    EnergyLabelEfficiencyClass(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EnergyLabelEfficiencyClass fromValue(String v) {
        for (EnergyLabelEfficiencyClass c: EnergyLabelEfficiencyClass.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
