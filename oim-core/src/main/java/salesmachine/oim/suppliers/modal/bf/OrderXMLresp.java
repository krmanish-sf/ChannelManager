//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.31 at 04:32:17 PM IST 
//


package salesmachine.oim.suppliers.modal.bf;

import java.util.ArrayList;
import java.util.List;
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
 *         &lt;element name="order">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                   &lt;element name="shipping">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="ship_to" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="ship_contact" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="ship_add" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *                             &lt;element name="ship_city" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="ship_state" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="ship_zip" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="processing">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="po_num" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                             &lt;element name="exp" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="ship_via" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="ship_acct" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="inv_notes" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="inv_num" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                             &lt;element name="xml_conf" type="{http://www.w3.org/2001/XMLSchema}short"/>
 *                             &lt;element name="xml_action" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="totals">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="sub_total" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *                             &lt;element name="disc" type="{http://www.w3.org/2001/XMLSchema}byte"/>
 *                             &lt;element name="handling" type="{http://www.w3.org/2001/XMLSchema}byte"/>
 *                             &lt;element name="exp_fee" type="{http://www.w3.org/2001/XMLSchema}byte"/>
 *                             &lt;element name="ship_amt" type="{http://www.w3.org/2001/XMLSchema}byte"/>
 *                             &lt;element name="inv_total" type="{http://www.w3.org/2001/XMLSchema}float"/>
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
 *         &lt;element name="items">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="item" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="vend_id" type="{http://www.w3.org/2001/XMLSchema}short"/>
 *                             &lt;element name="item_id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="qty" type="{http://www.w3.org/2001/XMLSchema}byte"/>
 *                             &lt;element name="action" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="price" type="{http://www.w3.org/2001/XMLSchema}float"/>
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
    "order",
    "items"
})
@XmlRootElement(name = "orderxmlresp")
public class OrderXMLresp {

    @XmlElement(required = true)
    protected OrderXMLresp.Order order;
    @XmlElement(required = true)
    protected OrderXMLresp.Items items;

    /**
     * Gets the value of the order property.
     * 
     * @return
     *     possible object is
     *     {@link OrderXMLresp.Order }
     *     
     */
    public OrderXMLresp.Order getOrder() {
        return order;
    }

    /**
     * Sets the value of the order property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrderXMLresp.Order }
     *     
     */
    public void setOrder(OrderXMLresp.Order value) {
        this.order = value;
    }

    /**
     * Gets the value of the items property.
     * 
     * @return
     *     possible object is
     *     {@link OrderXMLresp.Items }
     *     
     */
    public OrderXMLresp.Items getItems() {
        return items;
    }

