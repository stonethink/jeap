package com.jeap.poi.excel.exporter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.jeap.poi.excel.writer.DataWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import com.jeap.poi.PoiConstant;
import com.jeap.poi.config.AreaConfig;
import com.jeap.poi.config.AreaDefine;
import com.jeap.poi.config.CellDefine;
import com.jeap.poi.config.ColumnDefine;
import com.jeap.poi.config.Position;
import com.jeap.poi.config.RangePosition;
import com.jeap.poi.config.RowDefine;
import com.jeap.poi.config.SheetConfig;
import com.jeap.poi.config.SheetDefine;
import com.jeap.poi.config.VariableAppoint;
import com.jeap.poi.config.VariableDefine;
import com.jeap.poi.data.IDataContext;
import com.jeap.poi.data.IDataOper;
import com.jeap.poi.data.impl.DefaultDataContext;
import com.jeap.poi.util.ExcelUtil;
import com.jeap.tools.util.JeapStringUtil;

public class SheetExporter {
	private static final Log log = LogFactory.getLog(SheetExporter.class);

	private SheetConfig sheetConfig;

	private SheetDefine sheetDefine;
	private Workbook outWorkbook;
	private Sheet tplSheet;
	private Sheet outSheet;

	private Object repeatData;
	private int repeatIndex = -1;

	private IDataContext dataContext;
	private IDataOper dataOper;

	private List<Row> currTplRowList = null;
	private List<CellRangeAddress> currTplMergedRegionList = null;

	private Position currDSPosition = new Position();
	private int currAreaOneDataRowNum = 0;
	private Position currPosition = new Position();

	// construct
	public SheetExporter() {
		super();
	}

	public SheetExporter(SheetDefine sheetDefine) {
		super();
		setSheetDefine(sheetDefine);
	}

	public void doExport(IDataContext dataContext) {
		setDataContext(dataContext);

		doExport();
	}

	public void doExport(IDataContext dataContext, Object repeatData,
			int repeatIndex) {
		setDataContext(dataContext);
		setRepeatData(repeatData);
		setRepeatIndex(repeatIndex);

		doExport();
	}

	public void doExport() {
		// 开始时间
		Long begTime = System.currentTimeMillis();
		log.debug("Export sheet begin:" + new Date(begTime));

		if (dataContext == null) {
			dataContext = new DefaultDataContext();
		}
		VariableAppoint elementNameVar = sheetDefine.getElementNameVar();
		if (elementNameVar != null
				&& elementNameVar.getVariableDefine() != null) {
			String key = elementNameVar.getVariableDefine().getVarName();
			dataContext.putOneOutData(key, repeatData);
		}

		if (dataOper != null) {
			dataOper.setDataContext(dataContext);
			dataOper.preProduce();
		}

		exportInitialize();

		if (dataOper != null) {
			dataOper.setDataContext(dataContext);
			dataOper.produce();
		}

		for (int areaIndex = 0; areaIndex < sheetDefine.getAreaDefineList()
				.size(); areaIndex++) {
			AreaDefine areaDefine = sheetDefine.getAreaDefineList().get(
					areaIndex);
			AreaConfig areaConfig = sheetConfig.getAreaConfig(areaIndex);

			String fillModel = areaDefine.getFillModel();
			if (fillModel != null) {
				fillModel = fillModel.toUpperCase();
			}
			log.debug("Export Area>> index=" + areaIndex + ";id="
					+ areaDefine.getId() + ";fillModel=" + fillModel);
			switch (fillModel) {
			case PoiConstant.FILL_MODEL_AUTO:
				break;
			case PoiConstant.FILL_MODEL_DYNAMIC:
				expAreaDynamic(areaDefine, areaConfig);
				break;
			default: // FILL_MODEL_FIXED
				expAreaFixed(areaDefine, areaConfig);
			}
		}

		exportTerminate();

		if (dataOper != null) {
			dataOper.setDataContext(dataContext);
			dataOper.postProduce();
		}

		// 结束时间
		Long endTime = System.currentTimeMillis();
		log.debug("Export sheet end:" + new Date(endTime) + ";Run time:"
				+ (endTime - begTime));
	}

