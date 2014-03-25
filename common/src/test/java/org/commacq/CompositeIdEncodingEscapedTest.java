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
	public void testEscaping() {
		CompositeIdEncodingEscaped encoding = new CompositeIdEncodingEscaped();
		
		String compositeId = encoding.createCompositeId("Tui\\Travel", "Multiple/Airlines");
		assertEquals("Tui\\\\Travel/Multiple\\/Airlines", compositeId);
		
		String[] components = encoding.parseCompositeIdComponents(compositeId);
		assertEquals("Tui\\Travel", components[0]);
		assertEquals("Multiple/Airlines", components[1]);
	}
	
}