    /**
     * Sets the value of the items property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrderXMLresp.Items }
     *     
     */
    public void setItems(OrderXMLresp.Items value) {
        this.items = value;
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
     *         &lt;element name="item" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="vend_id" type="{http://www.w3.org/2001/XMLSchema}short"/>
     *                   &lt;element name="item_id" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="qty" type="{http://www.w3.org/2001/XMLSchema}byte"/>
     *                   &lt;element name="action" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="price" type="{http://www.w3.org/2001/XMLSchema}float"/>
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
        "item"
    })
    public static class Items {

        protected List<OrderXMLresp.Items.Item> item;

        /**
         * Gets the value of the item property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the item property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getItem().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link OrderXMLresp.Items.Item }
         * 
         * 
         */
        public List<OrderXMLresp.Items.Item> getItem() {
            if (item == null) {
                item = new ArrayList<OrderXMLresp.Items.Item>();
            }
            return this.item;
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
         *         &lt;element name="vend_id" type="{http://www.w3.org/2001/XMLSchema}short"/>
         *         &lt;element name="item_id" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *         &lt;element name="qty" type="{http://www.w3.org/2001/XMLSchema}byte"/>
         *         &lt;element name="action" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *         &lt;element name="price" type="{http://www.w3.org/2001/XMLSchema}float"/>
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
            "vendId",
            "itemId",
            "qty",
            "action",
            "price"
        })
        public static class Item {

            @XmlElement(name = "vend_id")
            protected short vendId;
            @XmlElement(name = "item_id", required = true)
            protected String itemId;
            protected byte qty;
            @XmlElement(required = true)
            protected String action;
            protected float price;

            /**
             * Gets the value of the vendId property.
             * 
             */
            public short getVendId() {
                return vendId;
            }

            /**
             * Sets the value of the vendId property.
             * 
             */
            public void setVendId(short value) {
                this.vendId = value;
            }

            /**
             * Gets the value of the itemId property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getItemId() {
                return itemId;
            }

            /**
             * Sets the value of the itemId property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setItemId(String value) {
                this.itemId = value;
            }

            /**
             * Gets the value of the qty property.
             * 
             */
            public byte getQty() {
                return qty;
            }

            /**
             * Sets the value of the qty property.
             * 
             */
            public void setQty(byte value) {
                this.qty = value;
            }

            /**
             * Gets the value of the action property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getAction() {
                return action;
            }

            /**
             * Sets the value of the action property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setAction(String value) {
                this.action = value;
            }

            /**
             * Gets the value of the price property.
             * 
             */
            public float getPrice() {
                return price;
            }

            /**
             * Sets the value of the price property.
             * 
             */
            public void setPrice(float value) {
                this.price = value;
            }

        }

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
     *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *         &lt;element name="shipping">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="ship_to" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="ship_contact" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="ship_add" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
     *                   &lt;element name="ship_city" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="ship_state" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="ship_zip" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="processing">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="po_num" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *                   &lt;element name="exp" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="ship_via" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="ship_acct" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="inv_notes" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="inv_num" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *                   &lt;element name="xml_conf" type="{http://www.w3.org/2001/XMLSchema}short"/>
     *                   &lt;element name="xml_action" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="totals">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="sub_total" type="{http://www.w3.org/2001/XMLSchema}float"/>
     *                   &lt;element name="disc" type="{http://www.w3.org/2001/XMLSchema}byte"/>
     *                   &lt;element name="handling" type="{http://www.w3.org/2001/XMLSchema}byte"/>
     *                   &lt;element name="exp_fee" type="{http://www.w3.org/2001/XMLSchema}byte"/>
     *                   &lt;element name="ship_amt" type="{http://www.w3.org/2001/XMLSchema}byte"/>
     *                   &lt;element name="inv_total" type="{http://www.w3.org/2001/XMLSchema}float"/>
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
        "id",
        "shipping",
        "processing",
        "totals"
    })
    public static class Order {

        protected int id;
        @XmlElement(required = true)
        protected OrderXMLresp.Order.Shipping shipping;
        @XmlElement(required = true)
        protected OrderXMLresp.Order.Processing processing;
        @XmlElement(required = true)
        protected OrderXMLresp.Order.Totals totals;

        /**
         * Gets the value of the id property.
         * 
         */
        public int getId() {
            return id;
        }

        /**
         * Sets the value of the id property.
         * 
         */
        public void setId(int value) {
            this.id = value;
        }

        /**
         * Gets the value of the shipping property.
         * 
         * @return
         *     possible object is
         *     {@link OrderXMLresp.Order.Shipping }
         *     
         */
        public OrderXMLresp.Order.Shipping getShipping() {
            return shipping;
        }

        /**
         * Sets the value of the shipping property.
         * 
         * @param value
         *     allowed object is
         *     {@link OrderXMLresp.Order.Shipping }
         *     
         */
        public void setShipping(OrderXMLresp.Order.Shipping value) {
            this.shipping = value;
        }

        /**
         * Gets the value of the processing property.
         * 
         * @return
         *     possible object is
         *     {@link OrderXMLresp.Order.Processing }
         *     
         */
        public OrderXMLresp.Order.Processing getProcessing() {
            return processing;
        }

        /**
         * Sets the value of the processing property.
         * 
         * @param value
         *     allowed object is
         *     {@link OrderXMLresp.Order.Processing }
         *     
         */
        public void setProcessing(OrderXMLresp.Order.Processing value) {
            this.processing = value;
        }

        /**
         * Gets the value of the totals property.
         * 
         * @return
         *     possible object is
         *     {@link OrderXMLresp.Order.Totals }
         *     
         */
        public OrderXMLresp.Order.Totals getTotals() {
            return totals;
        }

        /**
         * Sets the value of the totals property.
         * 
         * @param value
         *     allowed object is
         *     {@link OrderXMLresp.Order.Totals }
         *     
         */
        public void setTotals(OrderXMLresp.Order.Totals value) {
            this.totals = value;
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
         *         &lt;element name="po_num" type="{http://www.w3.org/2001/XMLSchema}int"/>
         *         &lt;element name="exp" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *         &lt;element name="ship_via" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *         &lt;element name="ship_acct" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *         &lt;element name="inv_notes" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *         &lt;element name="inv_num" type="{http://www.w3.org/2001/XMLSchema}int"/>
         *         &lt;element name="xml_conf" type="{http://www.w3.org/2001/XMLSchema}short"/>
         *         &lt;element name="xml_action" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
            "poNum",
            "exp",
            "shipVia",
            "shipAcct",
            "invNotes",
            "invNum",
            "xmlConf",
            "xmlAction"
        })
        public static class Processing {

            @XmlElement(name = "po_num")
            protected int poNum;
            @XmlElement(required = true)
            protected String exp;
            @XmlElement(name = "ship_via", required = true)
            protected String shipVia;
            @XmlElement(name = "ship_acct", required = true)
            protected String shipAcct;
            @XmlElement(name = "inv_notes", required = true)
            protected String invNotes;
            @XmlElement(name = "inv_num")
            protected int invNum;
            @XmlElement(name = "xml_conf")
            protected short xmlConf;
            @XmlElement(name = "xml_action", required = true)
            protected String xmlAction;

            /**
             * Gets the value of the poNum property.
             * 
             */
            public int getPoNum() {
                return poNum;
            }

            /**
             * Sets the value of the poNum property.
             * 
             */
            public void setPoNum(int value) {
                this.poNum = value;
            }

            /**
             * Gets the value of the exp property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getExp() {
                return exp;
            }

            /**
             * Sets the value of the exp property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setExp(String value) {
                this.exp = value;
            }

            /**
             * Gets the value of the shipVia property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getShipVia() {
                return shipVia;
            }

            /**
             * Sets the value of the shipVia property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setShipVia(String value) {
                this.shipVia = value;
            }

            /**
             * Gets the value of the shipAcct property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getShipAcct() {
                return shipAcct;
            }

            /**
             * Sets the value of the shipAcct property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setShipAcct(String value) {
                this.shipAcct = value;
            }

            /**
             * Gets the value of the invNotes property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getInvNotes() {
                return invNotes;
            }

            /**
             * Sets the value of the invNotes property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setInvNotes(String value) {
                this.invNotes = value;
            }

            /**
             * Gets the value of the invNum property.
             * 
             */
            public int getInvNum() {
                return invNum;
            }

            /**
             * Sets the value of the invNum property.
             * 
             */
            public void setInvNum(int value) {
                this.invNum = value;
            }

            /**
             * Gets the value of the xmlConf property.
             * 
             */
            public short getXmlConf() {
                return xmlConf;
            }

            /**
             * Sets the value of the xmlConf property.
             * 
             */
            public void setXmlConf(short value) {
                this.xmlConf = value;
            }

            /**
             * Gets the value of the xmlAction property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getXmlAction() {
                return xmlAction;
            }

            /**
             * Sets the value of the xmlAction property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setXmlAction(String value) {
                this.xmlAction = value;
            }

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
         *         &lt;element name="ship_to" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *         &lt;element name="ship_contact" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *         &lt;element name="ship_add" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
         *         &lt;element name="ship_city" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *         &lt;element name="ship_state" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *         &lt;element name="ship_zip" type="{http://www.w3.org/2001/XMLSchema}int"/>
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
            "shipTo",
            "shipContact",
            "shipAdd",
            "shipCity",
            "shipState",
            "shipZip"
        })
        public static class Shipping {

            @XmlElement(name = "ship_to", required = true)
            protected String shipTo;
            @XmlElement(name = "ship_contact", required = true)
            protected String shipContact;
            @XmlElement(name = "ship_add")
            protected List<String> shipAdd;
            @XmlElement(name = "ship_city", required = true)
            protected String shipCity;
            @XmlElement(name = "ship_state", required = true)
            protected String shipState;
            @XmlElement(name = "ship_zip")
            protected int shipZip;

            /**
             * Gets the value of the shipTo property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getShipTo() {
                return shipTo;
            }

            /**
             * Sets the value of the shipTo property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setShipTo(String value) {
                this.shipTo = value;
            }

            /**
             * Gets the value of the shipContact property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getShipContact() {
                return shipContact;
            }

            /**
             * Sets the value of the shipContact property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setShipContact(String value) {
                this.shipContact = value;
            }

            /**
             * Gets the value of the shipAdd property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the shipAdd property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getShipAdd().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link String }
             * 
             * 
             */
            public List<String> getShipAdd() {
                if (shipAdd == null) {
                    shipAdd = new ArrayList<String>();
                }
                return this.shipAdd;
            }

            /**
             * Gets the value of the shipCity property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getShipCity() {
                return shipCity;
            }

            /**
             * Sets the value of the shipCity property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setShipCity(String value) {
                this.shipCity = value;
            }

            /**
             * Gets the value of the shipState property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getShipState() {
                return shipState;
            }

            /**
             * Sets the value of the shipState property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setShipState(String value) {
                this.shipState = value;
            }

            /**
             * Gets the value of the shipZip property.
             * 
             */
            public int getShipZip() {
                return shipZip;
            }

            /**
             * Sets the value of the shipZip property.
             * 
             */
            public void setShipZip(int value) {
                this.shipZip = value;
            }

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
         *         &lt;element name="sub_total" type="{http://www.w3.org/2001/XMLSchema}float"/>
         *         &lt;element name="disc" type="{http://www.w3.org/2001/XMLSchema}byte"/>
         *         &lt;element name="handling" type="{http://www.w3.org/2001/XMLSchema}byte"/>
         *         &lt;element name="exp_fee" type="{http://www.w3.org/2001/XMLSchema}byte"/>
         *         &lt;element name="ship_amt" type="{http://www.w3.org/2001/XMLSchema}byte"/>
         *         &lt;element name="inv_total" type="{http://www.w3.org/2001/XMLSchema}float"/>
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
            "subTotal",
            "disc",
            "handling",
            "expFee",
            "shipAmt",
            "invTotal"
        })
        public static class Totals {

            @XmlElement(name = "sub_total")
            protected float subTotal;
            protected byte disc;
            protected byte handling;
            @XmlElement(name = "exp_fee")
            protected byte expFee;
            @XmlElement(name = "ship_amt")
            protected byte shipAmt;
            @XmlElement(name = "inv_total")
            protected float invTotal;

            /**
             * Gets the value of the subTotal property.
             * 
             */
            public float getSubTotal() {
                return subTotal;
            }

            /**
             * Sets the value of the subTotal property.
             * 
             */
            public void setSubTotal(float value) {
                this.subTotal = value;
            }

            /**
             * Gets the value of the disc property.
             * 
             */
            public byte getDisc() {
                return disc;
            }

            /**
             * Sets the value of the disc property.
             * 
             */
            public void setDisc(byte value) {
                this.disc = value;
            }

            /**
             * Gets the value of the handling property.
             * 
             */
            public byte getHandling() {
                return handling;
            }

            /**
             * Sets the value of the handling property.
             * 
             */
            public void setHandling(byte value) {
                this.handling = value;
            }

            /**
             * Gets the value of the expFee property.
             * 
             */
            public byte getExpFee() {
                return expFee;
            }

            /**
             * Sets the value of the expFee property.
             * 
             */
            public void setExpFee(byte value) {
                this.expFee = value;
            }

            /**
             * Gets the value of the shipAmt property.
             * 
             */
            public byte getShipAmt() {
                return shipAmt;
            }

            /**
             * Sets the value of the shipAmt property.
             * 
             */
            public void setShipAmt(byte value) {
                this.shipAmt = value;
            }

            /**
             * Gets the value of the invTotal property.
             * 
             */
            public float getInvTotal() {
                return invTotal;
            }

            /**
             * Sets the value of the invTotal property.
             * 
             */
            public void setInvTotal(float value) {
                this.invTotal = value;
            }

        }

    }

}
