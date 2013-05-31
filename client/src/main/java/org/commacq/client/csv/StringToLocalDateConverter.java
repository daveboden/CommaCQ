package org.commacq.client.csv;

import org.joda.time.LocalDate;
import org.springframework.core.convert.converter.Converter;

public class StringToLocalDateConverter implements Converter<String, LocalDate> {
	@Override
	public LocalDate convert(String source) {
		return new LocalDate(source);
	}
}