	private void expAreaFixed(AreaDefine areaDefine, AreaConfig areaConfig) {
		RangePosition realAreaPosition = areaConfig.getAreaPosition();
		currDSPosition.resetPosition(realAreaPosition.getBeginCell());
		int realAreaBegRow = areaConfig.getAreaPosition().getBegRow();
		int realAreaDataBegRow = realAreaBegRow + areaDefine.getDataBeginRow();
		currDSPosition.setRow(realAreaDataBegRow);
		log.debug("Area=" + areaDefine.getId() + "; eachArea["
				+ currDSPosition.getRow() + "," + currDSPosition.getColumn()
				+ "]");
		currPosition.resetPosition(currDSPosition);
		
		expRowsDataFixed(areaDefine, areaConfig);
	}
	
	private void expRowsDataFixed(AreaDefine areaDefine, AreaConfig areaConfig){
		if(areaDefine.getRowDefineList() == null){
			return;
		}
			
		Object elderVarData = null;
		VariableDefine elderVarDefine = null;
		VariableAppoint varAppoint = areaDefine.getAreaVarAppoint();
		if (varAppoint != null) {
			elderVarData = this.areaOutData(areaDefine);
			elderVarDefine = varAppoint.getVariableDefine();
		} else {
			elderVarData = repeatData;
			VariableAppoint elementNameVar = sheetDefine.getElementNameVar();
			VariableAppoint repeatByVar = sheetDefine.getRepeatByVar();
			if (elementNameVar != null) {
				elderVarDefine = elementNameVar.getVariableDefine();
			} else if (repeatByVar != null) {
				elderVarDefine = repeatByVar.getVariableDefine();
			}
		}

		Map<String, Object> outData = this.getDataContext().getOutData();
		for (int rowIdx = 0; rowIdx < areaDefine.getRowDefineList().size(); rowIdx++) {
			RowDefine rowDefine = areaDefine.getRowDefineList().get(rowIdx);
			int currRowNo = 0;
			currRowNo = this.currDSPosition.getRow()
					+ rowDefine.getRelativeRow();
			currPosition.setRow(currRowNo);
			log.debug("Row=" + rowDefine.getNo() + "; eachRow["
					+ currPosition.getRow() + "," + currPosition.getColumn()
					+ "]");
			for (CellDefine cellDefine : rowDefine.getCellDefineList()) {
				currPosition.setColumn(cellDefine.getPosition().getBegCol());
				VariableAppoint propertyVar = cellDefine.getPropertyVar();
				Object propValue = propertyVar.dataValue(outData, elderVarData,
						elderVarDefine);
				writeCellValue(cellDefine, propValue);
			}
		}		
	}

	/**
	 * 按行来进行数据填充<br>
	 * 1. 读取模板行（包括格式及初始数据）：Row <br>
	 * 2. 计算所需要插入的行数，并插入行<br>
	 * 3. 循环读取数据，进行填充 <br>
	 * 3.1 先按模板行的格式及初始数据进行填充 <br>
	 * 3.2 按行及单元格的配置导出输出数据<br>
	 * 
	 * @param areaDefine
	 */
	private void expAreaDynamic(AreaDefine areaDefine, AreaConfig areaConfig) {
		currTplRowList = null;
		currTplMergedRegionList = null;
		tplRows(areaDefine);
		tplMergedRegions(areaDefine);

		RangePosition realAreaPosition = areaConfig.getAreaPosition();
		currPosition.resetPosition(realAreaPosition.getBeginCell());

		List<Object> outDataList = areaOutDataList(areaDefine);
		shifAreaRows(areaDefine, areaConfig, outDataList);
		fillAreaDatas(areaDefine, areaConfig, outDataList);
	}

