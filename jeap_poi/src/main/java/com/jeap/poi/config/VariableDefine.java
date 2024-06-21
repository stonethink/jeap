package com.jeap.poi.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jeap.tools.util.BeanUtil;
import com.jeap.tools.util.JeapStringUtil;

public class VariableDefine {
	private static final Log log = LogFactory.getLog(VariableDefine.class);

	public static String VAR_TYPE_LIST = "L";
	public static String VAR_TYPE_OBJECT = "O";
	public static String VAR_TYPE_PROPERTY = "P";

	public static String VAR_PRE_FLAG_LIST = ":";
	public static String VAR_PRE_FLAG_OBJECT = ".";

	private String className;
	private String varName;
	private String varFullName;

	private Map<String, String> varTranMap;

	private String varType; // List, Object, Property
	private String varPreFlag; // :,.,""
	private String groupType; // group, merge

	private List<VariableDefine> varNameList;
	//private VariableDefine lastVarDefineTypeList = null;

	private VariableDefine parent;
	private Map<String, VariableDefine> subObjects = new HashMap<String, VariableDefine>();

	public Object dataValue(Map<String, Object> outData, Object elderVardata,
			VariableDefine elderVarDefine) {
		Object value = null;

		if (elderVardata == null || elderVarDefine == null) {
			value = dataValue(outData);
		} else {
			value = dataValue(elderVardata, elderVarDefine);
		}

		return value;
	}

	public Object dataValue(Map<String, Object> outData) {
		Object varValue = null;

		List<VariableDefine> currVarNameList = this.getVarNameList();
		if (currVarNameList == null || currVarNameList.size() == 0) {
			return varValue;
		}

		for (int index = 0; index < currVarNameList.size(); index++) {
			VariableDefine currVar = currVarNameList.get(index);
			String currVarName = currVar.getVarName();
			if (index == 0) {
				varValue = outData.get(currVarName);
			} else {
				if (varValue != null && !JeapStringUtil.isEmpty(currVarName)) {
					try {
						varValue = BeanUtil.getPropertyValue(varValue,
								currVarName);
					} catch (Exception e) {
						log.error("getPropertyValue error! " + e.toString()
								+ ">> varValue=" + varValue + "currVarName="
								+ currVarName);
					}
				} else {
					varValue = null;
				}
			}
		}
		varValue = dataValueByTranMap(varValue);
		return varValue;
	}

	public Object dataValue(Object elderVardata, VariableDefine elderVarDefine) {
		Object value = null;

		if (elderVardata == null || elderVarDefine == null) {
			return value;
		}

		List<VariableDefine> elderVarNameList = elderVarDefine.getVarNameList();
		List<VariableDefine> currVarNameList = this.getVarNameList();

		if (elderVarNameList == null || currVarNameList == null
				|| currVarNameList.size() < elderVarNameList.size()) {
			return value;
		}

		boolean isSonOfElder = true;
		for (int strIdx = 0; strIdx < elderVarNameList.size(); strIdx++) {
			VariableDefine elderVar = elderVarNameList.get(strIdx);
			VariableDefine currVar = currVarNameList.get(strIdx);
			if (!elderVar.equals(currVar)) {
				isSonOfElder = false;
				break;
			}
		}
		if (isSonOfElder) {
			value = dataValue(elderVardata, elderVarNameList.size());
		}

		return value;
	}

	public Object dataValue(Object elderVardata, int elderVarNameLen) {
		Object varValue = null;

		List<VariableDefine> currVarNameList = this.getVarNameList();
		if (currVarNameList == null || currVarNameList.size() < elderVarNameLen) {
			return varValue;
		}

		varValue = elderVardata;
		for (int index = elderVarNameLen; index < currVarNameList.size(); index++) {
			VariableDefine currVar = currVarNameList.get(index);
			String currVarName = currVar.getVarName();
			if (varValue != null && !JeapStringUtil.isEmpty(currVarName)) {
				//varValue = ReflectUtil.gettValue(varValue, currVarName);
				try {
					varValue = BeanUtil.getPropertyValue(varValue, currVarName);
				} catch (Exception e) {
					log.error("getPropertyValue error! " + e.toString()
							+ ">> varValue=" + varValue + "currVarName="
							+ currVarName);
				}
			} else {
				varValue = null;
			}
		}
		varValue = dataValueByTranMap(varValue);
		return varValue;
	}

	public Object createNewObject(Map<String, Object> impData) {
		Object objValue = null;
		objValue = newObject(this);
		putValue(objValue, impData);
		return objValue;
	}

