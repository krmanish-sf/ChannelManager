//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.30 at 03:18:20 PM IST 
//


package salesmachine.oim.stores.modal.shop.order.status;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the salesmachine.oim.stores.modal.shop.order.status package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _EXTERNALTEXT_QNAME = new QName("", "EXTERNAL_TEXT");
    private final static QName _INTERNALTEXT_QNAME = new QName("", "INTERNAL_TEXT");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: salesmachine.oim.stores.modal.shop.order.status
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ADIOSSTATUS }
     * 
     */
    public ADIOSSTATUS createADIOSSTATUS() {
        return new ADIOSSTATUS();
    }

    /**
     * Create an instance of {@link ADIOSORDERSTATUSDETAIL }
     * 
     */
    public ADIOSORDERSTATUSDETAIL createADIOSORDERSTATUSDETAIL() {
        return new ADIOSORDERSTATUSDETAIL();
    }

    /**
     * Create an instance of {@link ADIOSHEADER }
     * 
     */
    public ADIOSHEADER createADIOSHEADER() {
        return new ADIOSHEADER();
    }

    /**
     * Create an instance of {@link ADIOSORDERSTATUSTRANSMISSION }
     * 
     */
    public ADIOSORDERSTATUSTRANSMISSION createADIOSORDERSTATUSTRANSMISSION() {
        return new ADIOSORDERSTATUSTRANSMISSION();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "EXTERNAL_TEXT")
    public JAXBElement<String> createEXTERNALTEXT(String value) {
        return new JAXBElement<String>(_EXTERNALTEXT_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "INTERNAL_TEXT")
    public JAXBElement<String> createINTERNALTEXT(String value) {
        return new JAXBElement<String>(_INTERNALTEXT_QNAME, String.class, null, value);
    }

}
