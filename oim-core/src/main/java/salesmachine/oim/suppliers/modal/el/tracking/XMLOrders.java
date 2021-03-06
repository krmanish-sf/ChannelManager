//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.09.24 at 05:20:31 PM IST 
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
 *         &lt;element name="Order">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="response_code" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="web_order_number" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                   &lt;element name="cod_amount" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *                   &lt;element name="tracking_number" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="current_box_number" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                   &lt;element name="no_of_boxes" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                   &lt;element name="carrier_code" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="service_code" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="turns_invoice_number" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="date_shipment" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="customer_account_no" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="expected_delivery" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="shipping_cost" type="{http://www.w3.org/2001/XMLSchema}float"/>
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
@XmlRootElement(name = "XML_Orders")
public class XMLOrders {

    @XmlElement(name = "Order", required = true)
    protected XMLOrders.Order order;

    /**
     * Gets the value of the order property.
     * 
     * @return
     *     possible object is
     *     {@link XMLOrders.Order }
     *     
     */
    public XMLOrders.Order getOrder() {
        return order;
    }

    /**
     * Sets the value of the order property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLOrders.Order }
     *     
     */
    public void setOrder(XMLOrders.Order value) {
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
     *         &lt;element name="response_code" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="web_order_number" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *         &lt;element name="cod_amount" type="{http://www.w3.org/2001/XMLSchema}float"/>
     *         &lt;element name="tracking_number" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="current_box_number" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *         &lt;element name="no_of_boxes" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *         &lt;element name="carrier_code" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="service_code" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="turns_invoice_number" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="date_shipment" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="customer_account_no" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="expected_delivery" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="shipping_cost" type="{http://www.w3.org/2001/XMLSchema}float"/>
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
        "responseCode",
        "webOrderNumber",
        "codAmount",
        "trackingNumber",
        "currentBoxNumber",
        "noOfBoxes",
        "carrierCode",
        "serviceCode",
        "turnsInvoiceNumber",
        "dateShipment",
        "customerAccountNo",
        "expectedDelivery",
        "shippingCost"
    })
    public static class Order {

        @XmlElement(name = "response_code", required = true)
        protected String responseCode;
        @XmlElement(name = "web_order_number")
        protected int webOrderNumber;
        @XmlElement(name = "cod_amount")
        protected float codAmount;
        @XmlElement(name = "tracking_number", required = true)
        protected String trackingNumber;
        @XmlElement(name = "current_box_number")
        protected int currentBoxNumber;
        @XmlElement(name = "no_of_boxes")
        protected int noOfBoxes;
        @XmlElement(name = "carrier_code", required = true)
        protected String carrierCode;
        @XmlElement(name = "service_code", required = true)
        protected String serviceCode;
        @XmlElement(name = "turns_invoice_number", required = true)
        protected String turnsInvoiceNumber;
        @XmlElement(name = "date_shipment", required = true)
        protected String dateShipment;
        @XmlElement(name = "customer_account_no", required = true)
        protected String customerAccountNo;
        @XmlElement(name = "expected_delivery", required = true)
        protected String expectedDelivery;
        @XmlElement(name = "shipping_cost")
        protected float shippingCost;

        /**
         * Gets the value of the responseCode property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getResponseCode() {
            return responseCode;
        }

        /**
         * Sets the value of the responseCode property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setResponseCode(String value) {
            this.responseCode = value;
        }

        /**
         * Gets the value of the webOrderNumber property.
         * 
         */
        public int getWebOrderNumber() {
            return webOrderNumber;
        }

        /**
         * Sets the value of the webOrderNumber property.
         * 
         */
        public void setWebOrderNumber(int value) {
            this.webOrderNumber = value;
        }

        /**
         * Gets the value of the codAmount property.
         * 
         */
        public float getCodAmount() {
            return codAmount;
        }

        /**
         * Sets the value of the codAmount property.
         * 
         */
        public void setCodAmount(float value) {
            this.codAmount = value;
        }

        /**
         * Gets the value of the trackingNumber property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTrackingNumber() {
            return trackingNumber;
        }

        /**
         * Sets the value of the trackingNumber property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTrackingNumber(String value) {
            this.trackingNumber = value;
        }

        /**
         * Gets the value of the currentBoxNumber property.
         * 
         */
        public int getCurrentBoxNumber() {
            return currentBoxNumber;
        }

        /**
         * Sets the value of the currentBoxNumber property.
         * 
         */
        public void setCurrentBoxNumber(int value) {
            this.currentBoxNumber = value;
        }

        /**
         * Gets the value of the noOfBoxes property.
         * 
         */
        public int getNoOfBoxes() {
            return noOfBoxes;
        }

        /**
         * Sets the value of the noOfBoxes property.
         * 
         */
        public void setNoOfBoxes(int value) {
            this.noOfBoxes = value;
        }

        /**
         * Gets the value of the carrierCode property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getCarrierCode() {
            return carrierCode;
        }

        /**
         * Sets the value of the carrierCode property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setCarrierCode(String value) {
            this.carrierCode = value;
        }

        /**
         * Gets the value of the serviceCode property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getServiceCode() {
            return serviceCode;
        }

        /**
         * Sets the value of the serviceCode property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setServiceCode(String value) {
            this.serviceCode = value;
        }

        /**
         * Gets the value of the turnsInvoiceNumber property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTurnsInvoiceNumber() {
            return turnsInvoiceNumber;
        }

        /**
         * Sets the value of the turnsInvoiceNumber property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTurnsInvoiceNumber(String value) {
            this.turnsInvoiceNumber = value;
        }

        /**
         * Gets the value of the dateShipment property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDateShipment() {
            return dateShipment;
        }

        /**
         * Sets the value of the dateShipment property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDateShipment(String value) {
            this.dateShipment = value;
        }

        /**
         * Gets the value of the customerAccountNo property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getCustomerAccountNo() {
            return customerAccountNo;
        }

        /**
         * Sets the value of the customerAccountNo property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setCustomerAccountNo(String value) {
            this.customerAccountNo = value;
        }

        /**
         * Gets the value of the expectedDelivery property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getExpectedDelivery() {
            return expectedDelivery;
        }

        /**
         * Sets the value of the expectedDelivery property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setExpectedDelivery(String value) {
            this.expectedDelivery = value;
        }

        /**
         * Gets the value of the shippingCost property.
         * 
         */
        public float getShippingCost() {
            return shippingCost;
        }

        /**
         * Sets the value of the shippingCost property.
         * 
         */
        public void setShippingCost(float value) {
            this.shippingCost = value;
        }

    }

}