	public void putValue(Object objValue, Map<String, Object> impData) {
		VariableDefine varDefine = this;
		List<VariableDefine> varNameList = varDefine.getVarNameList();
		if (varNameList == null || impData == null) {
			return;
		}
		Object parentData = null;
		Object varData = null;
		int varNameListSize = varNameList.size();
		if (varNameListSize <= 1) {
			putValueToImpData(varDefine, impData, objValue);
		} else {
			for (int index = 0; index < varNameListSize - 1; index++) {
				VariableDefine theVarDefine = varNameList.get(index);
				if (index == 0) {
					varData = getVarData(theVarDefine, impData);
				} else {
					varData = getVarData(theVarDefine, parentData);
				}
				parentData = varData;
			}
			putValueToVarData(varDefine, parentData, objValue);
		}
	}

	@SuppressWarnings("unchecked")
	private Object getVarData(VariableDefine varDefine,
			Map<String, Object> impData) {
		Object varValue = null;
		String varName = varDefine.getVarName();
		String varType = varDefine.getVarType();
		if (impData.containsKey(varName)) {
			Object tmpVarValue = impData.get(varName);
			if (VariableDefine.VAR_TYPE_LIST.equals(varType)) {
				List<Object> listValue = (List<Object>) tmpVarValue;
				if (listValue == null) {
					listValue = new ArrayList<Object>();
					putValueToImpData(varDefine, impData, listValue);
				}
				if (listValue.size() == 0) {
					varValue = newObject(varDefine);
					listValue.add(varValue);
				} else {
					varValue = listValue.get(listValue.size() - 1);
				}
			} else {
				varValue = tmpVarValue;
			}
		} else {
			varValue = newObject(varDefine);
			putValueToImpData(varDefine, impData, varValue);
		}
		return varValue;
	}

	@SuppressWarnings("unchecked")
	private Object getVarData(VariableDefine varDefine, Object parentData) {
		Object varValue = null;
		String varName = varDefine.getVarName();
		String varType = varDefine.getVarType();

		if (parentData == null || JeapStringUtil.isEmpty(varName)) {
			log.error("parentData or varName is null! " + varDefine.dumpStr()
					+ ">> parentData=" + parentData + "; varName=" + varName);
			return varValue;
		}

		//String newObjectType = "";
		boolean isNewObject = false;
		List<Object> listValue = null;
		try {
			Object tmpVarValue = BeanUtil.getPropertyValue(parentData, varName);
			if (VariableDefine.VAR_TYPE_LIST.equals(varType)) {
				listValue = (List<Object>) tmpVarValue;
				if (listValue == null) {
					listValue = new ArrayList<Object>();
					isNewObject = true;
				} else if (listValue.size() == 0) {
					varValue = newObject(varDefine);
					listValue.add(varValue);
				} else {
					varValue = listValue.get(listValue.size() - 1);
				}
			} else {
				if (tmpVarValue == null) {
					//varValue = newObject(varDefine);
					varValue = BeanUtil
							.newObjectByProperty(parentData, varName);
					isNewObject = true;
				} else {
					varValue = tmpVarValue;
				}
			}
		} catch (Exception e) {
			log.error("getPropertyValue error! " + e.toString()
					+ ">> parentVarData=" + parentData + "; currVarName="
					+ varName);
		}

		if (isNewObject) {
			putValueToVarData(varDefine, parentData, varValue);
		}
		return varValue;
	}

	private void putValueToImpData(VariableDefine varDefine,
			Map<String, Object> impData, Object varValue) {
		String varName = varDefine.getVarName();
		String varType = varDefine.getVarType();
		
		varValue = putValueByTranMap(varValue);

		if (VariableDefine.VAR_TYPE_LIST.equals(varType)) {
			List<Object> listValue = new ArrayList<Object>();
			listValue.add(varValue);
			impData.put(varName, listValue);
		} else {
			impData.put(varName, varValue);
		}
	}

	@SuppressWarnings("unchecked")
	private void putValueToVarData(VariableDefine varDefine, Object parentData,
			Object varValue) {
		String varName = varDefine.getVarName();
		String varType = varDefine.getVarType();

		varValue = putValueByTranMap(varValue);

		List<Object> listValue = null;
		if (VariableDefine.VAR_TYPE_LIST.equals(varType)) {
			try {
				Object tmpVarValue = null;
				tmpVarValue = BeanUtil.getPropertyValue(parentData, varName);
				if (tmpVarValue == null) {
					listValue = new ArrayList<Object>();
					try {
						BeanUtil.setPropertyValue(parentData, varName,
								listValue);
					} catch (Exception e) {
						log.error("set parentData error! " + e.toString()
								+ ">> propName=" + varName + ";listValueSize="
								+ listValue.size());
					}
				} else {
					listValue = (List<Object>) tmpVarValue;
				}
				listValue.add(varValue);
			} catch (Exception e) {
				log.error("getPropertyValue error! " + e.toString()
						+ ">> parentVarData=" + parentData + "; currVarName="
						+ varName);
			}

		} else {
			try {
				BeanUtil.setPropertyValue(parentData, varName, varValue);
			} catch (Exception e) {
				log.error("set parentData error! " + e.toString()
						+ ">> propName=" + varName + ";varValue=" + varValue);
			}
		}
	}

