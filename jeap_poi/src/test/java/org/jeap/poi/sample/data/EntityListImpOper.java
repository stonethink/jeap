package org.jeap.poi.sample.data;

import org.jeap.poi.sample.service.IEntityImpService;
import org.springframework.beans.factory.annotation.Autowired;

import com.jeap.poi.data.impl.BaseDataOper;

public class EntityListImpOper  extends BaseDataOper{
	
	@Autowired
	IEntityImpService entityImpService;

	@Override
	public void preProduce() {
	}

	@Override
	public void produce() {
		entityImpService.importEntityList(dataContext);
	}

	@Override
	public void postProduce() {
		
	}

}
