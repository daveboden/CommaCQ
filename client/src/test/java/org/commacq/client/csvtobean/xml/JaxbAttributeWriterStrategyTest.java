package org.commacq.client.csvtobean.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import org.commacq.client.CsvToBeanStrategyResult;

public class JaxbAttributeWriterStrategyTest {

	@Test
	public void testJaxbWriterStrategy() throws Exception {

		String csv = "id,name" + "\n" +
		             "BMW,A car" + "\n" +
				     "MERC,Another car";
		
		JaxbAttributeWriterStrategy<BeanWithXmlAnnotations> strategy = new JaxbAttributeWriterStrategy<>(BeanWithXmlAnnotations.class);
		
		Map<String, BeanWithXmlAnnotations> output = strategy.getBeans(csv).getUpdated();
		
		assertEquals("BMW", output.get("BMW").getId());		
	}
	
	@Test
	public void testJaxbWriterStrategyWithUPPERCASEBeanName() throws Exception {
		
		String csv = "id,name" + "\n" +
				"BMW,A car" + "\n" +
				"MERC,Another car";
		
		JaxbAttributeWriterStrategy<UPPERCASEBeanWithXmlAnnotations> strategy = new JaxbAttributeWriterStrategy<>(UPPERCASEBeanWithXmlAnnotations.class);
		
		Map<String, UPPERCASEBeanWithXmlAnnotations> output = strategy.getBeans(csv).getUpdated();
		
		assertEquals("BMW", output.get("BMW").getId());		
	}
	
	@Test
	public void testNullNameField() throws Exception {
		
		String csv = "id,name" + "\n" +
				"BMW,A car" + "\n" +
				"MERC,";
		
		JaxbAttributeWriterStrategy<BeanWithXmlAnnotations> strategy = new JaxbAttributeWriterStrategy<>(BeanWithXmlAnnotations.class);
		
		Map<String, BeanWithXmlAnnotations> output = strategy.getBeans(csv).getUpdated();
		
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
		
		JaxbAttributeWriterStrategy<BeanWithXmlAnnotationsBoolean> strategy = new JaxbAttributeWriterStrategy<>(BeanWithXmlAnnotationsBoolean.class);
		
		Map<String, BeanWithXmlAnnotationsBoolean> output = strategy.getBeans(csv).getUpdated();
		
		assertTrue(output.get("ABC").isActive());
		assertFalse(output.get("DEF").isActive());
	}
	
	@Test
	public void testDeletedRows() {
		String csv = "id,active" + "\n" +
                     "ABC,TRUE" + "\n" +
				     "DEF"; //Indicates that DEF is deleted
		
		JaxbAttributeWriterStrategy<BeanWithXmlAnnotationsBoolean> strategy = new JaxbAttributeWriterStrategy<>(BeanWithXmlAnnotationsBoolean.class);
		
		CsvToBeanStrategyResult<BeanWithXmlAnnotationsBoolean> result = strategy.getBeans(csv);
		
		assertTrue(result.getDeleted().contains("DEF"));
	}
	
	@Test
	public void testConvertClassNameToTagName() {
		assertEquals("lowerCamelCase", JaxbAttributeWriterStrategy.convertClassNameToTagName("lowerCamelCase"));
		assertEquals("upperCamelCase", JaxbAttributeWriterStrategy.convertClassNameToTagName("UpperCamelCase"));
		assertEquals("abcdUppercase", JaxbAttributeWriterStrategy.convertClassNameToTagName("ABCDUppercase"));
	}
	
}