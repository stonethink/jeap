package com.yunkanghealth.yktechcom.poi.excel.importer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.yunkanghealth.yktechcom.poi.PoiConstant;
import com.yunkanghealth.yktechcom.poi.config.AreaConfig;
import com.yunkanghealth.yktechcom.poi.config.AreaDefine;
import com.yunkanghealth.yktechcom.poi.config.CellDefine;
import com.yunkanghealth.yktechcom.poi.config.ColumnDefine;
import com.yunkanghealth.yktechcom.poi.config.Position;
import com.yunkanghealth.yktechcom.poi.config.RangePosition;
import com.yunkanghealth.yktechcom.poi.config.RowDefine;
import com.yunkanghealth.yktechcom.poi.config.SheetConfig;
import com.yunkanghealth.yktechcom.poi.config.SheetDefine;
import com.yunkanghealth.yktechcom.poi.config.VariableAppoint;
import com.yunkanghealth.yktechcom.poi.config.VariableDefine;
import com.yunkanghealth.yktechcom.poi.data.IDataContext;
import com.yunkanghealth.yktechcom.poi.data.IDataOper;
import com.yunkanghealth.yktechcom.poi.data.impl.DefaultDataContext;
import com.yunkanghealth.yktechcom.poi.util.ExcelUtil;

public class SheetImporter {
	private static final Log log = LogFactory.getLog(SheetImporter.class);

	private SheetConfig sheetConfig;
	private SheetDefine sheetDefine;
	private Workbook impWorkbook;
	private Sheet impSheet;

	private IDataContext dataContext;
	private IDataOper dataOper;

	private Position currDSPosition = new Position();
	private int currAreaOneDataRowNum = 0;
	private Position currPosition = new Position();

	// construct
	public SheetImporter() {
		super();
	}

	public SheetImporter(SheetDefine sheetDefine, SheetConfig sheetConfig) {
		setSheetDefine(sheetDefine);
		setSheetConfig(sheetConfig);
	}

	public void doImport(Sheet sheet) {
		setImpSheet(sheet);

		doImport();
	}

	public void doImport() {
		if (sheetDefine == null || sheetConfig == null) {
			return;
		}
		if (impSheet == null) {
			return;
		}
		// 开始时间
		Long begTime = System.currentTimeMillis();
		log.debug("Import sheet begin:" + new Date(begTime));

		if (dataContext == null) {
			dataContext = new DefaultDataContext();
		}
		if (dataOper != null) {
			dataOper.setDataContext(dataContext);
			dataOper.preProduce();
		}

		importInitialize();

		for (AreaConfig areaConfig : sheetConfig.getAreaConfigList()) {
			String defineId = areaConfig.getId();
			AreaDefine areaDefine = sheetDefine.getAreaDefine(defineId);
			if (areaDefine == null) {
				log.error("AreaDefine is null!>> defineId=" + defineId);
				break;
			}

			String fillModel = areaDefine.getFillModel();
			log.debug("Import Area>> " + ";id=" + areaConfig.getId()
					+ ";fillModel=" + fillModel);
			if (fillModel != null) {
				fillModel = fillModel.toUpperCase();
			}

			switch (fillModel) {
			case PoiConstant.FILL_MODEL_AUTO:
				break;
			case PoiConstant.FILL_MODEL_DYNAMIC:
				impAreaDynamic(areaDefine, areaConfig);
				break;
			default: // FILL_MODEL_FIXED
				impAreaFixed(areaDefine, areaConfig);
			}
		}

		if (dataOper != null) {
			dataOper.setDataContext(dataContext);
			dataOper.produce();
		}

		importTerminate();

		if (dataOper != null) {
			dataOper.setDataContext(dataContext);
			dataOper.postProduce();
		}

		// 结束时间
		Long endTime = System.currentTimeMillis();
		log.debug("Import sheet end:" + new Date(endTime) + ";Run time:"
				+ (endTime - begTime));
	}

	private void impAreaFixed(AreaDefine areaDefine, AreaConfig areaConfig) {
		RangePosition realAreaPosition = areaConfig.getAreaPosition();
		currDSPosition.resetPosition(realAreaPosition.getBeginCell());
		int realAreaDataBegRow = areaConfig.getAreaPosition().getBegRow()
				+ areaDefine.getDataBeginRow();
		currDSPosition.setRow(realAreaDataBegRow);
		log.debug("Area=" + areaDefine.getId() + "; eachArea["
				+ currDSPosition.getRow() + "," + currDSPosition.getColumn()
				+ "]");
		currPosition.resetPosition(currDSPosition);

		Map<VariableDefine, List<VariableAppoint>> areaDataMap = new HashMap<VariableDefine, List<VariableAppoint>>();
		impRowsData(areaConfig, areaDefine, areaDataMap);
	}

