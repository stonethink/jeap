package org.jeap.poi.sample.service;

import com.jeap.poi.data.IDataContext;

public interface IEntityExpService {

	public void expEntityList();

	public void listEntity(IDataContext dataContext);

	public void getOneEntity(IDataContext dataContext);

}