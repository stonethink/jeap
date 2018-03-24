package org.jeap.poi.sample.data;

import org.jeap.poi.sample.service.IEntityExpService;
import org.springframework.beans.factory.annotation.Autowired;

import com.yunkanghealth.yktechcom.poi.data.impl.BaseDataOper;

public class EntityDataExpOper extends BaseDataOper {
	@Autowired
	IEntityExpService entityExpService;

	@Override
	public void preProduce() {
	}

	@Override
	public void produce() {
		entityExpService.getOneEntity(dataContext);
	}

	@Override
	public void postProduce() {
	}

}
