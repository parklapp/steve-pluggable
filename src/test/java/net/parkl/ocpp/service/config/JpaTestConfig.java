package net.parkl.ocpp.service.config;

import com.mysql.jdbc.Driver;
import org.hibernate.dialect.H2Dialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

import static java.util.Objects.requireNonNull;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "net.parkl.ocpp.repositories", transactionManagerRef = "ocppTransactionManager")
public class JpaTestConfig {

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setPersistenceUnitName("TestPU");
        em.setDataSource(dataSource());
        em.setPackagesToScan("net.parkl.ocpp.entities");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setShowSql(false);
        vendorAdapter.setGenerateDdl(true);
        vendorAdapter.setDatabasePlatform(H2Dialect.class.getName());

        em.setJpaVendorAdapter(vendorAdapter);
        return em;
    }

    @Bean
    @Primary
    public JpaTransactionManager ocppTransactionManager() {
        return new JpaTransactionManager(requireNonNull(entityManagerFactory().getObject()));
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName(Driver.class.getName());
        ds.setUrl("jdbc:h2:mem:steve;MODE=MySQL;DB_CLOSE_DELAY=-1;NON_KEYWORDS=VALUE");
        return ds;
    }
}
