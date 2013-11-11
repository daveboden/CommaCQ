package org.commacq.testclient;

import lombok.Data;

import org.joda.time.LocalDate;

@Data
public final class Holiday {

	private final String id;
	private final String currency;
	private final LocalDate holidayDate;

}