	private void impAreaDynamic(AreaDefine areaDefine, AreaConfig areaConfig) {
		RangePosition realAreaPosition = areaConfig.getAreaPosition();
		currDSPosition.resetPosition(realAreaPosition.getBeginCell());
		int realAreaDataBegRow = areaConfig.getAreaPosition().getBegRow()
				+ areaDefine.getDataBeginRow();
		currDSPosition.setRow(realAreaDataBegRow);
		log.debug("Area=" + areaDefine.getId() + "; eachArea["
				+ currDSPosition.getRow() + "," + currDSPosition.getColumn()
				+ "]");
		currPosition.resetPosition(currDSPosition);

		boolean isFinished = false;
		do {
			isFinished = impAreaData(areaDefine, areaConfig);
		} while (isFinished == false);
	}

	private boolean impAreaData(AreaDefine areaDefine, AreaConfig areaConfig) {
		boolean isFinished = false;
		int oneDataBegRow = currDSPosition.getRow();
		currAreaOneDataRowNum = areaDefine.getDataRowNum();

		Map<VariableDefine, List<VariableAppoint>> areaDataMap = new HashMap<VariableDefine, List<VariableAppoint>>();

		isFinished = impRowsData(areaConfig, areaDefine, areaDataMap);
		if (isFinished == false) {
			impColumnsData(areaConfig, areaDefine, areaDataMap);
		}

		int oneDataEndRow = oneDataBegRow + currAreaOneDataRowNum - 1;
		int nextOneDataBegRow = oneDataEndRow + 1;
		currDSPosition.setRow(nextOneDataBegRow);
		return isFinished;
	}

	private boolean impRowsData(AreaConfig areaConfig, AreaDefine areaDefine,
			Map<VariableDefine, List<VariableAppoint>> areaDataMap) {
		boolean isFinished = false;
		if (areaDefine.getRowDefineList() == null) {
			isFinished = true;
			return isFinished;
		}
		VariableAppoint repeatVarAppoint = sheetDefine.getRepeatByVar();
		VariableAppoint elementVarAppoint = sheetDefine.getElementNameVar();
		VariableAppoint areaVarAppoint = areaDefine.getAreaVarAppoint();
		if (areaVarAppoint == null) {
			if (elementVarAppoint != null) {
				areaVarAppoint = elementVarAppoint;
			} else if (repeatVarAppoint != null) {
				areaVarAppoint = repeatVarAppoint;
			}
		}

		for (RowDefine rowDefine : areaDefine.getRowDefineList()) {
			VariableAppoint rowVarAppoint = rowDefine.getVarAppoint();
			if (rowVarAppoint == null) {
				rowVarAppoint = areaVarAppoint;
			}
			VariableDefine varDefine = rowVarAppoint.getVariableDefine();
			List<VariableAppoint> varDataList = null;
			if (areaDataMap.containsKey(varDefine)) {
				varDataList = areaDataMap.get(varDefine);
			} else {
				varDataList = new ArrayList<VariableAppoint>();
				areaDataMap.put(varDefine, varDataList);
			}
			isFinished = readCellValueR2C(areaConfig, areaDefine, rowDefine,
					varDataList);
			if(isFinished == true){
				break;
			}
		}

		if (isFinished == true) {
			return isFinished;
		}

		// 创建变量并设值
		//if (isFinished == false) {
		Map<String, Object> outData = this.getDataContext().getOutData();

		VariableDefine areaVarDefine = areaVarAppoint.getVariableDefine();
		List<VariableAppoint> areaVarDataList = areaDataMap.get(areaVarDefine);

		if (areaVarDataList != null) {
			//Object areaDataObj = areaVarAppoint.createNewObject(outData);
			areaVarAppoint.createNewObject(outData);
			for (VariableAppoint varAppoint : areaVarDataList) {
				varAppoint.putValue(outData);
			}
		}

		for (VariableDefine varDefineKey : areaDataMap.keySet()) {
			if (!varDefineKey.equals(areaVarDefine)) {
				List<VariableAppoint> varDataList = areaDataMap
						.get(areaVarDefine);
				if (varDataList != null) {
					varDefineKey.createNewObject(outData);
					for (VariableAppoint varAppoint : varDataList) {
						varAppoint.putValue(outData);
					}
				}
			}
		}
		//}

		return isFinished;
	}

