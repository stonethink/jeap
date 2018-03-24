package org.jeap.devdb.entity.dev;

// Generated 2015-10-31 11:27:16 by Hibernate Tools 3.4.0.CR1

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;

/**
 * TDevErAttribute generated by hbm2java
 */
@Entity
//@Table(name = "t_dev_er_attribute", catalog = "db_jeap_dev")
@Table(name = "t_dev_er_attribute")
public class TDevErAttribute implements java.io.Serializable {

	private String id;
	private TDevEntRelation TDevEntRelation;
	private TDevAttribute TDevAttributeBySrcAttrId;
	private TDevAttribute TDevAttributeByDestAttrId;
	private Integer seq;
	private String srcAttrCode;
	private String destAttrCode;
	private String notes;
	private String CUid;
	private String CName;
	private Date CTime;
	private String MUid;
	private String MName;
	private Date MTime;

	public TDevErAttribute() {
	}

/*	public TDevErAttribute(String id, TDevEntRelation TDevEntRelation,
			TDevAttribute TDevAttributeBySrcAttrId,
			TDevAttribute TDevAttributeByDestAttrId) {
		this.id = id;
		this.TDevEntRelation = TDevEntRelation;
		this.TDevAttributeBySrcAttrId = TDevAttributeBySrcAttrId;
		this.TDevAttributeByDestAttrId = TDevAttributeByDestAttrId;
	}

	public TDevErAttribute(String id, TDevEntRelation TDevEntRelation,
			TDevAttribute TDevAttributeBySrcAttrId,
			TDevAttribute TDevAttributeByDestAttrId, Integer seq,
			String srcAttrCode, String destAttrCode, String notes, String CUid,
			String CName, Date CTime, String MUid, String MName, Date MTime) {
		this.id = id;
		this.TDevEntRelation = TDevEntRelation;
		this.TDevAttributeBySrcAttrId = TDevAttributeBySrcAttrId;
		this.TDevAttributeByDestAttrId = TDevAttributeByDestAttrId;
		this.seq = seq;
		this.srcAttrCode = srcAttrCode;
		this.destAttrCode = destAttrCode;
		this.notes = notes;
		this.CUid = CUid;
		this.CName = CName;
		this.CTime = CTime;
		this.MUid = MUid;
		this.MName = MName;
		this.MTime = MTime;
	}*/

	@Id
	@GeneratedValue(generator = "paymentableGenerator")
	@GenericGenerator(name = "paymentableGenerator", strategy = "uuid")
	@Column(name = "id", unique = true, nullable = false, length = 50)
	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "er_id", nullable = false)
	public TDevEntRelation getTDevEntRelation() {
		return this.TDevEntRelation;
	}

	public void setTDevEntRelation(TDevEntRelation TDevEntRelation) {
		this.TDevEntRelation = TDevEntRelation;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "src_attr_id", nullable = false)
	public TDevAttribute getTDevAttributeBySrcAttrId() {
		return this.TDevAttributeBySrcAttrId;
	}

	public void setTDevAttributeBySrcAttrId(
			TDevAttribute TDevAttributeBySrcAttrId) {
		this.TDevAttributeBySrcAttrId = TDevAttributeBySrcAttrId;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dest_attr_id", nullable = false)
	public TDevAttribute getTDevAttributeByDestAttrId() {
		return this.TDevAttributeByDestAttrId;
	}

	public void setTDevAttributeByDestAttrId(
			TDevAttribute TDevAttributeByDestAttrId) {
		this.TDevAttributeByDestAttrId = TDevAttributeByDestAttrId;
	}

	@Column(name = "seq")
	public Integer getSeq() {
		return this.seq;
	}

	public void setSeq(Integer seq) {
		this.seq = seq;
	}

	@Column(name = "src_attr_code", length = 50)
	public String getSrcAttrCode() {
		return this.srcAttrCode;
	}

	public void setSrcAttrCode(String srcAttrCode) {
		this.srcAttrCode = srcAttrCode;
	}

	@Column(name = "dest_attr_code", length = 50)
	public String getDestAttrCode() {
		return this.destAttrCode;
	}

	public void setDestAttrCode(String destAttrCode) {
		this.destAttrCode = destAttrCode;
	}

	@Column(name = "notes")
	public String getNotes() {
		return this.notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	@Column(name = "c_uid", length = 50)
	public String getCUid() {
		return this.CUid;
	}

	public void setCUid(String CUid) {
		this.CUid = CUid;
	}

	@Column(name = "c_name", length = 50)
	public String getCName() {
		return this.CName;
	}

	public void setCName(String CName) {
		this.CName = CName;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "c_time", length = 19)
	public Date getCTime() {
		return this.CTime;
	}

	public void setCTime(Date CTime) {
		this.CTime = CTime;
	}

	@Column(name = "m_uid", length = 50)
	public String getMUid() {
		return this.MUid;
	}

	public void setMUid(String MUid) {
		this.MUid = MUid;
	}

	@Column(name = "m_name", length = 50)
	public String getMName() {
		return this.MName;
	}

	public void setMName(String MName) {
		this.MName = MName;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "m_time", length = 19)
	public Date getMTime() {
		return this.MTime;
	}

	public void setMTime(Date MTime) {
		this.MTime = MTime;
	}

}
