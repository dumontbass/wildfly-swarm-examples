package org.wildfly.swarm.examples.jpa;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.datasources.Datasource;
import org.wildfly.swarm.datasources.DatasourcesFraction;
import org.wildfly.swarm.datasources.Driver;
import org.wildfly.swarm.jpa.JPAFraction;
import org.wildfly.swarm.undertow.WARArchive;

/**
 * @author Ken Finnigan
 */
public class Main {
    public static void main(String[] args) throws Exception {
        Container container = new Container();

        container.subsystem(new DatasourcesFraction()
                        .driver(new Driver("h2")
                                .datasourceClassName("org.h2.Driver")
                                .xaDatasourceClassName("org.h2.jdbcx.JdbcDataSource")
                                .module("com.h2database.h2"))
                        .datasource(new Datasource("MyDS")
                                .driver("h2")
                                .connectionURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
                                .authentication("sa", "sa"))
        );

        // Prevent JPA Fraction from installing it's default datasource fraction
        container.fraction(new JPAFraction()
                        .inhibitDefaultDatasource()
                        .defaultDatasourceName("MyDS")
        );

        container.start();

        WARArchive deployment = ShrinkWrap.create(WARArchive.class);
        deployment.addClasses(Employee.class);
        deployment.addClass(EmployeeServlet.class);
        deployment.addAsWebInfResource(new ClassLoaderAsset("META-INF/persistence.xml", Main.class.getClassLoader()), "classes/META-INF/persistence.xml");
        deployment.addAsWebInfResource(new ClassLoaderAsset("META-INF/load.sql", Main.class.getClassLoader()), "classes/META-INF/load.sql");
        deployment.addAllDependencies();

        container.deploy(deployment);
    }
}
