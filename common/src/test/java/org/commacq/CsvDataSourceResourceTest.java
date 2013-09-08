package org.commacq;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

public class CsvDataSourceResourceTest {

	@Test
	public void test() {
		CsvDataSourceResourceFactory factory = new CsvDataSourceResourceFactory(new ClassPathResource("/resources-test/"), false, ".csv"); 
		CsvDataSource resource = factory.createCsvDataSource("customer");
		CsvLineCallbackListImpl list = new CsvLineCallbackListImpl();
		resource.getAllCsvLines(list);
		List<CsvLine> lines = list.getUpdateList();
		assertEquals(2, lines.size());
	}

}
