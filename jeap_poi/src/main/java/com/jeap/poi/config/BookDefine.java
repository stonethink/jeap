package com.jeap.poi.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jeap.tools.util.JeapStringUtil;

@XmlAccessorType(XmlAccessType.PROPERTY)  
//@XmlAccessorType(XmlAccessType.FIELD)  
@XmlRootElement(name = "Book")  
@XmlType(propOrder = {})  
public class BookDefine {
	private static final Log log = LogFactory.getLog(BookDefine.class);

	private Map<String,SheetDefine> sheetDefineMap;
	private Map<String,VariableDefine> variablesMap;

	private String id;
	private String name;
	private String importSheets;
	private String notImportSheets;

	private List<SheetDefine> sheetDefineList;
	
	public SheetDefine getSheetDefine(String defineId){
		if(sheetDefineMap == null){
			return null;
		}else{
			SheetDefine sheetDefine = sheetDefineMap.get(defineId);
			return sheetDefine;
		}
	}
	
	public boolean isImportSheet(String sheetName){
		boolean isImpSheet = false;
		if(JeapStringUtil.isEmpty(sheetName)){
			return isImpSheet;
		}
		isImpSheet = checkImportSheet(sheetName);
		if(isImpSheet == false){
			return false;
		}
		isImpSheet = checkImportSheetByNot(sheetName);
		if(isImpSheet == false){
			return false;
		}
		boolean isTplSheet = isTemplateSheet(sheetName);
		if(isTplSheet){
			return false;
		}
		return isImpSheet;
	}
	
	private boolean checkImportSheet(String sheetName){
		boolean isImpSheet = false;
		if(JeapStringUtil.isEmpty(importSheets)){
			return true;
		}
		String[] strs = importSheets.split(",");
		for(String str:strs){
			isImpSheet = JeapStringUtil.checkStrSameAs(sheetName,str);
			if(isImpSheet == true){
				return true;
			}
		}
		return isImpSheet;		
	}
	
	private boolean checkImportSheetByNot(String sheetName){ // true is import sheet
		boolean isImpSheet = true;
		if(JeapStringUtil.isEmpty(notImportSheets)){
			return true;
		}
		String[] strs = notImportSheets.split(",");
		for(String str:strs){
			boolean isNotImpSheet = JeapStringUtil.checkStrSameAs(sheetName,str);
			if(isNotImpSheet == true){
				return false;
			}
		}
		return isImpSheet;		
	}

	private boolean isTemplateSheet(String sheetName){
		boolean isImpSheet = false;
		for(SheetDefine sheetDefine:sheetDefineList){
			String defineTplSheet = sheetDefine.getTplSheet();
			if(sheetName.equalsIgnoreCase(defineTplSheet)){
				isImpSheet = true;
				break;
			}
		}
		return isImpSheet;
	}
	
	
	
	// getter/setter
	public String getId() {
		return id;
	}

	@XmlAttribute
	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	@XmlAttribute
	public void setName(String name) {
		this.name = name;
	}

	public String getImportSheets() {
		return importSheets;
	}

	@XmlAttribute
	public void setImportSheets(String importSheets) {
		this.importSheets = importSheets;
	}

	public String getNotImportSheets() {
		return notImportSheets;
	}

	@XmlAttribute
	public void setNotImportSheets(String notImportSheets) {
		this.notImportSheets = notImportSheets;
	}

	public List<SheetDefine> getSheetDefineList() {
		return sheetDefineList;
	}

	@XmlElement(name = "Sheet")
	public void setSheetDefineList(List<SheetDefine> sheetDefineList) {
		this.sheetDefineList = sheetDefineList;
	}

	public void setVariablesMap(Map<String, VariableDefine> variablesMap) {
		this.variablesMap = variablesMap;
	}

	public Map<String, VariableDefine> getVariablesMap() {
		return variablesMap;
	}
	
	// reset BookDefine
	public void resetDefine(Map<String,VariableDefine> variablesMap){
		log.debug("Begin resetDefine!!!");
		if(variablesMap == null){
			variablesMap = new HashMap<String,VariableDefine>();
		}
		setVariablesMap(variablesMap);
		
		if(sheetDefineMap == null){
			sheetDefineMap = new HashMap<String,SheetDefine>();
		}
		for(SheetDefine sheetDefine:this.sheetDefineList){
			log.debug("SheetDefine=" + sheetDefine.getId());
			sheetDefineMap.put(sheetDefine.getId(), sheetDefine);
			sheetDefine.resetDefine(getVariablesMap());
		}
		log.debug("End resetDefine!!!");
	}
}