	private boolean readCellValueR2C(AreaConfig areaConfig,
			AreaDefine areaDefine, RowDefine rowDefine,
			List<VariableAppoint> varDataList) {
		boolean isFinished = false;

		boolean isBegSameAsEnd = areaDefine.getPosition().isSameRow();
		int dataEndRow = areaConfig.getAreaPosition().getEndRow()
				+ areaDefine.getDataEndRow();

		int oneDataBegRow = currDSPosition.getRow();
		int rowNo = oneDataBegRow + rowDefine.getRelativeRow();
		currPosition.setRow(rowNo);
		log.debug("Position>> eachRow[" + currPosition.getRow() + ","
				+ currPosition.getColumn() + "]");

		int excelRowNo = rowNo - 1;
		Sheet sheet = this.impSheet;
		Row row = sheet.getRow(excelRowNo);
		if (row == null) {
			isFinished = true;
			return isFinished;
		}

		for (CellDefine cellDefine : rowDefine.getCellDefineList()) {
			VariableAppoint cellVarAppoint = cellDefine.getPropertyVar();

			currPosition.setColumn(cellDefine.getPosition().getBegCol());
			Object cellValue = readCellValue(row, currPosition);
			log.debug(cellVarAppoint.dumpStr() + ">> Position[" + currPosition.getRow() + ","
					+ currPosition.getColumn() + "]" + cellValue);
			if (!isBegSameAsEnd && rowNo > dataEndRow) {
				isFinished = true;
				return isFinished;
			}

			if ((cellValue == null || cellValue.toString() == "")
					&& PoiConstant.NOT_NULL_FLAG.equalsIgnoreCase(cellDefine
							.getNotNull())) {
				isFinished = true;
				return isFinished;
			} else {
				cellVarAppoint.setValue(cellValue);
				varDataList.add(cellVarAppoint);
			}
		}

		return isFinished;
	}

	private void impColumnsData(AreaConfig areaConfig, AreaDefine areaDefine,
			Map<VariableDefine, List<VariableAppoint>> areaDataMap) {
		if (areaDefine.getColumnDefineList() == null) {
			return;
		}
		for (ColumnDefine colDefine : areaDefine.getColumnDefineList()) {
			impOneColumnData(areaConfig, areaDefine, colDefine);
		}
	}

	private void impOneColumnData(AreaConfig areaConfig, AreaDefine areaDefine,
			ColumnDefine colDefine) {

		VariableAppoint varAppoint = colDefine.getVarAppoint();

		if (varAppoint == null) {
			varAppoint = areaDefine.getAreaVarAppoint(); // 啥事都没干
		} else if (VariableDefine.VAR_TYPE_LIST.equals(varAppoint
				.getVariableDefine().getVarType())) {
			boolean isFinished = false;
			int colDataIdx = 0;
			do {
				isFinished = impOneColumnOneData(areaConfig, areaDefine,
						colDefine, colDataIdx++);
			} while (isFinished == false);
		}
	}

	private boolean impOneColumnOneData(AreaConfig areaConfig,
			AreaDefine areaDefine, ColumnDefine colDefine, int colDataIdx) {
		boolean isFinished = false;
		int colDefineBegRow = colDefine.getPosition().getBegRow();
		int colDataBegRow = currDSPosition.getRow()
				+ colDefine.getRelativeRow();
		int colDataBegCol = colDefine.getPosition().getBegCol();

		int colDefineEndCol = colDefine.getPosition().getEndCol();

		List<VariableAppoint> varDataList = new ArrayList<VariableAppoint>();
		for (CellDefine cellDefine : colDefine.getCellDefineList()) {
			VariableAppoint cellVarAppoint = cellDefine.getPropertyVar();
			int cellDefineRow = cellDefine.getPosition().getBegRow();
			int cellDefineColNum = cellDefine.getPosition().getColNum();
			int rowNo = colDataBegRow + cellDefineRow - colDefineBegRow;
			int colNo = colDataBegCol + cellDefineColNum * colDataIdx;
			currPosition.setRow(rowNo);
			currPosition.setColumn(colNo);

			log.debug("eachColumn>> position[" + currPosition.getRow() + ","
					+ currPosition.getColumn() + "]");
			// 判断是否结束
			if (colNo > colDefineEndCol) {
				isFinished = true;
				return isFinished;
			}

			Object cellValue = readCellValue(currPosition);
			if ((cellValue == null || cellValue.toString() == "")
					&& PoiConstant.NOT_NULL_FLAG.equalsIgnoreCase(cellDefine
							.getNotNull())) {
				isFinished = true;
				return isFinished;
			} else {
				cellVarAppoint.setValue(cellValue);
				varDataList.add(cellVarAppoint);
			}
		}

		if (isFinished == false) {
			Map<String, Object> outData = this.getDataContext().getOutData();
			VariableAppoint colVarAppoint = colDefine.getVarAppoint();
			if (colVarAppoint != null) {
				colVarAppoint.createNewObject(outData); // 如果只有ColumnDefine,且没有设置colVarAppoint时未处理
			}
			for (VariableAppoint varAppoint : varDataList) {
				varAppoint.putValue(outData);
			}
		}
		return isFinished;
	}

