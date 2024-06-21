package org.jeap.poi.sample.service.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jeap.devdb.entity.dev.TDevEntity;
import org.jeap.poi.sample.service.IEntityExpService;
import org.jeecgframework.core.common.service.impl.CommonServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeap.poi.data.IDataContext;
import com.jeap.poi.excel.IBookExporter;

@Service("entityExpService")
@Transactional
public class EntityExpService extends CommonServiceImpl implements IEntityExpService{
	private static final String KEY_SYS_CODE = "sysCode";
	private static final String KEY_ENTITY_LIST = "entityList";
	private static final String KEY_ENTITY = "entity";
	
	private static final Log log = LogFactory.getLog(EntityExpService.class);
	
	@Autowired
	IBookExporter entityBookExporter;

	@Override
	public void expEntityList(){
		String sysCode = "devp";
		log.debug("expEntityList>> key=" + KEY_SYS_CODE + "; value=" + sysCode);
		entityBookExporter.putOneOutData(KEY_SYS_CODE, sysCode);
		entityBookExporter.doExport();
	}
	
	@Override
	public void listEntity(IDataContext dataContext){
		String sysCode = (String)dataContext.getOneOutData(KEY_SYS_CODE);
		List<TDevEntity> entityList = listEntity(sysCode);
		dataContext.putOneOutData(KEY_ENTITY_LIST, entityList);
		if(entityList == null){
			log.debug("listEntity>> key=" + KEY_ENTITY_LIST + "; list is null!!!");
		}else{
			log.debug("listEntity>> key=" + KEY_ENTITY_LIST + "; listNum=" + entityList.size());
		}
	}
	
	private List<TDevEntity>  listEntity(String sysLid){
		//String hql = "from TDevEntity as ent where 1 = 1 AND ent.sysCode = ? order by ent.sysCode, ent.modCode, ent.Seq";
		String hql = "from TDevEntity as ent where 1 = 1 AND ent.sysCode = ? ";
		
		List<TDevEntity> tDevEntityList;
		//tDevEntityList = commonDao.findHql(hql, sysLid,modLid);
		tDevEntityList = commonDao.findHql(hql, sysLid);
		
		return tDevEntityList;
	}
	
	@Override
	public void getOneEntity(IDataContext dataContext){
		TDevEntity entityIn = (TDevEntity)dataContext.getOneOutData(KEY_ENTITY);
		TDevEntity entity = getEntity(entityIn);
		dataContext.putOneOutData(KEY_ENTITY, entity);
		if(entity == null){
			log.debug("getOneEntity>> key=" + KEY_ENTITY + "; entity is null!!!");
		}else{
			log.debug("getOneEntity>> key=" + KEY_ENTITY + "; entity=" + entity.getCode());
		}
	}
	
	private TDevEntity getEntity(TDevEntity entityIn){
		TDevEntity entity = commonDao.get(TDevEntity.class, entityIn.getId());
		return entity;
	}
}