	private Object dataValueByTranMap(Object value) {
		Object tranValue = null;
		if (varTranMap == null || varTranMap.size() == 0) {
			return value;
		}
		String key = "";
		if (value == null) {
			key = "null";
		} else {
			key = value.toString();
		}
		if (varTranMap.containsKey(key)) {
			tranValue = varTranMap.get(key);
		} else {
			tranValue = value;
		}
		return tranValue;
	}

	private Object putValueByTranMap(Object value) {
		Object tranValue = null;
		if (varTranMap == null || varTranMap.size() == 0 || value == null) {
			return value;
		}
		if (varTranMap.containsValue(value)) {
			//tranValue = varTranMap.;
			for (Entry<String, String> entry : varTranMap.entrySet()) {
				if (value.equals(entry.getValue())) {
					tranValue = entry.getKey();
					break;
				}
			}
		} else {
			tranValue = value;
		}
		return tranValue;
	}

	/**
	public void putValue(Object dataValue, Map<String, Object> impData,
			boolean isNewRecord) {
		List<VariableDefine> currVarNameList = this.getVarNameList();

		if (currVarNameList == null || impData == null) {
			return;
		}

		Object parentVarData = null;
		VariableDefine parentVarDefine = null;

		int varNameListSize = currVarNameList.size();
		for (int index = 0; index < varNameListSize; index++) {
			VariableDefine currVarDefine = currVarNameList.get(index);

			Object varValue = null;
			String varType = currVarDefine.getVarType();

			if (index < varNameListSize - 1) {
				if (VariableDefine.VAR_TYPE_LIST.equals(varType)) {
					varValue = doForVarTypeList(currVarDefine, parentVarData,
							parentVarDefine, dataValue, impData, isNewRecord);
				} else {
					varValue = doForVarTypeObject(currVarDefine, parentVarData,
							parentVarDefine, dataValue, impData, isNewRecord);
				}
				parentVarData = varValue;
				parentVarDefine = currVarDefine;
			} else { //last varName
				setValueToParentVarData(currVarDefine, parentVarData,
						parentVarDefine, dataValue, impData);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object doForVarTypeList(VariableDefine currVarDefine,
			Object parentVarData, VariableDefine parentVarDefine,
			Object dataValue, Map<String, Object> impData, boolean isNewRecord) {
		Object rtnObject = null;
		Object varValue = null;
		Object oneObjValue = null;
		List listValue = null;

		varValue = currVarDefine.dataValue(impData, parentVarData,
				parentVarDefine);
		if (varValue == null) {
			oneObjValue = newObject(currVarDefine);
			if (oneObjValue == null) {
				log.error("New Object is null!>> " + currVarDefine.dumpStr());
			} else {
				listValue = new ArrayList();
				listValue.add(oneObjValue);
				// setValue 将当前值(varValue)设置到上级对象中
				setValueToParentVarData(currVarDefine, parentVarData,
						parentVarDefine, listValue, impData);
			}
		} else {
			listValue = (List) varValue;
			if (listValue.size() == 0) {
				oneObjValue = newObject(currVarDefine);
				if (oneObjValue == null) {
					log.error("New Object is null!>> "
							+ currVarDefine.dumpStr());
				} else {
					listValue.add(oneObjValue);
				}
			} else {
				if (currVarDefine == lastVarDefineTypeList && isNewRecord) {
					oneObjValue = newObject(currVarDefine);
					if (oneObjValue == null) {
						log.error("New Object is null!>> "
								+ currVarDefine.dumpStr());
					} else {
						listValue.add(oneObjValue);
					}
				} else {
					oneObjValue = listValue.get(listValue.size() - 1);
				}
			}
		}
		rtnObject = oneObjValue;
		return rtnObject;
	}

	private Object doForVarTypeObject(VariableDefine currVarDefine,
			Object parentVarData, VariableDefine parentVarDefine,
			Object dataValue, Map<String, Object> impData, boolean isNewRecord) {
		Object varValue = null;

		//		if(isNewRecord == true && parentVarData == null){
		//			varValue = null;
		//		}else{
		varValue = currVarDefine.dataValue(impData, parentVarData,
				parentVarDefine);
		//		}
		if (varValue == null) {
			varValue = newObject(currVarDefine);
			if (varValue == null) {
				log.error("New Object is null!>> " + currVarDefine.dumpStr());
				return null;
			}
			// setValue 将当前值(varValue)设置到上级对象中
			setValueToParentVarData(currVarDefine, parentVarData,
					parentVarDefine, varValue, impData);
		}
		return varValue;
	}

	private void setValueToParentVarData(VariableDefine currVarDefine,
			Object parentVarData, VariableDefine parentVarDefine,
			Object dataValue, Map<String, Object> impData) {
		String propName = currVarDefine.getVarName();
		if (parentVarDefine == null) {
			impData.put(propName, dataValue);
		} else if (parentVarData != null) {
			//ReflectUtil.setpValue(parentVarData, propName, dataValue);
			try {
				BeanUtil.setPropertyValue(parentVarData, propName, dataValue);
			} catch (Exception e) {
				log.error("set parentVarData error! " + e.toString()
						+ ">> propName=" + propName + ";dataValue=" + dataValue);
			}
		} else {
			log.error("setpValue parentVarData is null!>> "
					+ currVarDefine.dumpStr());
		}
	}
	**/

