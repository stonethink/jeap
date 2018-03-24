package org.jeap.poi.sample.service.impl;

import static org.junit.Assert.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jeap.poi.sample.service.IEntityExpService;
import org.jeap.poi.sample.service.IEntityImpService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = "classpath:devp/spring-mvc-devp.xml")
@ContextConfiguration({
	"classpath:spring-mvc-context.xml",
	//"classpath:spring-mvc-aop.xml",
	"classpath:hibernate4hib.xml",
	//"classpath:spring-minidao.xml",
	//"classpath:spring-mvc.xml",
	"classpath:poi/spring-mvc-poi.xml"
	})
public class EntityImpServiceTest extends AbstractJUnit4SpringContextTests{
	private static final Log log = LogFactory.getLog(EntityImpServiceTest.class);
	
	@Autowired
	IEntityImpService entityImpService;

	@Test
	public void testImpEntityList() {
		log.debug("TestCase Begin!");
		entityImpService.impEntityList();
		log.debug("TestCase End!");
	}

}