	private void fillAreaDatas(AreaDefine areaDefine, AreaConfig areaConfig,
			List<Object> outDataList) {
		if (outDataList == null) {
			return;
		}
		RangePosition realAreaPosition = areaConfig.getAreaPosition();
		currDSPosition.resetPosition(realAreaPosition.getBeginCell());
		int realAreaBegRow = areaConfig.getAreaPosition().getBegRow();
		int realAreaDataBegRow = realAreaBegRow + areaDefine.getDataBeginRow();
		currDSPosition.setRow(realAreaDataBegRow);
		log.debug("Area=" + areaDefine.getId() + "; eachArea["
				+ currDSPosition.getRow() + "," + currDSPosition.getColumn()
				+ "]");

		int dataNum = outDataList.size();;
		for (int dataIdx = 0; dataIdx < dataNum; dataIdx++) {
			Object oneData = outDataList.get(dataIdx);
			log.debug("fillAreaRows>> dataIndex=" + dataIdx + ";oneData="
					+ oneData);
			fillOneDataDynamic(areaDefine, areaConfig, oneData, dataIdx);
		}
	}

	private void fillOneDataDynamic(AreaDefine areaDefine,
			AreaConfig areaConfig, Object oneData, int dataIdx) {
		currPosition.resetPosition(currDSPosition);
		int oneDataBegRow = currDSPosition.getRow();
		currAreaOneDataRowNum = areaDefine.getDataRowNum();

		// fill rows by template rows
		applyTemplate(areaDefine, oneData);

		fillRowDatas(areaDefine, areaConfig, oneData, dataIdx);
		fillColumns(areaDefine, areaConfig, oneData, dataIdx);

		int oneDataEndRow = oneDataBegRow + currAreaOneDataRowNum - 1;
		int nextOneDataBegRow = oneDataEndRow + 1;
		currDSPosition.setRow(nextOneDataBegRow);
	}

	private void fillRowDatas(AreaDefine areaDefine, AreaConfig areaConfig,
			Object oneData, int dataIdx) {
		if(areaDefine.getRowDefineList() == null || oneData == null){
			return;
		}
		Object elderVardata = oneData;
		VariableDefine elderVarDefine = null;
		if (areaDefine.getAreaVarAppoint() != null) {
			elderVarDefine = areaDefine.getAreaVarAppoint().getVariableDefine();
		}

		int oneDataBegRow = currDSPosition.getRow();

		Map<String, Object> outData = this.getDataContext().getOutData();
		for (RowDefine rowDefine : areaDefine.getRowDefineList()) {
			int rowNo = oneDataBegRow + rowDefine.getRelativeRow();
			currPosition.setRow(rowNo);
			log.debug("currPosition>> eachRow[" + currPosition.getRow() + ","
					+ currPosition.getColumn() + "]");
			for (CellDefine cellDefine : rowDefine.getCellDefineList()) {
				currPosition.setColumn(cellDefine.getPosition().getBegCol());
				VariableAppoint propertyVar = cellDefine.getPropertyVar();
				Object propValue = propertyVar.dataValue(outData, elderVardata,
						elderVarDefine);
				writeCellValue(cellDefine, propValue);
			}
		}
	}

	private void fillColumns(AreaDefine areaDefine, AreaConfig areaConfig,
			Object oneAreaData, int dataIdx) {
		if(areaDefine.getColumnDefineList() == null || oneAreaData == null){
			return;
		}
		
		VariableDefine elderVarDefine = null;
		Object elderVarData = null;
		elderVarData = oneAreaData;
		if (areaDefine.getAreaVarAppoint() != null) {
			elderVarDefine = areaDefine.getAreaVarAppoint().getVariableDefine();
		}

		Map<String, Object> outData = this.getDataContext().getOutData();
		for (ColumnDefine colDefine : areaDefine.getColumnDefineList()) {
			VariableAppoint currVarAppoint = colDefine.getVarAppoint();
			Object colData = null;
			if (currVarAppoint != null) {
				colData = currVarAppoint.dataValue(outData, elderVarData,
						elderVarDefine);
			} else {
				colData = elderVarData;
			}
			fillOneColumn(areaDefine, areaConfig, colDefine, colData);
		}
	}

