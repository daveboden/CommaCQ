package org.commacq.testclient;

import java.math.BigDecimal;

import javax.annotation.concurrent.Immutable;

import lombok.Data;

import org.joda.time.LocalDate;

/**
 * An example immutable class that can be used as a static data bean.
 */
@Immutable
@Data
public final class Customer2 {

	private final String id;
	private final String description;
	private final boolean active;
	private final LocalDate accountOpeningDate;
	private final BigDecimal currentBalance;

}
