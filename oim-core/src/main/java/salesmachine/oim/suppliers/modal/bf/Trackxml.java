//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.04.02 at 07:54:39 PM IST 
//

package salesmachine.oim.suppliers.modal.bf;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="AccessRequest">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="XMLlickey" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="UserId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="Password" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="Version" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="tracking">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="invoice" type="{http://www.w3.org/2001/XMLSchema}short"/>
 *                   &lt;element name="user_po" type="{http://www.w3.org/2001/XMLSchema}short"/>
 *                   &lt;element name="ship_date" type="{http://www.w3.org/2001/XMLSchema}int"/>
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
@XmlType(name = "", propOrder = { "accessRequest", "tracking" })
@XmlRootElement(name = "trackxml")
public class Trackxml {

	@XmlElement(name = "AccessRequest", required = true)
	protected Trackxml.AccessRequest accessRequest;
	@XmlElement(required = true)
	protected Trackxml.Tracking tracking;

	/**
	 * Gets the value of the accessRequest property.
	 * 
	 * @return possible object is {@link Trackxml.AccessRequest }
	 * 
	 */
	public Trackxml.AccessRequest getAccessRequest() {
		return accessRequest;
	}

	/**
	 * Sets the value of the accessRequest property.
	 * 
	 * @param value
	 *            allowed object is {@link Trackxml.AccessRequest }
	 * 
	 */
	public void setAccessRequest(Trackxml.AccessRequest value) {
		this.accessRequest = value;
	}

	/**
	 * Gets the value of the tracking property.
	 * 
	 * @return possible object is {@link Trackxml.Tracking }
	 * 
	 */
	public Trackxml.Tracking getTracking() {
		return tracking;
	}

	/**
	 * Sets the value of the tracking property.
	 * 
	 * @param value
	 *            allowed object is {@link Trackxml.Tracking }
	 * 
	 */
	public void setTracking(Trackxml.Tracking value) {
		this.tracking = value;
	}

	/**
	 * <p>
	 * Java class for anonymous complex type.
	 * 
	 * <p>
	 * The following schema fragment specifies the expected content contained
	 * within this class.
	 * 
	 * <pre>
	 * &lt;complexType>
	 *   &lt;complexContent>
	 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *       &lt;sequence>
	 *         &lt;element name="XMLlickey" type="{http://www.w3.org/2001/XMLSchema}string"/>
	 *         &lt;element name="UserId" type="{http://www.w3.org/2001/XMLSchema}string"/>
	 *         &lt;element name="Password" type="{http://www.w3.org/2001/XMLSchema}string"/>
	 *         &lt;element name="Version" type="{http://www.w3.org/2001/XMLSchema}float"/>
	 *       &lt;/sequence>
	 *     &lt;/restriction>
	 *   &lt;/complexContent>
	 * &lt;/complexType>
	 * </pre>
	 * 
	 * 
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = { "xmLlickey", "userId", "password",
			"version" })
	public static class AccessRequest {

		@XmlElement(name = "XMLlickey", required = true)
		protected String xmLlickey;
		@XmlElement(name = "UserId", required = true)
		protected String userId;
		@XmlElement(name = "Password", required = true)
		protected String password;
		@XmlElement(name = "Version")
		protected float version;

		/**
		 * Gets the value of the xmLlickey property.
		 * 
		 * @return possible object is {@link String }
		 * 
		 */
		public String getXMLlickey() {
			return xmLlickey;
		}

		/**
		 * Sets the value of the xmLlickey property.
		 * 
		 * @param value
		 *            allowed object is {@link String }
		 * 
		 */
		public void setXMLlickey(String value) {
			this.xmLlickey = value;
		}

		/**
		 * Gets the value of the userId property.
		 * 
		 * @return possible object is {@link String }
		 * 
		 */
		public String getUserId() {
			return userId;
		}

		/**
		 * Sets the value of the userId property.
		 * 
		 * @param value
		 *            allowed object is {@link String }
		 * 
		 */
		public void setUserId(String value) {
			this.userId = value;
		}

		/**
		 * Gets the value of the password property.
		 * 
		 * @return possible object is {@link String }
		 * 
		 */
		public String getPassword() {
			return password;
		}

		/**
		 * Sets the value of the password property.
		 * 
		 * @param value
		 *            allowed object is {@link String }
		 * 
		 */
		public void setPassword(String value) {
			this.password = value;
		}

		/**
		 * Gets the value of the version property.
		 * 
		 */
		public float getVersion() {
			return version;
		}

		/**
		 * Sets the value of the version property.
		 * 
		 */
		public void setVersion(float value) {
			this.version = value;
		}

	}

	/**
	 * <p>
	 * Java class for anonymous complex type.
	 * 
	 * <p>
	 * The following schema fragment specifies the expected content contained
	 * within this class.
	 * 
	 * <pre>
	 * &lt;complexType>
	 *   &lt;complexContent>
	 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *       &lt;sequence>
	 *         &lt;element name="invoice" type="{http://www.w3.org/2001/XMLSchema}short"/>
	 *         &lt;element name="user_po" type="{http://www.w3.org/2001/XMLSchema}short"/>
	 *         &lt;element name="ship_date" type="{http://www.w3.org/2001/XMLSchema}int"/>
	 *       &lt;/sequence>
	 *     &lt;/restriction>
	 *   &lt;/complexContent>
	 * &lt;/complexType>
	 * </pre>
	 * 
	 * 
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = { "invoice", "userPo", "shipDate" })
	public static class Tracking {

		protected String invoice;
		@XmlElement(name = "user_po")
		protected String userPo;
		@XmlElement(name = "ship_date")
		protected String shipDate;

		/**
		 * Gets the value of the invoice property.
		 * 
		 */
		public String getInvoice() {
			return invoice;
		}

		/**
		 * Sets the value of the invoice property.
		 * 
		 */
		public void setInvoice(String value) {
			this.invoice = value;
		}

		/**
		 * Gets the value of the userPo property.
		 * 
		 */
		public String getUserPo() {
			return userPo;
		}

		/**
		 * Sets the value of the userPo property.
		 * 
		 */
		public void setUserPo(String value) {
			this.userPo = value;
		}

		/**
		 * Gets the value of the shipDate property.
		 * 
		 */
		public String getShipDate() {
			return shipDate;
		}

		/**
		 * Sets the value of the shipDate property.
		 * 
		 */
		public void setShipDate(String value) {
			this.shipDate = value;
		}

	}

}
