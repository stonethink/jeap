package com.yunkanghealth.yktechcom.poi.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
//import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.yunkanghealth.yktechcom.tools.util.JeapStringUtil;

@XmlAccessorType(XmlAccessType.PROPERTY)
//@XmlAccessorType(XmlAccessType.FIELD)  
//@XmlRootElement(name = "Sheet")  
@XmlType(propOrder = {})
public class SheetDefine {
	private static final Log log = LogFactory.getLog(SheetDefine.class);

	private String id;
	private String tplSheet;
	private String repeatBy;
	private String elementName;
	private String sheetName;

	private List<AreaDefine> areaDefineList;
	private VariableAppoint repeatByVar;
	private VariableAppoint elementNameVar;

	private VariableAppoint sheetNameVar;

	private Map<String, AreaDefine> areaDefineMap = new HashMap<String, AreaDefine>();

	public AreaDefine getAreaDefine(String defineId) {
		AreaDefine areaDefine = areaDefineMap.get(defineId);
		return areaDefine;
	}

	// getter/setter
	public String getId() {
		return id;
	}

	@XmlAttribute
	public void setId(String id) {
		this.id = id;
	}

	public String getTplSheet() {
		return tplSheet;
	}

	@XmlAttribute
	public void setTplSheet(String tplSheet) {
		this.tplSheet = tplSheet;
	}

	public String getRepeatBy() {
		return repeatBy;
	}

	@XmlAttribute
	public void setRepeatBy(String repeatBy) {
		this.repeatBy = repeatBy;
	}

	public String getElementName() {
		return elementName;
	}

	@XmlAttribute
	public void setElementName(String elementName) {
		this.elementName = elementName;
	}

	public String getSheetName() {
		return sheetName;
	}

	@XmlAttribute
	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}

	public List<AreaDefine> getAreaDefineList() {
		return areaDefineList;
	}

	//@XmlElementWrapper(name = "Areas") 
	@XmlElement(name = "Area")
	public void setAreaDefineList(List<AreaDefine> areaDefineList) {
		this.areaDefineList = areaDefineList;
	}

	public VariableAppoint getRepeatByVar() {
		return repeatByVar;
	}

	public VariableAppoint getElementNameVar() {
		return elementNameVar;
	}

	public VariableAppoint getSheetNameVar() {
		return sheetNameVar;
	}

	public void resetDefine(Map<String, VariableDefine> variablesMap) {
		parserRepeatByVarDefine(variablesMap);
		parserElementNameVarDefine(variablesMap);
		parserSheetNameVarDefine(variablesMap);

		sortAreaDefineList();

		for (AreaDefine area : this.areaDefineList) {
			log.debug("AreaDefine=" + area.getId());
			areaDefineMap.put(area.getId(), area);
			area.resetDefine(variablesMap);
		}
	}

	private void sortAreaDefineList() {
		if (this.areaDefineList == null) {
			return;
		}

		List<AreaDefine> areaDefineListNew = new ArrayList<AreaDefine>();

		for (int idx = 0; idx < this.areaDefineList.size(); idx++) {
			AreaDefine currArea = this.areaDefineList.get(idx);
			RangePosition currPosition = currArea.getPosition();
			if (idx == 0) {
				areaDefineListNew.add(idx, currArea);
			} else {
				for (int serIdx = idx - 1; serIdx >= 0; serIdx--) {
					RangePosition serPosition = areaDefineListNew.get(serIdx)
							.getPosition();
					if (currPosition.getBegRow() >= serPosition.getBegRow()) {
						areaDefineListNew.add(serIdx + 1, currArea);
						break;
					}
				}
			}
		}

		this.areaDefineList = areaDefineListNew;
	}

	private void parserRepeatByVarDefine(
			Map<String, VariableDefine> variablesMap) {
		VariableAppoint varAppoint = null;
		String configStr = this.repeatBy;

		if (!JeapStringUtil.isEmpty(configStr)) {
			varAppoint = new VariableAppoint(variablesMap);
			varAppoint.parserVariable(configStr);
			log.debug("RepeatBy=" + varAppoint.dumpStr());
		}

		this.repeatByVar = varAppoint;
	}

	private void parserElementNameVarDefine(
			Map<String, VariableDefine> variablesMap) {
		VariableAppoint varAppoint = null;
		String configStr = this.elementName;

		if (!JeapStringUtil.isEmpty(configStr)) {
			varAppoint = new VariableAppoint(variablesMap);
			varAppoint.parserVariable(configStr);
			log.debug("ElementName=" + varAppoint.dumpStr());
		}
		if (varAppoint != null && varAppoint.getVariableDefine() != null) {
			String className = varAppoint.getVariableDefine().getClassName();
			if (JeapStringUtil.isEmpty(className)) {
				if (repeatByVar != null
						&& repeatByVar.getVariableDefine() != null) {
					String repeatClassName = repeatByVar.getVariableDefine()
							.getClassName();
					varAppoint.getVariableDefine()
							.setClassName(repeatClassName);
				}
			}
		}
		this.elementNameVar = varAppoint;
	}

	private void parserSheetNameVarDefine(
			Map<String, VariableDefine> variablesMap) {
		VariableAppoint varAppoint = null;
		String configStr = this.sheetName;

		if (!JeapStringUtil.isEmpty(configStr)) {
			VariableDefine defaultObjDefine = null;
			if (this.elementNameVar != null) {
				defaultObjDefine = this.elementNameVar.getVariableDefine();
			} else if (this.repeatByVar != null) {
				defaultObjDefine = this.repeatByVar.getVariableDefine();
			}
			boolean isProperty = true;

			varAppoint = new VariableAppoint(variablesMap);
			varAppoint.parserVariable(configStr, defaultObjDefine, isProperty);
			log.debug("SheetName=" + varAppoint.dumpStr());
		}

		this.sheetNameVar = varAppoint;
	}

}
