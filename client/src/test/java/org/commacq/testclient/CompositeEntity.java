package org.commacq.testclient;

import java.math.BigDecimal;

import javax.annotation.concurrent.Immutable;

import lombok.Data;

/**
 * An example immutable class that can be used as a static data bean.
 */
@Immutable
@Data
public final class CompositeEntity {

	private final String id;
	private final String site;
	private final int year;
	private final int month;
	private final BigDecimal revenue;

}
