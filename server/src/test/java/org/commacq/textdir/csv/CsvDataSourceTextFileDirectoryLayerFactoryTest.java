package org.commacq.textdir.csv;

import static org.junit.Assert.assertEquals;

import org.commacq.CsvDataSourceLayer;
import org.junit.Test;

import com.google.common.collect.ImmutableSortedSet;

public class CsvDataSourceTextFileDirectoryLayerFactoryTest {

	@Test
	public void testCreate() {
		CsvDataSourceTextFileDirectoryLayerFactory factory = new CsvDataSourceTextFileDirectoryLayerFactory();
		CsvDataSourceLayer layer = factory.create("classpath:/org/commacq/textdir/files");
		
		assertEquals(ImmutableSortedSet.of(
				"CsvDataSourceTextFileSingleDirectoryTestFiles",
				"SecondTextFileDirectory"
				),
				layer.getEntityIds()
		);
	}

}
