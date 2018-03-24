package com.yunkanghealth.yktechcom.poi.config;

public class AreaConfig {
	private int index;
	
	private String id; //_TBL_	TH
	private String name;
	private String type;
	private String fillModel; //区域填充类型	CELL
	private String beginCell; //起始单元格	ID
	private String endCell; //结束单元格	0
	private String range;
	
	private int titleRow; //标题起始行(相对起始位)
	private int titleRowNum; //标题行数
	private int dataBeginRow; //数据起始行(相对起始位)
	private int dataEndRow; //数据结束行(相对结束位)
	private String notNullColumn; //非空检查列
	
	private int tplRow; //模板记录行
	private int tplRowNum; //模板记录行数
	private String defaultVariable; //缺省数据对象名
	
	private RangePosition areaPosition;
	
	// construct
	public AreaConfig(){
		super();
	}
	
	public AreaConfig(AreaDefine areaDefine){
		super();
		this.resetAreaConfig(areaDefine);
	}
	
	public void resetAreaConfig(AreaDefine areaDefine) {
		this.setId(areaDefine.getId());
		this.setFillModel(areaDefine.getFillModel());
		this.setBeginCell(areaDefine.getBeginCell());
		this.setEndCell(areaDefine.getEndCell());
		this.setTitleRow(areaDefine.getTitleRow());
		this.setTitleRowNum(areaDefine.getTitleRowNum());
		this.setDataBeginRow(areaDefine.getDataBeginRow());
		this.setDataEndRow(areaDefine.getDataEndRow());
		this.setNotNullColumn(areaDefine.getNotNullColumn());
		this.setTplRow(areaDefine.getTplRow());
		this.setTplRowNum(areaDefine.getTplRowNum());
		this.setDefaultVariable(areaDefine.getVariable());
		
		this.areaPosition = new RangePosition(areaDefine.getPosition());
	}
	
	public void resetAreaPosition(){
		areaPosition = new RangePosition(beginCell,endCell);
	}

	// setter/getter
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getFillModel() {
		return fillModel;
	}
	public void setFillModel(String fillModel) {
		this.fillModel = fillModel;
	}
	public String getBeginCell() {
		return beginCell;
	}
	public void setBeginCell(String beginCell) {
		this.beginCell = beginCell;
	}
	public String getEndCell() {
		return endCell;
	}
	public void setEndCell(String endCell) {
		this.endCell = endCell;
	}
	public String getRange() {
		return range;
	}
	public void setRange(String range) {
		this.range = range;
	}
	public int getTitleRow() {
		return titleRow;
	}
	public void setTitleRow(int titleRow) {
		this.titleRow = titleRow;
	}
	public int getTitleRowNum() {
		return titleRowNum;
	}
	public void setTitleRowNum(int titleRowNum) {
		this.titleRowNum = titleRowNum;
	}
	public int getDataBeginRow() {
		return dataBeginRow;
	}
	public void setDataBeginRow(int dataBeginRow) {
		this.dataBeginRow = dataBeginRow;
	}
	public int getDataEndRow() {
		return dataEndRow;
	}
	public void setDataEndRow(int dataEndRow) {
		this.dataEndRow = dataEndRow;
	}
	public String getNotNullColumn() {
		return notNullColumn;
	}
	public void setNotNullColumn(String notNullColumn) {
		this.notNullColumn = notNullColumn;
	}
	public int getTplRow() {
		return tplRow;
	}
	public void setTplRow(int tplRow) {
		this.tplRow = tplRow;
	}
	public int getTplRowNum() {
		return tplRowNum;
	}
	public void setTplRowNum(int tplRowNum) {
		this.tplRowNum = tplRowNum;
	}
	public String getDefaultVariable() {
		return defaultVariable;
	}
	public void setDefaultVariable(String defaultVariable) {
		this.defaultVariable = defaultVariable;
	}

	public RangePosition getAreaPosition() {
		return areaPosition;
	}
	public void setAreaPosition(RangePosition areaPosition) {
		this.areaPosition = areaPosition;
	}
}