	@SuppressWarnings("unchecked")
	private void fillOneColumn(AreaDefine areaDefine, AreaConfig areaConfig,
			ColumnDefine colDefine, Object colData) {
		if (colData == null) {
			return;
		}

		List<Object> colDataList = null;
		if (colData instanceof List) {
			colDataList = (List<Object>) colData;
			for (int colDataIdx = 0; colDataIdx < colDataList.size(); colDataIdx++) {
				Object oneColData = colDataList.get(colDataIdx);
				fillColumnOneData(areaDefine, areaConfig, colDefine,
						oneColData, colDataIdx);
			}
		} else {
			int colDataIdx = 0;
			fillColumnOneData(areaDefine, areaConfig, colDefine, colData,
					colDataIdx);
		}
	}

	private void fillColumnOneData(AreaDefine areaDefine,
			AreaConfig areaConfig, ColumnDefine colDefine, Object oneData,
			int dataIdx) {
		VariableDefine elderVarDefine = null;
		Object elderVarData = oneData;
		if (colDefine.getVarAppoint() != null) {
			elderVarDefine = colDefine.getVarAppoint().getVariableDefine();
		} else if (areaDefine.getAreaVarAppoint() != null) {
			elderVarDefine = areaDefine.getAreaVarAppoint().getVariableDefine();
		}

		int colDefineBegRow = colDefine.getPosition().getBegRow();
		int colDataBegRow = currDSPosition.getRow()
				+ colDefine.getRelativeRow();
		int colDataBegCol = colDefine.getPosition().getBegCol();

		Map<String, Object> outData = this.getDataContext().getOutData();
		for (CellDefine cellDefine : colDefine.getCellDefineList()) {
			int cellDefineRow = cellDefine.getPosition().getBegRow();
			int cellDefineColNum = cellDefine.getPosition().getColNum();
			int rowNo = colDataBegRow + cellDefineRow - colDefineBegRow;
			int colNo = colDataBegCol + cellDefineColNum * dataIdx;
			currPosition.setRow(rowNo);
			currPosition.setColumn(colNo);

			VariableAppoint propertyVar = cellDefine.getPropertyVar();
			Object propValue = propertyVar.dataValue(outData, elderVarData,
					elderVarDefine);
			writeCellValue(cellDefine, propValue);
		}
	}

	private void writeCellValue(CellDefine cellDefine, Object propValue) {
		log.debug("currPosition>> writeCell[" + currPosition.getRow() + ","
				+ currPosition.getColumn() + "]");

		Sheet sheet = this.outSheet;
		int rowNo = currPosition.getRow() - 1;
		int columnNo = currPosition.getColumn() - 1;
		Row row = sheet.getRow(rowNo);
		if (row == null) {
			row = sheet.createRow(rowNo);
		}
		Cell cell = row.getCell(columnNo);
		if (cell == null) {
			cell = row.createCell(columnNo);
		}
		DataWriter dataWriter = new DataWriter(outWorkbook, outSheet, row, cell);
		String format = cellDefine.getPropertyVar().getVarFormat();
		dataWriter.writeValue(propValue, format);
		log.debug("writeCell>> row=" + rowNo + ";col=" + columnNo + ";value="
				+ propValue);
	}

	private void applyTemplate(AreaDefine area, Object oneData) {
		int tplRow = area.getTplRow();
		if (tplSheet == null || tplRow == 0) {
			return;
		}
		if (area.getPosition().isSameRow()) {
			return;
		}

		applyTplMergedRegions(area, oneData);
		applyTPlRows(area, oneData);
	}

