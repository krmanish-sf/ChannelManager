package salesmachine.hibernatedb;

// Generated 30 Mar, 2010 7:26:10 PM by Hibernate Tools 3.2.4.GA

import java.util.HashSet;
import java.util.Set;

/**
 * OimSupplierMethodattrNames generated by hbm2java
 */
public class OimSupplierMethodattrNames implements java.io.Serializable {

	private Integer attrId;
	private String attrName;
	private Set oimSupplierMethodattrValueses = new HashSet(0);

	public OimSupplierMethodattrNames() {
	}

	public OimSupplierMethodattrNames(Integer attrId) {
		this.attrId = attrId;
	}

	public OimSupplierMethodattrNames(Integer attrId, String attrName,
			Set oimSupplierMethodattrValueses) {
		this.attrId = attrId;
		this.attrName = attrName;
		this.oimSupplierMethodattrValueses = oimSupplierMethodattrValueses;
	}

	public Integer getAttrId() {
		return this.attrId;
	}

	public void setAttrId(Integer attrId) {
		this.attrId = attrId;
	}

	public String getAttrName() {
		return this.attrName;
	}

	public void setAttrName(String attrName) {
		this.attrName = attrName;
	}

	public Set getOimSupplierMethodattrValueses() {
		return this.oimSupplierMethodattrValueses;
	}

	public void setOimSupplierMethodattrValueses(
			Set oimSupplierMethodattrValueses) {
		this.oimSupplierMethodattrValueses = oimSupplierMethodattrValueses;
	}

}
