package org.jeap.devdb.entity.dev;

// Generated 2015-10-31 11:27:16 by Hibernate Tools 3.4.0.CR1

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.GenericGenerator;

/**
 * TDevModule generated by hbm2java
 */
@Entity
//@Table(name = "t_dev_module", catalog = "db_jeap_dev", uniqueConstraints = @UniqueConstraint(columnNames = {
//		"sys_code", "full_code" }))
@Table(name = "t_dev_module", uniqueConstraints = @UniqueConstraint(columnNames = {"sys_code", "full_code" }))
public class TDevModule implements java.io.Serializable {

	private String id;
	private TDevModule TDevModule;
	private TDevSystem TDevSystem;
	private String fullCode;
	private String code;
	private String name;
	private Integer seq;
	private String notes;
	private String CUid;
	private String CName;
	private Date CTime;
	private String MUid;
	private String MName;
	private Date MTime;
	private List<TDevEntity> TDevEntities = new ArrayList<TDevEntity>(0);
	private List<TDevModule> TDevModules = new ArrayList<TDevModule>(0);

	public TDevModule() {
	}

/*	public TDevModule(String id, TDevSystem TDevSystem, String fullCode,
			String code, String name) {
		this.id = id;
		this.TDevSystem = TDevSystem;
		this.fullCode = fullCode;
		this.code = code;
		this.name = name;
	}

	public TDevModule(String id, TDevModule TDevModule, TDevSystem TDevSystem,
			String fullCode, String code, String name, Integer seq,
			String notes, String CUid, String CName, Date CTime, String MUid,
			String MName, Date MTime, List<TDevEntity> TDevEntities,
			Set<TDevModule> TDevModules) {
		this.id = id;
		this.TDevModule = TDevModule;
		this.TDevSystem = TDevSystem;
		this.fullCode = fullCode;
		this.code = code;
		this.name = name;
		this.seq = seq;
		this.notes = notes;
		this.CUid = CUid;
		this.CName = CName;
		this.CTime = CTime;
		this.MUid = MUid;
		this.MName = MName;
		this.MTime = MTime;
		this.TDevEntities = TDevEntities;
		this.TDevModules = TDevModules;
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
	@JoinColumn(name = "parent_id")
	public TDevModule getTDevModule() {
		return this.TDevModule;
	}

	public void setTDevModule(TDevModule TDevModule) {
		this.TDevModule = TDevModule;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sys_code",referencedColumnName="code", nullable = false)
	public TDevSystem getTDevSystem() {
		return this.TDevSystem;
	}

	public void setTDevSystem(TDevSystem TDevSystem) {
		this.TDevSystem = TDevSystem;
	}

	@Column(name = "full_code", nullable = false)
	public String getFullCode() {
		return this.fullCode;
	}

	public void setFullCode(String fullCode) {
		this.fullCode = fullCode;
	}

	@Column(name = "code", nullable = false, length = 50)
	public String getCode() {
		return this.code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Column(name = "name", nullable = false, length = 50)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "seq")
	public Integer getSeq() {
		return this.seq;
	}

	public void setSeq(Integer seq) {
		this.seq = seq;
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

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "TDevModule")
	public List<TDevEntity> getTDevEntities() {
		return this.TDevEntities;
	}

	public void setTDevEntities( List<TDevEntity> TDevEntities) {
		this.TDevEntities = TDevEntities;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "TDevModule")
	public List<TDevModule> getTDevModules() {
		return this.TDevModules;
	}

	public void setTDevModules( List<TDevModule> TDevModules) {
		this.TDevModules = TDevModules;
	}

}