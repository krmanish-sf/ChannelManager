//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.09.24 at 05:19:36 PM IST 
//


package salesmachine.oim.suppliers.modal.el.tracking;

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
 *         &lt;element name="key" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="XML_Orders">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Order">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="Order_id" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                             &lt;element name="Order_customer" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="Order_shipping_cost" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
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
    "key",
    "xmlOrders"
})
@XmlRootElement(name = "ELTrackRequest")
public class ELTrackRequest {

    @XmlElement(required = true)
    protected String key;
    @XmlElement(name = "XML_Orders", required = true)
    protected ELTrackRequest.XMLOrders xmlOrders;

    /**
     * Gets the value of the key property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the value of the key property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKey(String value) {
        this.key = value;
    }

    /**
     * Gets the value of the xmlOrders property.
     * 
     * @return
     *     possible object is
     *     {@link ELTrackRequest.XMLOrders }
     *     
     */
    public ELTrackRequest.XMLOrders getXMLOrders() {
        return xmlOrders;
    }

    /**
     * Sets the value of the xmlOrders property.
     * 
     * @param value
     *     allowed object is
     *     {@link ELTrackRequest.XMLOrders }
     *     
     */
    public void setXMLOrders(ELTrackRequest.XMLOrders value) {
        this.xmlOrders = value;
    }


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
     *         &lt;element name="Order">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="Order_id" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *                   &lt;element name="Order_customer" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="Order_shipping_cost" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
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
        "order"
    })
    public static class XMLOrders {

        @XmlElement(name = "Order", required = true)
        protected ELTrackRequest.XMLOrders.Order order;

        /**
         * Gets the value of the order property.
         * 
         * @return
         *     possible object is
         *     {@link ELTrackRequest.XMLOrders.Order }
         *     
         */
        public ELTrackRequest.XMLOrders.Order getOrder() {
            return order;
        }

        /**
         * Sets the value of the order property.
         * 
         * @param value
         *     allowed object is
         *     {@link ELTrackRequest.XMLOrders.Order }
         *     
         */
        public void setOrder(ELTrackRequest.XMLOrders.Order value) {
            this.order = value;
        }


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
         *         &lt;element name="Order_id" type="{http://www.w3.org/2001/XMLSchema}int"/>
         *         &lt;element name="Order_customer" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *         &lt;element name="Order_shipping_cost" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
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
            "orderId",
            "orderCustomer",
            "orderShippingCost"
        })
        public static class Order {

            @XmlElement(name = "Order_id")
            protected int orderId;
            @XmlElement(name = "Order_customer", required = true)
            protected String orderCustomer;
            @XmlElement(name = "Order_shipping_cost")
            protected boolean orderShippingCost;

            /**
             * Gets the value of the orderId property.
             * 
             */
            public int getOrderId() {
                return orderId;
            }

            /**
             * Sets the value of the orderId property.
             * 
             */
            public void setOrderId(int value) {
                this.orderId = value;
            }

            /**
             * Gets the value of the orderCustomer property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getOrderCustomer() {
                return orderCustomer;
            }

            /**
             * Sets the value of the orderCustomer property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setOrderCustomer(String value) {
                this.orderCustomer = value;
            }

            /**
             * Gets the value of the orderShippingCost property.
             * 
             */
            public boolean isOrderShippingCost() {
                return orderShippingCost;
            }

            /**
             * Sets the value of the orderShippingCost property.
             * 
             */
            public void setOrderShippingCost(boolean value) {
                this.orderShippingCost = value;
            }

        }

    }

}
