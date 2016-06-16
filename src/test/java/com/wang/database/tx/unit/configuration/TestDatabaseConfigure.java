package com.wang.database.tx.unit.configuration;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import bitronix.tm.resource.jdbc.PoolingDataSource;

@EnableConfigurationProperties({ PropertySourcesPlaceholderConfigurer.class })
@PropertySource(value = "classpath:config/database.properties")
@Configuration
@EnableTransactionManagement
public class TestDatabaseConfigure {
	@Configuration
	public static class MultipleDataSourceConfig {
		@Bean(name = "dataSource1")
		public DataSource dataSource1(@Value("${database.ds1.driverClassName}") String className,
				@Value("${database.ds1.url}") String url, @Value("${database.ds1.username}") String username,
				@Value("${database.ds1.password}") String password) {
			PoolingDataSource poolingDataSource = new PoolingDataSource();
			poolingDataSource.setClassName(className);
			poolingDataSource.setEnableJdbc4ConnectionTest(false);
			poolingDataSource.setAllowLocalTransactions(true);
			poolingDataSource.setUniqueName("jdbc/dataSource1");
			poolingDataSource.setMinPoolSize(5);
			poolingDataSource.setMaxPoolSize(10);
			poolingDataSource.getDriverProperties().setProperty("url", url);
			if (poolingDataSource.getClassName().equals("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource"))
				poolingDataSource.getDriverProperties().setProperty("user", username);
			else
				poolingDataSource.getDriverProperties().setProperty("username", username);
			poolingDataSource.getDriverProperties().setProperty("password", password);
			return poolingDataSource;
		}

		@Bean(name = "dataSource2")
		public DataSource dataSource2(@Value("${database.ds2.driverClassName}") String className,
				@Value("${database.ds2.url}") String url, @Value("${database.ds2.username}") String username,
				@Value("${database.ds2.password}") String password) {
			PoolingDataSource poolingDataSource = new PoolingDataSource();
			poolingDataSource.setClassName(className);
			poolingDataSource.setEnableJdbc4ConnectionTest(false);
			poolingDataSource.setAllowLocalTransactions(true);
			poolingDataSource.setUniqueName("jdbc/dataSource2");
			poolingDataSource.setMinPoolSize(5);
			poolingDataSource.setMaxPoolSize(10);
			poolingDataSource.getDriverProperties().setProperty("url", url);
			if (poolingDataSource.getClassName().equals("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource"))
				poolingDataSource.getDriverProperties().setProperty("user", username);
			else
				poolingDataSource.getDriverProperties().setProperty("username", username);
			poolingDataSource.getDriverProperties().setProperty("password", password);
			return poolingDataSource;
		}
	}

	@Configuration
	public static class SingleDataSourceConfig {
		@ConfigurationProperties("database.ds1")
		@Bean(name = "dataSource1")
		public DataSource dataSource1() {
			return DataSourceBuilder.create().build();
		}
	}
}