	private Object readCellValue(Row row, Position position) {
		Object cellValue = null;
		int excelColNo = position.getColumn() - 1;
		Cell cell = row.getCell(excelColNo);
		if (cell == null) {
			return cellValue;
		}
		cellValue = ExcelUtil.readCellValue(cell);
		return cellValue;
	}

	private Object readCellValue(Position position) {
		Object cellValue = null;
		int excelRowNo = position.getRow() - 1;
		int excelColNo = position.getColumn() - 1;

		Sheet sheet = this.impSheet;
		Row row = sheet.getRow(excelRowNo);
		if (row == null) {
			return cellValue;
		}
		Cell cell = row.getCell(excelColNo);
		if (cell == null) {
			return cellValue;
		}
		cellValue = ExcelUtil.readCellValue(cell);
		return cellValue;
	}

	/**
	private void impCellsValue(ColumnDefine colDefine,
			List<Object> cellValueList, boolean isNewObject) {
		int cellIdx = 0;
		for (CellDefine cellDefine : colDefine.getCellDefineList()) {
			Map<String, Object> outData = this.getDataContext().getOutData();
			Object cellValue = cellValueList.get(cellIdx++);
			VariableAppoint propertyVar = cellDefine.getPropertyVar();
			propertyVar.putValue(cellValue, outData, isNewObject);
			log.debug("cellValue=" + cellValue + ";propertyVar="
					+ propertyVar.dumpStr());
		}
	}

	private void impCellValue(Row row, CellDefine cellDefine,
			boolean isNewObject) {
		//currPosition.setColumn(cellDefine.getPosition().getBegCol());
		int columnNo = currPosition.getColumn() - 1;
		Cell cell = row.getCell(columnNo);
		if (cell == null) {
			cell = row.createCell(columnNo);
		}

		Map<String, Object> outData = this.getDataContext().getOutData();
		Object cellValue = ExcelUtil.readCellValue(cell);
		VariableAppoint propertyVar = cellDefine.getPropertyVar();
		propertyVar.putValue(cellValue, outData, isNewObject);
		log.debug("cellValue=" + cellValue + ";propertyVar="
				+ propertyVar.dumpStr());
	}
	**/

	private void importInitialize() {
	}

	private void importTerminate() {
	}

	// getter/setter
	public SheetConfig getSheetConfig() {
		return sheetConfig;
	}

	public void setSheetConfig(SheetConfig sheetConfig) {
		this.sheetConfig = sheetConfig;
	}

	public SheetDefine getSheetDefine() {
		return sheetDefine;
	}

	public void setSheetDefine(SheetDefine sheetDefine) {
		this.sheetDefine = sheetDefine;
	}

	public Workbook getImpWorkbook() {
		return impWorkbook;
	}

	public void setImpWorkbook(Workbook impWorkbook) {
		this.impWorkbook = impWorkbook;
	}

	public Sheet getImpSheet() {
		return impSheet;
	}

	public void setImpSheet(Sheet impSheet) {
		this.impSheet = impSheet;
	}

	public IDataContext getDataContext() {
		return dataContext;
	}

	public void setDataContext(IDataContext dataContext) {
		this.dataContext = dataContext;
	}

	public IDataOper getDataOper() {
		return dataOper;
	}

	public void setDataOper(IDataOper dataOper) {
		this.dataOper = dataOper;
	}
}