	private Object newObject(VariableDefine currVarDefine) {
		Object objValue = null;
		String className = currVarDefine.getClassName();
		if (className != null) {
			try {
				objValue = Class.forName(className).newInstance();
			} catch (ClassNotFoundException | InstantiationException
					| IllegalAccessException e) {
				log.error(e.toString());
			}
		} else {
			log.error(currVarDefine.dumpStr() + ">> Class Name is null!!!");
		}
		return objValue;
	}

	public String dumpStr() {
		StringBuffer varSB = new StringBuffer();
		if (!JeapStringUtil.isEmpty(className)) {
			varSB.append("{");
			if (VAR_TYPE_LIST.equals(varType)) {
				varSB.append("*");
			}
			varSB.append(className);
			varSB.append("}");
		}

		varSB.append("$[");
		if (parent != null) {
			varSB.append(parent.getVarFullName());
		}
		varSB.append(varPreFlag);
		varSB.append(varName);

		if (!JeapStringUtil.isEmpty(groupType)) {
			varSB.append(",");
			varSB.append(groupType);
		}

		if (varTranMap != null) {
			varSB.append(",(");
			int idx = 0;
			for (String key : varTranMap.keySet()) {
				if (idx++ != 0) {
					varSB.append(",");
				}
				varSB.append(key);
				varSB.append("=");
				varSB.append(varTranMap.get(key));
			}
			varSB.append(")");
		}
		varSB.append("]");

		return varSB.toString();
	}

	// getter/setter
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getVarName() {
		return varName;
	}

	public void setVarName(String varName) {
		this.varName = varName;
	}

	public String getVarFullName() {
		return varFullName;
	}

	public void setVarFullName(String varFullName) {
		this.varFullName = varFullName;
	}

	public Map<String, String> getVarTranMap() {
		return varTranMap;
	}

	public void setVarTranMap(Map<String, String> varTranMap) {
		this.varTranMap = varTranMap;
	}

	public String getVarType() {
		return varType;
	}

	public void setVarType(String varType) {
		this.varType = varType;
	}

	public String getVarPreFlag() {
		return varPreFlag;
	}

	public void setVarPreFlag(String varPreFlag) {
		this.varPreFlag = varPreFlag;
	}

	public String getGroupType() {
		return groupType;
	}

	public void setGroupType(String groupType) {
		this.groupType = groupType;
	}

	public VariableDefine getParent() {
		return parent;
	}

	public void setParent(VariableDefine parent) {
		this.parent = parent;
	}

	public Map<String, VariableDefine> getSubObjects() {
		return subObjects;
	}

	public void setSubObjects(Map<String, VariableDefine> subObjects) {
		this.subObjects = subObjects;
	}

	public void setVarNameList() {
		VariableDefine currVar = this;

		varNameList = new ArrayList<VariableDefine>();
		while (currVar != null) {
			varNameList.add(0, currVar);
			currVar = currVar.getParent();
		}
		for (int index = varNameList.size() - 1; index >= 0; index--) {
			VariableDefine varDefine = varNameList.get(index);
			String varType = varDefine.getVarType();
			if (VariableDefine.VAR_TYPE_LIST.equals(varType)) {
				//this.lastVarDefineTypeList = varDefine;
				break;
			}
		}
	}

	public List<VariableDefine> getVarNameList() {
		return varNameList;
	}
}
