package org.commacq.client.csvtobean.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.commacq.CsvLineCallbackListImpl;
import org.commacq.CsvTextBlockToCallback;
import org.commacq.client.CsvToBeanStrategy;
import org.junit.Test;

public class JaxbAttributeWriterStrategyTest {

	@Test
	public void testJaxbWriterStrategy() throws Exception {

		String csv = "id,name" + "\n" +
		             "BMW,A car" + "\n" +
				     "MERC,Another car";
		
		JaxbAttributeWriterStrategy strategy = new JaxbAttributeWriterStrategy();
		
		Map<String, BeanWithXmlAnnotations> output = getBeans(BeanWithXmlAnnotations.class, strategy, csv);
		
		assertEquals("BMW", output.get("BMW").getId());		
	}
	
	@Test
	public void testJaxbWriterStrategyWithUPPERCASEBeanName() throws Exception {
		
		String csv = "id,name" + "\n" +
				"BMW,A car" + "\n" +
				"MERC,Another car";
		
		JaxbAttributeWriterStrategy strategy = new JaxbAttributeWriterStrategy();
		
		Map<String, UPPERCASEBeanWithXmlAnnotations> output = getBeans(UPPERCASEBeanWithXmlAnnotations.class, strategy, csv);
		
		assertEquals("BMW", output.get("BMW").getId());		
	}
	
	@Test
	public void testNullNameField() throws Exception {
		
		String csv = "id,name" + "\n" +
				"BMW,A car" + "\n" +
				"MERC,";
		
		JaxbAttributeWriterStrategy strategy = new JaxbAttributeWriterStrategy();
		
		Map<String, BeanWithXmlAnnotations> output = getBeans(BeanWithXmlAnnotations.class, strategy, csv);
		
		assertNull(output.get("MERC").getName());		
	}
	
	/**
	 * Excel has a habit of saving strings that it sees as booleans ("true", "false") as
	 * uppercase! "TRUE", "FALSE". This is fairly terrible behaviour, but we should account
	 * for it by ensuring that our unmarshaller has a good go at converting FALSE to boolean false
	 * and TRUE to boolean true. Without taking any action, the unmarshaller just sees TRUE as
	 * not equal to "true" and so a boolean ends up as false.
	 */
	@Test
	public void testUppercaseBooleanField() {
		String csv = "id,active" + "\n" +
	                 "ABC,TRUE" + "\n" +
				     "DEF,FALSE";
		
		JaxbAttributeWriterStrategy strategy = new JaxbAttributeWriterStrategy();
		
		Map<String, BeanWithXmlAnnotationsBoolean> output = getBeans(BeanWithXmlAnnotationsBoolean.class, strategy, csv);
		
		assertTrue(output.get("ABC").isActive());
		assertFalse(output.get("DEF").isActive());
	}
	
	@Test
	public void testConvertClassNameToTagName() {
		assertEquals("lowerCamelCase", JaxbAttributeWriterStrategy.convertClassNameToTagName("lowerCamelCase"));
		assertEquals("upperCamelCase", JaxbAttributeWriterStrategy.convertClassNameToTagName("UpperCamelCase"));
		assertEquals("abcdUppercase", JaxbAttributeWriterStrategy.convertClassNameToTagName("ABCDUppercase"));
	}
	
	private <BeanType> Map<String, BeanType> getBeans(Class<BeanType> beanType, CsvToBeanStrategy strategy, String csvHeaderAndBody) {
		CsvTextBlockToCallback csvTextBlockToCallback = new CsvTextBlockToCallback();
		CsvLineCallbackListImpl callbackListImpl = new CsvLineCallbackListImpl();
		csvTextBlockToCallback.presentTextBlockToCsvLineCallback("testEntity", csvHeaderAndBody, callbackListImpl, true);
		return strategy.getBeans(beanType, callbackListImpl.getColumnNamesCsv(), callbackListImpl.getUpdateList());
	}
	
}
