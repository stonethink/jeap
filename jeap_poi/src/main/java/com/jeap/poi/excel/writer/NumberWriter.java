package com.jeap.poi.excel.writer;

import com.jeap.tools.util.FormatUtils;

public class NumberWriter extends AbstractWriter {

	@Override
	public void writeValue(Object value) {
        if(value instanceof Number){
            Number num = (Number) value;
            cell.setCellValue(num.doubleValue());
            
            if(getFormat() != null){
                String result = FormatUtils.formatNumber(num.toString(), getFormat());
                cell.setCellValue(Double.parseDouble(result));
            }else{
                cell.setCellValue(num.doubleValue());
            }
        }
	}
}
