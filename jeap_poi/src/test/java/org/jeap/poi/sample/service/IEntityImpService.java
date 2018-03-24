package org.jeap.poi.sample.service;

import com.yunkanghealth.yktechcom.poi.data.IDataContext;

public interface IEntityImpService {

	public void impEntityList();

	public void importEntityList(IDataContext dataContext);

	public void importEntity(IDataContext dataContext);

}