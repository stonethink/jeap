package org.jeap.poi.sample.data;

import org.jeap.poi.sample.service.IEntityExpService;
import org.springframework.beans.factory.annotation.Autowired;

import com.jeap.poi.data.impl.BaseDataOper;

public class EntityListExpOper extends BaseDataOper{
	@Autowired
	IEntityExpService entityExpService;

	@Override
	public void preProduce() {
	}

	@Override
	public void produce() {
		entityExpService.listEntity(dataContext);
	}

	@Override
	public void postProduce() {
	}
}