	private void applyTplMergedRegions(AreaDefine area, Object oneData) {
		int begRow = area.getTplRow() - 1;
		for (CellRangeAddress tplRange : currTplMergedRegionList) {
			int firstRow = currPosition.getRow() - 1
					+ (tplRange.getFirstRow() - begRow);
			int lastRow = currPosition.getRow() - 1
					+ (tplRange.getLastRow() - begRow);
			int firstCol = tplRange.getFirstColumn();
			int lastCol = tplRange.getLastColumn();

			CellRangeAddress outRange = new CellRangeAddress(firstRow, lastRow,
					firstCol, lastCol);
			outSheet.addMergedRegion(outRange);
		}
	}

	private void applyTPlRows(AreaDefine area, Object oneData) { // 未来每条数据对应多个模块Rows时需要扩充
		for (int index = 0; index < currTplRowList.size(); index++) {
			Row tplRow = currTplRowList.get(index);
			int outRowNo = currPosition.getRow() - 1 + index;
			Row outRow = outSheet.getRow(outRowNo);
			if (outRow == null) {
				outRow = outSheet.createRow(outRowNo);
			}
			boolean isCopyValue = true;
			ExcelUtil.copyRow(outWorkbook, tplRow, outRow, isCopyValue);
		}
	}

	private void tplRows(AreaDefine area) {
		int tplRow = area.getTplRow();
		int tplRowNum = area.getTplRowNum();
		if (tplSheet == null || tplRow == 0) {
			return;
		}
		if (area.getPosition().isSameRow()) {
			return;
		}

		List<Row> tplRowList = new ArrayList<Row>();

		int rowIdx; // row index = template row no - 1
		for (int idx = 0; idx < tplRowNum; idx++) {
			rowIdx = tplRow - 1 + idx; // row index = template row no - 1
			tplRowList.add(tplSheet.getRow(rowIdx));
		}
		currTplRowList = tplRowList;
	}

	private void tplMergedRegions(AreaDefine area) {
		int tplRow = area.getTplRow();
		if (tplSheet == null || tplRow == 0) {
			return;
		}
		if (area.getPosition().isSameRow()) {
			return;
		}

		Sheet sheet = tplSheet;
		int begRow = area.getTplRow() - 1;
		int endRow = begRow + area.getTplRowNum() - 1;

		List<CellRangeAddress> mergedRegion = new ArrayList<CellRangeAddress>();

		int sheetMergerCount = sheet.getNumMergedRegions();
		for (int i = 0; i < sheetMergerCount; i++) {
			CellRangeAddress mergedRegionAt = sheet.getMergedRegion(i);
			if (mergedRegionAt.getFirstRow() >= begRow
					&& mergedRegionAt.getLastRow() <= endRow) {
				mergedRegion.add(mergedRegionAt);
			}
		}

		currTplMergedRegionList = mergedRegion;
	}

	private int areaDataRowNum(AreaDefine area, List<Object> outDataList) {
		int rowNum = 0;

		int tplRowNum = area.getTplRowNum();
		int dataNum = 0;
		if (outDataList != null) {
			dataNum = outDataList.size();
		}
		rowNum = dataNum * tplRowNum;
		return rowNum;
	}

	@SuppressWarnings("unchecked")
	private List<Object> areaOutDataList(AreaDefine area) {
		List<Object> dataList = null;

		Object dataObj = areaOutData(area);
		if (dataObj != null && dataObj instanceof List) {
			dataList = (List<Object>) dataObj;
		}

		return dataList;
	}

	private Object areaOutData(AreaDefine area) {
		Object dataObj = null;
		VariableAppoint areaDefaultVar = area.getAreaVarAppoint();
		if (areaDefaultVar != null) {
			Object elderVardata = repeatData;
			VariableDefine elderVarDefine = null;
			VariableAppoint elementNameVar = sheetDefine.getElementNameVar();
			VariableAppoint repeatByVar = sheetDefine.getRepeatByVar();
			if (elementNameVar != null) {
				elderVarDefine = elementNameVar.getVariableDefine();
			} else if (repeatByVar != null) {
				elderVarDefine = repeatByVar.getVariableDefine();
			}
			Map<String, Object> outData = this.getDataContext().getOutData();
			dataObj = areaDefaultVar.dataValue(outData, elderVardata,
					elderVarDefine);
		}
		return dataObj;
	}

