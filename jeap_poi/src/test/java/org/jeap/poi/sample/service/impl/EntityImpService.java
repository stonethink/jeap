package org.jeap.poi.sample.service.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jeap.devdb.entity.dev.TDevAttribute;
import org.jeap.devdb.entity.dev.TDevEntKey;
import org.jeap.devdb.entity.dev.TDevEntRelation;
import org.jeap.devdb.entity.dev.TDevEntity;
import org.jeap.devdb.entity.dev.TDevErAttribute;
import org.jeap.devdb.entity.dev.TDevKeyAttribute;
import org.jeap.poi.sample.service.IEntityImpService;
import org.jeecgframework.core.common.service.impl.CommonServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yunkanghealth.yktechcom.poi.data.IDataContext;
import com.yunkanghealth.yktechcom.poi.excel.IBookImporter;
import com.yunkanghealth.yktechcom.tools.util.JeapStringUtil;

@Service("entityImpService")
@Transactional
public class EntityImpService extends CommonServiceImpl implements
		IEntityImpService {
	private static final String KEY_SYS_CODE = "sysCode";
	private static final String KEY_ENTITY_LIST = "entityList";
	private static final String KEY_ENTITY = "entity";

	private static final Log log = LogFactory.getLog(EntityImpService.class);

	@Autowired
	IBookImporter entityBookImporter;

	@Override
	public void impEntityList() {
		String sysCode = "devp";
		log.debug("expEntityList>> key=" + KEY_SYS_CODE + "; value=" + sysCode);
		entityBookImporter.putOneOutData(KEY_SYS_CODE, sysCode);
		entityBookImporter.doImport();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void importEntityList(IDataContext dataContext) {
		// 取得读取的数据对象
		List<TDevEntity> entityList = (List<TDevEntity>) dataContext
				.getOneOutData(KEY_ENTITY_LIST);
		if (entityList == null) {
			log.debug("updateEntityList>> key=" + KEY_ENTITY_LIST
					+ "; list is null!!!");
		} else {
			log.debug("updateEntityList>> key=" + KEY_ENTITY_LIST
					+ "; listNum=" + entityList.size());
		}

		// 删除数据

		// 新增数据

	}

	@Override
	public void importEntity(IDataContext dataContext) {
		// 取得读取的数据对象
		TDevEntity entity = (TDevEntity) dataContext.getOneOutData(KEY_ENTITY);
		if (entity == null) {
			log.debug("updateEntity>> key=" + KEY_ENTITY
					+ "; entity is null!!!");
		} else {
			log.debug("updateEntity>> key=" + KEY_ENTITY + "; id="
					+ entity.getId() + "; name=" + entity.getCode());
		}

		// 导入数据
		importEntity(entity);
	}

	private void importEntity(TDevEntity entity) {
		deleteEntity(entity);
		insertEntity(entity);
	}

	private void deleteEntity(TDevEntity entity) {
		String entId = entity.getId();

		if (!JeapStringUtil.isEmpty(entId)) {
			try {
				// 删除从表
				//t_dev_ent_relation
				String hqlEntRelationList = "from TDevEntRelation  as er where 1 = 1 AND (er.TDevEntityBySrcEntId = ? OR er.TDevEntityByDestEntId = ?) ";
				TDevEntity tDevEntity = new TDevEntity();
				tDevEntity.setId(entId);

				List<TDevEntRelation> entRelationList = this.findHql(
						hqlEntRelationList, tDevEntity, tDevEntity);
				if (entRelationList != null && entRelationList.size() > 0) {
					this.deleteAllEntitie(entRelationList);
				}

				//t_dev_ent_key
				String hqlEntKeyList = "from TDevEntKey as entKey where 1 = 1 AND entKey.TDevEntity = ? ";
				List<TDevEntKey> entKeyList = this.findHql(hqlEntKeyList,
						tDevEntity);
				if (entKeyList != null && entKeyList.size() > 0) {
					this.deleteAllEntitie(entKeyList);
				}

				// 删除主表
				// t_dev_entity
				this.deleteEntityById(TDevEntity.class, entId);
			} catch (Exception e) {
				log.error(e.toString());
			}
		}

		/**
		if (!JeapStringUtil.isEmpty(entId)) {
			// 删除从表
			//t_dev_er_attribute@
			String hqlErAttributeList = "from TDevErAttribute as erAttr,TDevEntRelation as er where 1 = 1 AND erAttr.erId = er.id AND (er.srcEntId = ? OR er.destEntId = ?)";
			List<TDevErAttribute> erAttributeList = this.findHql(hqlErAttributeList, entId, entId);
			this.deleteAllEntitie(erAttributeList);
			
			//t_dev_ent_relation
			String hqlEntRelationList = "from TDevEntRelation  as er where 1 = 1 AND (er.srcEntId = ? OR er.destEntId = ?) ";
			List<TDevEntRelation> entRelationList = this.findHql(hqlEntRelationList, entId, entId);
			this.deleteAllEntitie(entRelationList);
			
			//t_dev_key_attribute
			String hqlEntKeyAttrList = "from TDevEntKeyAttrribute as entKeyAttrr, TDevEntKey as entKey where 1 = 1 AND entKeyAttrr.keyId = entKey.id AND  entKey.entId = ? ";
			List<TDevKeyAttribute> entKeyAttrList = this.findHql(hqlEntKeyAttrList, entId);
			this.deleteAllEntitie(entKeyAttrList);

			//t_dev_ent_key
			String hqlEntKeyList = "from TDevEntKey as entKey where 1 = 1 AND entKey.entId = ? ";
			List<TDevEntKey> entKeyList = this.findHql(hqlEntKeyList, entId);
			this.deleteAllEntitie(entKeyList);

			//t_dev_attribute
			String hqlAttributeList = "from TDevAttribute as attr where 1 = 1 AND attr.entId = ? ";
			List<TDevAttribute> attributeList = this.findHql(hqlAttributeList, entId);
			this.deleteAllEntitie(attributeList);
			
			// 删除主表
			// super.delete(entity);
			this.deleteEntityById(TDevEntity.class, entId);
		}
		**/
	}

	private void insertEntity(TDevEntity entity) {
		//String entId = null;
		entity.setId(null);
		if (JeapStringUtil.isEmpty(entity.getCUid())) {
			entity.setCUid(entity.getMUid());
		}
		if (JeapStringUtil.isEmpty(entity.getCName())) {
			entity.setCName(entity.getMName());
		}
		if (entity.getCTime() == null) {
			entity.setCTime(new Date());
		}
		entity.setMTime(new Date());

		if (entity.getTDevAttributes() != null) {
			//for(TDevAttribute attribute:entity.getTDevAttributes()){
			for (int index = 0; index < entity.getTDevAttributes().size(); index++) {
				TDevAttribute attribute = entity.getTDevAttributes().get(index);
				//String attId = null;
				attribute.setId(null);
				attribute.setTDevEntity(entity);
				if (attribute.getSeq() == null || attribute.getSeq() == 0) {
					attribute.setSeq(index);
				}
				if (JeapStringUtil.isEmpty(attribute.getCUid())) {
					attribute.setCUid(attribute.getMUid());
				}
				if (JeapStringUtil.isEmpty(attribute.getCName())) {
					attribute.setCName(attribute.getMName());
				}
				if (attribute.getCTime() == null) {
					attribute.setCTime(new Date());
				}
				attribute.setMTime(new Date());
			}
		}
		super.saveOrUpdate(entity);
		insertEntKeys(entity);
		insertER(entity);
	}

	private void insertEntKeys(TDevEntity entity) {
		if (entity.getTDevEntKeys() == null) {
			return;
		}

		for (int index = 0; index < entity.getTDevEntKeys().size(); index++) {
			TDevEntKey entKey = entity.getTDevEntKeys().get(index);
			entKey.setId(null);
			entKey.setTDevEntity(entity);
			if (entKey.getSeq() == null || entKey.getSeq() == 0) {
				entKey.setSeq(index);
			}
			if (JeapStringUtil.isEmpty(entKey.getCUid())) {
				entKey.setCUid(entKey.getMUid());
			}
			if (JeapStringUtil.isEmpty(entKey.getCName())) {
				entKey.setCName(entKey.getMName());
			}
			if (entKey.getCTime() == null) {
				entKey.setCTime(new Date());
			}
			entKey.setMTime(new Date());

			if (entKey.getTDevKeyAttributes() != null) {
				//for (TDevKeyAttribute keyAttr : entKey.getTDevKeyAttributes()) {
				for (int keyAttrIndex = 0; keyAttrIndex < entKey
						.getTDevKeyAttributes().size(); keyAttrIndex++) {
					TDevKeyAttribute keyAttr = entKey.getTDevKeyAttributes()
							.get(keyAttrIndex);

					keyAttr.setId(null);
					keyAttr.setTDevEntKey(entKey);

					TDevAttribute attrDb = loadTDevAttributeByCode(entity,
							keyAttr.getTDevAttribute());
					keyAttr.setTDevAttribute(attrDb);

					if (keyAttr.getSeq() == null || keyAttr.getSeq() == 0) {
						keyAttr.setSeq(keyAttrIndex);
					}

					if (JeapStringUtil.isEmpty(keyAttr.getCUid())) {
						keyAttr.setCUid(keyAttr.getMUid());
					}
					if (JeapStringUtil.isEmpty(keyAttr.getCName())) {
						keyAttr.setCName(keyAttr.getMName());
					}
					if (keyAttr.getCTime() == null) {
						keyAttr.setCTime(new Date());
					}
					keyAttr.setMTime(new Date());
				}
			}
			super.saveOrUpdate(entKey);
		}

	}

	private void insertER(TDevEntity entity) {
		if (entity.getTDevEntRelationsForSrcEntId() == null) {
			return;
		}

		for (int index = 0; index < entity.getTDevEntRelationsForSrcEntId()
				.size(); index++) {
			TDevEntRelation entRelation = entity
					.getTDevEntRelationsForSrcEntId().get(index);
			entRelation.setId(null);
			entRelation.setTDevEntityBySrcEntId(entity);

			TDevEntity entIn = new TDevEntity();
			entIn.setSysCode(entity.getSysCode());
			entIn.setCode(entRelation.getTDevEntityByDestEntId().getCode());
			TDevEntity entDest = loadEntityByCode(entIn);
			entRelation.setTDevEntityByDestEntId(entDest);

/**			
			if (entRelation.getSrcKeySeq() == null
					|| entRelation.getSrcKeySeq() == 0) {
				entRelation.setSrcKeySeq(index);
			}
**/			
			if (JeapStringUtil.isEmpty(entRelation.getCUid())) {
				entRelation.setCUid(entRelation.getMUid());
			}
			if (JeapStringUtil.isEmpty(entRelation.getCName())) {
				entRelation.setCName(entRelation.getMName());
			}
			if (entRelation.getCTime() == null) {
				entRelation.setCTime(new Date());
			}
			entRelation.setMTime(new Date());

			if (entRelation.getTDevErAttributes() != null) {
				for (int attrIndex = 0; attrIndex < entRelation
						.getTDevErAttributes().size(); attrIndex++) {
					TDevErAttribute attr = entRelation.getTDevErAttributes()
							.get(attrIndex);

					attr.setId(null);
					attr.setTDevEntRelation(entRelation);

					TDevAttribute attrSrcDb = loadTDevAttributeByCode(entity,
							attr.getTDevAttributeBySrcAttrId());
					//attr.setTDevAttributeBySrcAttrId(attrSrcDb);
					attr.getTDevAttributeBySrcAttrId().setId(attrSrcDb.getId());
					attr.setSrcAttrCode(attrSrcDb.getCode());

					TDevAttribute attrDestDb = loadTDevAttributeByCode(entity,
							attr.getTDevAttributeByDestAttrId());
					//attr.setTDevAttributeBySrcAttrId(attrDestDb);
					attr.getTDevAttributeByDestAttrId().setId(
							attrDestDb.getId());
					attr.setDestAttrCode(attrDestDb.getCode());

					if (attr.getSeq() == null || attr.getSeq() == 0) {
						attr.setSeq(attrIndex);
					}

					if (JeapStringUtil.isEmpty(attr.getCUid())) {
						attr.setCUid(attr.getMUid());
					}
					if (JeapStringUtil.isEmpty(attr.getCName())) {
						attr.setCName(attr.getMName());
					}
					if (attr.getCTime() == null) {
						attr.setCTime(new Date());
					}
					attr.setMTime(new Date());
				}
			}
			super.saveOrUpdate(entRelation);
		}
	}

	private TDevEntity loadEntityByCode(TDevEntity entIn) {
		String hql = "from TDevEntity as ent where 1 = 1 AND ent.sysCode = ? AND ent.code = ? ";
		TDevEntity entDB = null;
		List<TDevEntity> entList = this.findHql(hql, entIn.getSysCode(),
				entIn.getCode());
		if (entList != null && entList.size() > 0) {
			return entList.get(0);
		}
		return entDB;
	}

	private TDevAttribute loadTDevAttributeByCode(TDevEntity entity,
			TDevAttribute attrIn) {
		String hqlAttributeList = "from TDevAttribute as attr where 1 = 1 AND attr.TDevEntity = ? AND attr.code = ? ";
		TDevAttribute attrDb = null;
		//String entId = entity.getId();
		String attrCode = attrIn.getCode();
		List<TDevAttribute> attributeList = this.findHql(hqlAttributeList,
				entity, attrCode);
		if (attributeList != null && attributeList.size() > 0) {
			return attributeList.get(0);
		}

		return attrDb;
	}

}
