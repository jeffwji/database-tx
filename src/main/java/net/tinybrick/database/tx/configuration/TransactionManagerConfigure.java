package net.tinybrick.database.tx.configuration;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import java.util.List;

@Configuration
@EnableAutoConfiguration
//@EnableConfigurationProperties({ PropertySourcesPlaceholderConfigurer.class })
@PropertySource(value = "classpath:config/transaction.properties")
public class TransactionManagerConfigure implements ApplicationContextAware {
    Logger logger = Logger.getLogger(this.getClass());

    @Autowired
    List<DataSource> dataSources;

    @Value("${transaction.tx.serviceId:default}")
    String serviceId;
    @Value("${transaction.tx.logPart1Filename:btm1.tlog}")
    String logPart1Filename;
    @Value("${transaction.tx.logPart2Filename:btm2.tlog}")
    String logPart2Filename;

    /**
     * 获得 BitronixTransactionManager 的 BeanDefinition
     *
     * @return
     */
    protected BeanDefinition BitronixTransactionManagerBeanDefinition() {
        TransactionManagerServices.getConfiguration().setServerId(serviceId).setLogPart1Filename(logPart1Filename)
                .setLogPart2Filename(logPart2Filename);

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(TransactionManagerServices.class);
        builder.setDestroyMethodName("shutdown");
        builder.setFactoryMethod("getTransactionManager");

        return builder.getBeanDefinition();
    }

    /**
     * 获得 PlatformTransactionManager 的 BeanDefinition
     *
     * @param transactionManagerBeanName
     * @return
     */
    public BeanDefinition PlatformTransactionManagerBeanDefinition(String transactionManagerBeanName) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(JtaTransactionManager.class);
        builder.addPropertyReference("transactionManager", transactionManagerBeanName);
        builder.addPropertyReference("userTransaction", transactionManagerBeanName);

        return builder.getBeanDefinition();
    }

    /**
     * 获得 DataSourceTransactionManager 的 BeanDefinition
     *
     * @return
     */
    public BeanDefinition DataSourceTransactionManagerBeanDefinition() {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(DataSourceTransactionManager.class);
        builder.addPropertyValue("dataSource", dataSources.get(0));

        return builder.getBeanDefinition();
    }

    @Autowired(required = false)
    TransactionManager transactionManager;

    /**
     * 注册 TransactionManager
     */
    @PostConstruct
    public void registerTransactionManager() {
        if (null != transactionManager)
            throw new RuntimeException(
                    "You must disable default TransactionManager by add \"spring.jta.enabled=false\" to your configure file");

        if (null != dataSources && dataSources.size() != 0) {
            GenericApplicationContext context = new GenericApplicationContext();

            if (dataSources.size() > 1) {
                logger.info("2 datasources was found!");
                for (DataSource dataSource : dataSources) {
                    if (!(dataSource instanceof PoolingDataSource)) {
                        logger.warn(dataSource.getClass().getName() + " is unrecogernized, it may not support XA.");
                    }
                }

                String transactionManagerBeanName = BitronixTransactionManager.class.getSimpleName();

                context.registerBeanDefinition(transactionManagerBeanName, BitronixTransactionManagerBeanDefinition());
                context.registerBeanDefinition(PlatformTransactionManager.class.getSimpleName(),
                        PlatformTransactionManagerBeanDefinition(transactionManagerBeanName));
            } else {
                logger.info("1 datasource was found.");
                context.registerBeanDefinition(DataSourceTransactionManager.class.getSimpleName(),
                        DataSourceTransactionManagerBeanDefinition());
            }

            context.refresh();
            addApplicationContext(context);
        }

    }

    protected void addApplicationContext(ApplicationContext context) {
        ApplicationContext currentApplicationContext = null;
        ApplicationContext parent = applicationContext;
        do {
            currentApplicationContext = parent;
            parent = ((AbstractApplicationContext) currentApplicationContext).getParent();
        } while (null != parent);

        ((AbstractApplicationContext) currentApplicationContext).setParent(context);
    }

    ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
