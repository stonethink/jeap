package com.yunkanghealth.yktechcom.poi.config;

import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
//import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.yunkanghealth.yktechcom.tools.util.JeapStringUtil;

@XmlAccessorType(XmlAccessType.PROPERTY)
// @XmlAccessorType(XmlAccessType.FIELD)
// @XmlType(propOrder = {"pos","configStr"})
@XmlType(propOrder = {})
public class CellDefine {
	private static final Log log = LogFactory.getLog(CellDefine.class);

	private String id;
	private String pos;
	private String notNull;
	private String configStr;
	
	private RangePosition position;

	private VariableAppoint propertyVar;

	public String getId() {
		return id;
	}

	@XmlAttribute
	public void setId(String id) {
		this.id = id;
	}

	public String getPos() {
		return pos;
	}

	@XmlAttribute
	public void setPos(String pos) {
		this.pos = pos;
		this.position = new RangePosition(pos);
	}

	public String getNotNull() {
		return notNull;
	}

	@XmlAttribute
	public void setNotNull(String notNull) {
		if(notNull != null){
			this.notNull = notNull.toUpperCase();
		}
	}

	public String getConfigStr() {
		return configStr;
	}

	@XmlValue
	public void setConfigStr(String configStr) {
		this.configStr = configStr;
	}

	public RangePosition getPosition() {
		return position;
	}

	public VariableAppoint getPropertyVar() {
		return propertyVar;
	}

	public void resetDefine(Map<String, VariableDefine> variablesMap,
			VariableDefine areaDefaultObjDefine) {
		VariableAppoint varAppoint = null;
		String configStr = this.configStr;

		if (!JeapStringUtil.isEmpty(configStr)) {
			VariableDefine defaultObjDefine = areaDefaultObjDefine;
			boolean isProperty = true;
			
			varAppoint = new VariableAppoint(variablesMap);
			varAppoint.parserVariable(configStr,defaultObjDefine, isProperty);
			log.debug("CellPos=" + this.getPos() + "; CellVar=" + varAppoint.dumpStr());
		}
		
		this.propertyVar = varAppoint;
	}
}