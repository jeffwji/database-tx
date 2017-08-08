package net.tinybrick.database.tx.unit;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import net.tinybrick.database.tx.unit.configuration.TestDatabaseConfigure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.test.context.SpringBootTest;

import net.tinybrick.database.tx.configuration.TransactionManagerConfigure;

//@TestPropertySource(locations = "classpath:config/database.properties")
public class TransactionalTestCase {
	Logger logger = LogManager.getLogger(this.getClass());

	final static String sql = "INSERT INTO test_table_01(name, address, type) VALUES('海淘车科技有限公司', '北京市东三环南路58号', '科技公司')";
	final static String select = "SELECT count(*) FROM test_table_01 WHERE name = '海淘车科技有限公司'";

	@Autowired List<DataSource> dataSources;
	List<JdbcTemplate> jdbcTemplates = new ArrayList<JdbcTemplate>();

	@RunWith(SpringJUnit4ClassRunner.class)
	@ComponentScan
	@SpringBootTest(
			classes = { TestDatabaseConfigure.SingleDataSourceConfig.class, TransactionManagerConfigure.class },
			properties = {
					"database.ds1.driverClassName:com.mysql.jdbc.Driver",
					"database.ds1.url:jdbc:mysql://db01.dev.htche.com/test01?autoReconnect=true&amp;useUnicode=true&amp;characterEncoding=utf8",
					"database.ds1.username:test01",
					"database.ds1.password:dDcuH1WOOKzdr49xYaoL" })
	public static class SingleDataSourceTestCase extends TransactionalTestCase {
		@Transactional
		@Test
		public void testDataSource() {
			for (int i = 0; i < dataSources.size(); i++) {
				logger.debug("DataSource #" + i);
				jdbcTemplates.add(new JdbcTemplate(dataSources.get(i)));
			}

			for (int i = 0; i < jdbcTemplates.size(); i++) {
				logger.debug("jdbcTemplates #" + i);
				jdbcTemplates.get(i).execute(sql);
				Assert.assertEquals(1, jdbcTemplates.get(i).queryForObject(select, Integer.class).intValue());
			}
		}
	}

	@RunWith(SpringJUnit4ClassRunner.class)
	@ComponentScan
	@SpringBootTest(
			classes = { TestDatabaseConfigure.MultipleDataSourceConfig.class, TransactionManagerConfigure.class },
			properties = {
					"database.ds1.driverClassName:com.mysql.jdbc.jdbc2.optional.MysqlXADataSource",
					"database.ds1.url:jdbc:mysql://db01.dev.htche.com/test01?autoReconnect=true&amp;useUnicode=true&amp;characterEncoding=utf8",
					"database.ds1.username:test01",
					"database.ds1.password:dDcuH1WOOKzdr49xYaoL",
					"database.ds2.driverClassName:com.mysql.jdbc.jdbc2.optional.MysqlXADataSource",
					"database.ds2.url:jdbc:mysql://db01.dev.htche.com/test02?autoReconnect=true&amp;useUnicode=true&amp;characterEncoding=utf8",
					"database.ds2.username:test02",
					"database.ds2.password:UsAbOuncMovDYRynPgO9" })
	public static class MultipleDataSourceTestCase extends TransactionalTestCase {
		@Transactional
		@Test
		public void testDataSource() {
			for (int i = 0; i < dataSources.size(); i++) {
				logger.debug("DataSource #" + i);
				jdbcTemplates.add(new JdbcTemplate(dataSources.get(i)));
			}

			for (int i = 0; i < jdbcTemplates.size(); i++) {
				logger.debug("jdbcTemplates #" + i);
				jdbcTemplates.get(i).execute(sql);
				Assert.assertEquals(1, jdbcTemplates.get(i).queryForObject(select, Integer.class).intValue());
			}
		}
	}
}
