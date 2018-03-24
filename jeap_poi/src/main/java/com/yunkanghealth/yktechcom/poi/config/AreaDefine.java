package com.yunkanghealth.yktechcom.poi.config;

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
// @XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {})
public class AreaDefine {
	private static final Log log = LogFactory.getLog(BookDefine.class);

	private String id;
	private String name;
	private String type;
	private String fillModel;
	private String beginCell;
	private String endCell;
	private String range;
	private String variable;
	private int titleRow;
	private int titleRowNum;

	private int tplRow;
	private int tplRowNum;

	private int dataBeginRow;
	private int dataEndRow;
	private String notNullColumn;
	private int notNullColNum = -1;

	// private List<RowDefine> titleDefineList;
	private List<RowDefine> rowDefineList;
	private List<ColumnDefine> columnDefineList;

	private RangePosition position;
	private VariableAppoint areaVarAppoint;

	private int dataRowNum;

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

	public String getType() {
		return type;
	}

	@XmlAttribute
	public void setType(String type) {
		this.type = type;
	}

	public String getRange() {
		return range;
	}

	@XmlAttribute
	public void setRange(String range) {
		this.range = range;
		if (this.position == null) {
			this.position = new RangePosition();
		}
		this.position.setRangeStr(range);
	}

	public String getFillModel() {
		return fillModel;
	}

	@XmlAttribute
	public void setFillModel(String fillModel) {
		this.fillModel = fillModel;
	}

	public String getBeginCell() {
		return beginCell;
	}

	@XmlAttribute
	public void setBeginCell(String beginCell) {
		this.beginCell = beginCell;

		if (this.position == null) {
			this.position = new RangePosition();
		}
		this.position.setBeginCell(beginCell);
	}

	public String getEndCell() {
		return endCell;
	}

	@XmlAttribute
	public void setEndCell(String endCell) {
		this.endCell = endCell;

		if (this.position == null) {
			this.position = new RangePosition();
		}
		this.position.setEndCell(endCell);
	}

	public String getVariable() {
		return variable;
	}

	@XmlAttribute
	public void setVariable(String variable) {
		this.variable = variable;
	}

	public int getTitleRow() {
		return titleRow;
	}

	@XmlAttribute
	public void setTitleRow(int titleRow) {
		this.titleRow = titleRow;
	}

	public int getTitleRowNum() {
		return titleRowNum;
	}

	@XmlAttribute
	public void setTitleRowNum(int titleRowNum) {
		this.titleRowNum = titleRowNum;
	}

	public int getNotNullColNum() {
		return notNullColNum;
	}

	public String getNotNullColumn() {
		return notNullColumn;
	}

	@XmlAttribute
	public void setNotNullColumn(String notNullColumn) {
		this.notNullColumn = notNullColumn;
		Position position = new Position(notNullColumn);
		this.notNullColNum = position.getColumn();
	}

	public int getTplRow() {
		if (tplRow == 0) {
			tplRow = this.position.getBegRow() + this.dataBeginRow;
		}
		return tplRow;
	}

	@XmlAttribute
	public void setTplRow(int tplRow) {
		this.tplRow = tplRow;
	}

	public int getTplRowNum() {
		if (tplRowNum == 0) {
			tplRowNum = this.getDataRowNum();
		}
		return tplRowNum;
	}

	@XmlAttribute
	public void setTplRowNum(int tplRowNum) {
		this.tplRowNum = tplRowNum;
	}

	public int getDataBeginRow() {
		return dataBeginRow;
	}

	@XmlAttribute
	public void setDataBeginRow(int dataBeginRow) {
		this.dataBeginRow = dataBeginRow;
	}

	public int getDataEndRow() {
		return dataEndRow;
	}

	@XmlAttribute
	public void setDataEndRow(int dataEndRow) {
		this.dataEndRow = dataEndRow;
	}

	public List<RowDefine> getRowDefineList() {
		return rowDefineList;
	}

	// @XmlElementWrapper(name = "Rows")
	@XmlElement(name = "Row")
	public void setRowDefineList(List<RowDefine> rowDefineList) {
		this.rowDefineList = rowDefineList;
	}

	// @XmlElementWrapper(name = "Columns")
	@XmlElement(name = "Column")
	public List<ColumnDefine> getColumnDefineList() {
		return columnDefineList;
	}

	public void setColumnDefineList(List<ColumnDefine> columnDefineList) {
		this.columnDefineList = columnDefineList;
	}

	public RangePosition getPosition() {
		return position;
	}

	public VariableAppoint getAreaVarAppoint() {
		return areaVarAppoint;
	}

	public int getDataRowNum() {
		return this.dataRowNum;
	}

