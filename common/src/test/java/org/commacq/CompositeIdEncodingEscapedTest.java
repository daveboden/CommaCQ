package org.commacq;

import static org.junit.Assert.*;

import org.junit.Test;

public class CompositeIdEncodingEscapedTest {

	@Test
	public void testNoEscapingRequiredSimple() {
		testNoEscapingRequired(new CompositeIdEncodingSimple());
	}
	
	@Test
	public void testNoEscapingRequiredEscapedImpl() {
		testNoEscapingRequired(new CompositeIdEncodingEscaped());
	}
	
	private void testNoEscapingRequired(CompositeIdEncoding encoding) {
		String compositeId = encoding.createCompositeId("Tui", "British Airways");
		assertEquals("Tui/British Airways", compositeId);
		
		String[] components = encoding.parseCompositeIdComponents(compositeId);
		assertEquals("Tui", components[0]);
		assertEquals("British Airways", components[1]);
	}
	
	@Test
	public void testNullsSimple() {
		testNulls(new CompositeIdEncodingSimple());
	}
	
	@Test
	public void testNullsEscapedImpl() {
		testNulls(new CompositeIdEncodingEscaped());
	}
	
	private void testNulls(CompositeIdEncoding encoding) {
		String compositeId = encoding.createCompositeId(null, "Tui", null, "British Airways", null, null);
		assertEquals("/Tui//British Airways//", compositeId);
		
		String[] components = encoding.parseCompositeIdComponents(compositeId);
		assertEquals(6, components.length);
		assertNull(components[0]);
		assertEquals("Tui", components[1]);
		assertNull(components[2]);
		assertEquals("British Airways", components[3]);
		assertNull(components[4]);
		assertNull(components[5]);
	}	

	@Test
	public void testEscaping() {
		CompositeIdEncodingEscaped encoding = new CompositeIdEncodingEscaped();
		
		String compositeId = encoding.createCompositeId("Tui\\Travel", "Multiple/Airlines");
		assertEquals("Tui\\\\Travel/Multiple\\/Airlines", compositeId);
		
		String[] components = encoding.parseCompositeIdComponents(compositeId);
		assertEquals("Tui\\Travel", components[0]);
		assertEquals("Multiple/Airlines", components[1]);
	}
	
}
