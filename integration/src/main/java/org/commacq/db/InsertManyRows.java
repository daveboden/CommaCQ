package org.commacq.db;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactoryBean;

@RequiredArgsConstructor
public class InsertManyRows implements BeanPostProcessor {

	private final String dataSourceBeanName;
	
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}
	
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if(!beanName.equals(dataSourceBeanName)) {
			return bean;
		}
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(((EmbeddedDatabaseFactoryBean)bean).getObject());
		
		for(int i = 0; i < 5000; i++) {
			jdbcTemplate.execute("insert into CUSTOMER (code, description, active, accountOpeningDate, currentBalance, currentBalanceDigits) values ('ID" + i + "', 'Name" + i + "', 1, '2012-02-02', 14032, 0)");
		}
		
		return bean;
	}
	
}