	private void shifAreaRows(AreaDefine area, AreaConfig areaConfig,
			List<Object> outDataList) {
		if (area.getPosition().isSameRow()) {
			return;
		}

		int currAreaDataRow = currPosition.getRow() + area.getDataBeginRow();
		int lastRowNum = outSheet.getLastRowNum();
		int dataRowNum = areaDataRowNum(area, outDataList);
		if (dataRowNum > 0) {
			outSheet.shiftRows(currAreaDataRow - 1, lastRowNum, dataRowNum);
		}

		sheetConfig.shifAreaRows(currAreaDataRow, dataRowNum);
	}

	private void exportInitialize() {
		openSheet();
	}

	private void exportTerminate() {
		outSheet.setForceFormulaRecalculation(true);
	}

	private void openSheet() {
		String tplSheetName = sheetDefine.getTplSheet();
		int tplSheetIndex = -100;

		if (JeapStringUtil.isEmpty(tplSheetName)) {
			tplSheet = null;
		} else {
			tplSheet = outWorkbook.getSheet(tplSheetName);
			tplSheetIndex = outWorkbook.getSheetIndex(tplSheet);
		}

		String outSheetName = outSheetName();
		log.debug("TplSheet>> index=" + tplSheetIndex + ";name ="
				+ tplSheetName + "; outSheetName=" + outSheetName);

		int outSheetIndex = -100;
		if (tplSheet != null) {
			outSheet = outWorkbook.cloneSheet(tplSheetIndex);
			if (!JeapStringUtil.isEmpty(outSheetName)) {
				outSheetIndex = outWorkbook.getSheetIndex(outSheet);
				outWorkbook.setSheetName(outSheetIndex, outSheetName);
			}
		} else {
			if (JeapStringUtil.isEmpty(outSheetName)) {
				outSheet = outWorkbook.createSheet();
			} else {
				outSheetIndex = outWorkbook.getSheetIndex(outSheetName);
				if (outSheetIndex < 0) {
					outSheet = outWorkbook.createSheet(outSheetName);
				}
			}
		}
		log.debug("OutSheet>> index=" + outSheetIndex + ";name ="
				+ outSheetName);
	}

	private String outSheetName() {
		String sheetName = "";

		VariableAppoint repeatDataVar = sheetDefine.getRepeatByVar();
		VariableAppoint elementNameVar = sheetDefine.getElementNameVar();
		VariableAppoint sheetNameVar = sheetDefine.getSheetNameVar();

		Object elderObj = repeatData;
		VariableDefine elderVarDefine = null;
		if (elementNameVar != null) {
			elderVarDefine = elementNameVar.getVariableDefine();
		} else if (repeatDataVar != null) {
			elderVarDefine = repeatDataVar.getVariableDefine();
		}

		sheetName = sheetNameVar.dataValue(elderObj, elderVarDefine).toString();

		return sheetName;
	}

	// getter/setter
	public SheetDefine getSheetDefine() {
		return sheetDefine;
	}

	public void setSheetDefine(SheetDefine sheetDefine) {
		this.sheetDefine = sheetDefine;
		this.sheetConfig = new SheetConfig(sheetDefine);
		// resetAreaPositionList();
	}

	public Workbook getOutWorkbook() {
		return outWorkbook;
	}

	public void setOutWorkbook(Workbook outWorkbook) {
		this.outWorkbook = outWorkbook;
	}

	public Object getRepeatData() {
		return repeatData;
	}

	public void setRepeatData(Object repeatData) {
		this.repeatData = repeatData;
	}

	public int getRepeatIndex() {
		return repeatIndex;
	}

	public void setRepeatIndex(int repeatIndex) {
		this.repeatIndex = repeatIndex;
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