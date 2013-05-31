package org.commacq.testclient;

import java.math.BigDecimal;

import javax.annotation.concurrent.Immutable;

import org.joda.time.LocalDate;

/**
 * An example immutable class that can be used as a static data bean.
 */
@Immutable
public final class Customer {

	private final String id;
	private final String description;
	private final boolean active;
	private final LocalDate accountOpeningDate;
	private final BigDecimal currentBalance;
	
	public Customer(String id, String description, Boolean active, LocalDate accountOpeningDate, BigDecimal currentBalance) {
		this.id = id;
		this.description = description;
		this.active = active;
		this.accountOpeningDate = accountOpeningDate;
		this.currentBalance = currentBalance;
	}

	public String getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public boolean isActive() {
		return active;
	}
	
	public LocalDate getAccountOpeningDate() {
		return accountOpeningDate;
	}
	
	public BigDecimal getCurrentBalance() {
		return currentBalance;
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Customer other = (Customer) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Customer [id=" + id + ", description=" + description
				+ ", active=" + active + "]";
	}

}