	public void resetDefine(Map<String, VariableDefine> variablesMap) {
		parserAreaDefaultVar(variablesMap);

		VariableDefine defaultVarDefine = null;
		if (this.areaVarAppoint != null) {
			defaultVarDefine = this.areaVarAppoint.getVariableDefine();
		}

		int firstRowColDefineRow = 0;
		if (this.rowDefineList != null) {
			int firstRowDefineRow = this.rowDefineList.get(0).getPosition()
					.getBegRow();
			if (firstRowDefineRow > 0) {
				firstRowColDefineRow = firstRowDefineRow;
			}
		}
		if (this.columnDefineList != null) {
			int firstColDefineRow = this.columnDefineList.get(0).getPosition()
					.getBegRow();
			if (firstColDefineRow > 0
					&& firstColDefineRow < firstRowColDefineRow) {
				firstRowColDefineRow = firstColDefineRow;
			}
		}

		if (this.rowDefineList != null) {
			for (int index = 0; index < this.rowDefineList.size(); index++) {
				RowDefine rowDefine = this.rowDefineList.get(index);
				//log.debug("Row=" + rowDefine.getNo());
				rowDefine.resetDefine(variablesMap, defaultVarDefine);
				resetRelativeRowNo(firstRowColDefineRow, rowDefine, index);
			}
		}

		if (this.columnDefineList != null) {
			for (int index = 0; index < this.columnDefineList.size(); index++) {
				ColumnDefine colDefine = this.columnDefineList.get(index);
				//log.debug("Row=" + colDefine.getNo());
				colDefine.resetDefine(variablesMap, defaultVarDefine);
				resetRelativeRowNo(firstRowColDefineRow, colDefine, index);
			}
		}
		log.debug("areaBegRow=" + this.getPosition().getBegRow()
				+ "; areaEndRow=" + this.getPosition().getEndRow()
				+ "; dataBegRow=" + (this.getPosition().getBegRow()
				+ this.getDataBeginRow()) + "; dataRowNum="
				+ this.getDataRowNum() + "; dataEndRow="
				+ (this.getPosition().getEndRow() + this.getDataEndRow())
				+ "; tplRow=" + this.getTplRow() + "; tplRowNum="
				+ this.getTplRowNum());
	}

	private void resetRelativeRowNo(int firstRowColDefineRow,
			RowDefine rowDefine, int defineIdx) {
		int relativeRow = 0;
		int areaDefineBegRow = this.getPosition().getBegRow();
		int rowDefineBegRow = rowDefine.getPosition().getBegRow();

		int dataBegRow = this.getDataBeginRow();
		if(dataBegRow == 0 ){
			relativeRow = rowDefineBegRow - areaDefineBegRow;
		}else{
			relativeRow = rowDefineBegRow - firstRowColDefineRow;
		}
		rowDefine.setRelativeRow(relativeRow);

		int currDataRowNum = 1;
		int rowDefineEndRow = rowDefine.getPosition().getEndRow();
		if (rowDefineEndRow == 0) {
			currDataRowNum = relativeRow + 1;
		} else {
			currDataRowNum = relativeRow + rowDefineEndRow - rowDefineBegRow
					+ 1;
		}
		if (currDataRowNum > this.dataRowNum) {
			this.dataRowNum = currDataRowNum;
		}
	}

	private void resetRelativeRowNo(int firstRowColDefineRow,
			ColumnDefine colDefine, int defineIdx) {
		/**
		int relativeRow = 0;
		int defineBegRow = colDefine.getPosition().getBegRow();
		int areaDataBegRow = this.getPosition().getBegRow()
				+ this.getDataBeginRow();

		relativeRow = defineBegRow - areaDataBegRow;
		colDefine.setRelativeRow(relativeRow);
		
		int currDataRowNum = 0;
		int defineEndRow = colDefine.getPosition().getBegRow();
		if (defineEndRow == 0) {
			currDataRowNum = relativeRow + 1;
		} else {
			currDataRowNum = relativeRow + defineEndRow - defineBegRow + 1;
		}
		if (currDataRowNum > this.dataRowNum) {
			this.dataRowNum = currDataRowNum;
		}
		**/
		
		int relativeRow = 0;
		int areaDefineBegRow = this.getPosition().getBegRow();
		int colDefineBegRow = colDefine.getPosition().getBegRow();

		int dataBegRow = this.getDataBeginRow();
		if(dataBegRow == 0 ){
			relativeRow = colDefineBegRow - areaDefineBegRow;
		}else{
			relativeRow = colDefineBegRow - firstRowColDefineRow;
		}
		colDefine.setRelativeRow(relativeRow);

		int currDataRowNum = 1;
		int colDefineEndRow = colDefine.getPosition().getEndRow();
		if (colDefineEndRow == 0) {
			currDataRowNum = relativeRow + 1;
		} else {
			currDataRowNum = relativeRow + colDefineEndRow - colDefineBegRow
					+ 1;
		}
		if (currDataRowNum > this.dataRowNum) {
			this.dataRowNum = currDataRowNum;
		}
	}

	private void parserAreaDefaultVar(Map<String, VariableDefine> variablesMap) {
		VariableAppoint varAppoint = null;
		String configStr = this.variable;

		if (!JeapStringUtil.isEmpty(configStr)) {
			varAppoint = new VariableAppoint(variablesMap);
			varAppoint.parserVariable(configStr);
			log.debug("AreaDefaultVar=" + varAppoint.dumpStr());
		}

		this.areaVarAppoint = varAppoint;
	}
}