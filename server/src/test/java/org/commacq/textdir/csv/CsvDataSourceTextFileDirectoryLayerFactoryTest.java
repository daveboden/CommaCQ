package org.commacq.textdir.csv;

import static org.junit.Assert.assertEquals;

import org.commacq.layer.Layer;
import org.junit.Test;

import com.google.common.collect.ImmutableSortedSet;

public class CsvDataSourceTextFileDirectoryLayerFactoryTest {

	@Test
	public void testCreate() {
		CsvDataSourceTextFileDirectoryLayerFactory factory = new CsvDataSourceTextFileDirectoryLayerFactory();
		Layer layer = factory.create("classpath:/org/commacq/textdir/files");
		
		assertEquals(ImmutableSortedSet.of(
				"CsvDataSourceTextFileSingleDirectoryTestFiles",
				"SecondTextFileDirectory"
				),
				layer.getEntityIds()
		);
	}

}
