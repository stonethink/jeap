package com.jeap.poi.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.jeap.poi.util.ExcelUtil;
import com.jeap.tools.util.BeanUtil;
import com.jeap.tools.util.JeapStringUtil;

public class SheetConfig {
	private static final Log log = LogFactory.getLog(SheetConfig.class);

	private static int DEFINE_BEG_ROW = 2;
	private static int DEFINE_BEG_COL = 2; //B?

	private static String CONFIG_DEFINESS[][] = {
			{ "配置ID", "id", "String", "BY_VAL" },
			{ "填充类型", "fillModel", "String", "BY_VAL" },
			{ "起始单元格", "beginCell", "String", "BY_REF" },
			{ "结束单元格", "endCell", "String", "BY_REF" },
			{ "非空检查列", "notNullColumn", "String", "BY_REF" },
			{ "标题起始行", "titleRow", "int", "BY_VAL" },
			{ "标题行数", "titleRowNum", "int", "BY_VAL" },
			{ "数据起始行", "dataBeginRow", "int", "BY_VAL" },
			{ "数据结束行", "dataEndRow", "int", "BY_VAL" },
			{ "模板行数", "tplRowNum", "int", "BY_VAL" },
			{ "缺省变量名", "defaultVariable", "String", "BY_VAL" } };

	//private SheetDefine sheetDefine;

	private String sheetDefineId;
	private List<AreaConfig> areaConfigList = new ArrayList<AreaConfig>();

	// construct
	public SheetConfig() {
		super();
	}

	public SheetConfig(SheetDefine sheetDefine) {
		super();
		resetAreaConfigListByDefine(sheetDefine);
	}

	public SheetConfig(Sheet sheet) {
		loadConfig(sheet);
	}

	public void shifAreaRows(int currRowNo, int shifRowNum) {
		for (AreaConfig areaConfig : this.areaConfigList) {
			RangePosition rangePosition = areaConfig.getAreaPosition();

			int begRow = rangePosition.getBegRow();
			int endRow = rangePosition.getEndRow();
			if (endRow == 0) {
				endRow = begRow;
			}
			if (begRow > currRowNo) {
				rangePosition.getBeginCell().setRow(begRow + shifRowNum);
			}
			if (endRow > currRowNo) {
				rangePosition.getEndCell().setRow(endRow + shifRowNum);
			}
		}
	}

	public void loadConfig(Sheet sheet) {
		if (sheet == null) {
			this.sheetDefineId = null;
			return;
		}
		this.sheetDefineId = getSheetDefineId(sheet);

		if (JeapStringUtil.isEmpty(this.sheetDefineId)) {
			return;
		}

		Object cellValue = null;
		int rowNo = DEFINE_BEG_ROW;
		int colNo = DEFINE_BEG_COL + 1;

		for (int colIndex = 0;; colIndex++) {
			colNo = DEFINE_BEG_COL + 1 + colIndex;
			cellValue = readCell(sheet, rowNo - 1, colNo - 1);
			if (cellValue == null || cellValue.toString() == "") {
				break;
			} else {
				loadAreaConfg(sheet, colIndex);
			}
		}
	}

	public String getSheetDefineId(Sheet sheet) {
		int rowNo = DEFINE_BEG_ROW;
		int colNo = DEFINE_BEG_COL;
		Object cellValue = readCell(sheet, rowNo - 1, colNo - 1);
		String defineId = "";
		if (cellValue == null) {
			defineId = null;
		} else {
			defineId = cellValue.toString();
		}
		return defineId;
	}

	public AreaConfig getAreaConfig(int areaIndex) {
		return this.getAreaConfigList().get(areaIndex);
	}

	private void loadAreaConfg(Sheet sheet, int colIndex) {
		AreaConfig areaConfig = new AreaConfig();

		int startRowNo = DEFINE_BEG_ROW;
		int currColNo = DEFINE_BEG_COL + 1 + colIndex;

		for (int rowIdx = 0; rowIdx < CONFIG_DEFINESS.length; rowIdx++) {
			String[] configStrs = CONFIG_DEFINESS[rowIdx];
			String propName = configStrs[1];
			//String rowNoStr = configStrs[2];
			String propType = configStrs[2];
			String cellType = configStrs[3];
			log.trace("param>> propName=" + propName
			//+ ",rowNoStr=" + rowNoStr
					+ ",propType=" + propType + ",cellType=" + cellType);

			if (JeapStringUtil.isEmpty(propName)) {
				continue;
			}
			int excelRowNo = startRowNo + rowIdx - 1;
			int excelColNo = currColNo - 1;
			Object cellValue = readCell(sheet, excelRowNo, excelColNo, cellType);
			log.debug("param>> propName=" + propName + ",propType=" + propType
					+ ",cellType=" + cellType + ";ExcelPosition[" + excelRowNo
					+ "," + excelColNo + "]; value=" + cellValue);
			try {
				BeanUtil.setPropertyValue(areaConfig, propName, cellValue);
			} catch (Exception e) {
				log.error("set areaConfig error!!!" + e.toString()
						+ ";propName=" + propName + ";cellValue=" + cellValue);
			}
		}

		if (!JeapStringUtil.isEmpty(areaConfig.getId())) {
			areaConfig.resetAreaPosition();
			areaConfigList.add(areaConfig);
		}
	}

	// getter/setter
	public String getSheetDefineId() {
		return sheetDefineId;
	}

	public void setSheetDefineId(String sheetDefineId) {
		this.sheetDefineId = sheetDefineId;
	}

	public List<AreaConfig> getAreaConfigList() {
		return areaConfigList;
	}

	// method for property
	private void resetAreaConfigListByDefine(SheetDefine sheetDefine) {
		List<AreaDefine> areaDefineList = sheetDefine.getAreaDefineList();
		for (int idx = 0; idx < areaDefineList.size(); idx++) {
			AreaDefine areaDefine = sheetDefine.getAreaDefineList().get(idx);
			AreaConfig areaConfig = new AreaConfig(areaDefine);
			areaConfig.setIndex(idx);
			this.areaConfigList.add(idx, areaConfig);
		}
	}

	private Object readCell(Sheet sheet, int excelRowNo, int excelColNo) {
		return readCell(sheet, excelRowNo, excelColNo, "BY_VAL");
	}

	private Object readCell(Sheet sheet, int excelRowNo, int excelColNo,
			String cellType) {
		Object value = null;
		Row row = null;
		Cell cell = null;
		row = sheet.getRow(excelRowNo);
		if (row == null) {
			return null;
		}
		cell = row.getCell(excelColNo);
		if (cell == null) {
			return null;
		}

		if ("BY_REF".equals(cellType)) {
			value = ExcelUtil.readCellFormula(cell);
		} else {
			value = ExcelUtil.readCellValue(cell);
		}
		return value;
	}
}
