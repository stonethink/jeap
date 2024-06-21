package com.jeap.poi.config;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jeap.tools.util.JeapStringUtil;

@XmlAccessorType(XmlAccessType.PROPERTY)
// @XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {})
public class RowDefine {
	private static final Log log = LogFactory.getLog(RowDefine.class);

	private String id;
	private String no;
	private String type;
	// private String fillModel;
	// private String range;
	private String beginCell;
	private String endCell;
	private String variable;
	private List<CellDefine> cellDefineList;

	private RangePosition position;
	private VariableAppoint varAppoint;

	//private int deltaRow = 0;
	private int relativeRow = 0;

	public String getId() {
		return id;
	}

	@XmlAttribute
	public void setId(String id) {
		this.id = id;
	}

	public String getNo() {
		return no;
	}

	@XmlAttribute
	public void setNo(String no) {
		this.no = no;
		int rowNo = Integer.parseInt(no);
		this.position = new RangePosition();
		this.position.setRowPosition(rowNo);
	}

	public String getType() {
		return type;
	}

	@XmlAttribute
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * public String getRange() { return range; }
	 * 
	 * @XmlAttribute public void setRange(String range) { this.range = range; if
	 *               (this.position == null) { this.position = new
	 *               RangePosition(); } this.position.setRangeStr(range); }
	 **/

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

	public List<CellDefine> getCellDefineList() {
		return cellDefineList;
	}

	// @XmlElementWrapper(name = "Cells")
	@XmlElement(name = "Cell")
	// @XmlMixed
	// @XmlElementRef(type=CellDefine.class, name="Cell")
	public void setCellDefineList(List<CellDefine> cellDefineList) {
		this.cellDefineList = cellDefineList;
	}

	public int getRelativeRow() {
		return relativeRow;
	}

	@XmlTransient
	public void setRelativeRow(int relativeRow) {
		this.relativeRow = relativeRow;
	}

	public RangePosition getPosition() {
		return position;
	}

	public VariableAppoint getVarAppoint() {
		return varAppoint;
	}

	// reset Define
	public void resetDefine(Map<String, VariableDefine> variablesMap,
			VariableDefine areaDefaultVarDefine) {
		parserDefaultVarAppoint(variablesMap);

		VariableDefine defaultVarDefine = null;
		if (this.varAppoint != null) {
			defaultVarDefine = this.varAppoint.getVariableDefine();
		} else {
			defaultVarDefine = areaDefaultVarDefine;
		}

		resetCellsDefine(variablesMap, defaultVarDefine);
	}

	public void resetCellsDefine(Map<String, VariableDefine> variablesMap,
			VariableDefine defaultVarDefine) {

		for (CellDefine cell : this.cellDefineList) {
			//log.debug("Cell=" + cell.getPos());
			cell.resetDefine(variablesMap, defaultVarDefine);
		}
	}

	private void parserDefaultVarAppoint(
			Map<String, VariableDefine> variablesMap) {
		VariableAppoint varAppoint = null;
		String configStr = this.variable;

		if (!JeapStringUtil.isEmpty(configStr)) {
			varAppoint = new VariableAppoint(variablesMap);
			varAppoint.parserVariable(configStr);
			log.debug("RowVar=" + varAppoint.dumpStr());
		}

		this.varAppoint = varAppoint;
	}
}
