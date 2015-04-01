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
 * <p>Java class for HumanInterfaceInputType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="HumanInterfaceInputType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="buttons"/>
 *     &lt;enumeration value="dial"/>
 *     &lt;enumeration value="handwriting_recognition"/>
 *     &lt;enumeration value="keyboard"/>
 *     &lt;enumeration value="keypad"/>
 *     &lt;enumeration value="keypad_stroke"/>
 *     &lt;enumeration value="keypad_stroke"/>
 *     &lt;enumeration value="microphone"/>
 *     &lt;enumeration value="touch_screen"/>
 *     &lt;enumeration value="touch_screen_stylus_pen"/>
 *     &lt;enumeration value="trackpoint_pointing_device"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "HumanInterfaceInputType")
@XmlEnum
public enum HumanInterfaceInputType {

    @XmlEnumValue("buttons")
    BUTTONS("buttons"),
    @XmlEnumValue("dial")
    DIAL("dial"),
    @XmlEnumValue("handwriting_recognition")
    HANDWRITING_RECOGNITION("handwriting_recognition"),
    @XmlEnumValue("keyboard")
    KEYBOARD("keyboard"),
    @XmlEnumValue("keypad")
    KEYPAD("keypad"),
    @XmlEnumValue("keypad_stroke")
    KEYPAD_STROKE("keypad_stroke"),
    @XmlEnumValue("microphone")
    MICROPHONE("microphone"),
    @XmlEnumValue("touch_screen")
    TOUCH_SCREEN("touch_screen"),
    @XmlEnumValue("touch_screen_stylus_pen")
    TOUCH_SCREEN_STYLUS_PEN("touch_screen_stylus_pen"),
    @XmlEnumValue("trackpoint_pointing_device")
    TRACKPOINT_POINTING_DEVICE("trackpoint_pointing_device");
    private final String value;

    HumanInterfaceInputType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static HumanInterfaceInputType fromValue(String v) {
        for (HumanInterfaceInputType c: HumanInterfaceInputType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
