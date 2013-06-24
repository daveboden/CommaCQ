package org.commacq.db;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

public class InsertManyRows {

	public InsertManyRows(DataSource dataSource) {		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		
		for(int i = 0; i < 50000; i++) {
			jdbcTemplate.execute("insert into CUSTOMER (code, description, active, accountOpeningDate, currentBalance, currentBalanceDigits) values ('ID" + i + "', 'Name" + i + "', 1, '2012-02-02', 14032, 0)");
		}
	}
	
}
