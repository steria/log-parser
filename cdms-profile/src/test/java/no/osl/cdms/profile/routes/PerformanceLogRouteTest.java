package no.osl.cdms.profile.routes;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.test.spring.CamelSpringTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class PerformanceLogRouteTest extends CamelSpringTestSupport {

    PerformanceLogRoute performanceLogRoute;

    @Before
    public void setup() throws Exception {
        performanceLogRoute = new PerformanceLogRoute();
        context.setTracing(true);
        context.addRoutes(performanceLogRoute);
        context.start();

    }

    @Test
    public void routeTest() throws Exception {
        assert(true);
        context.stop();
    }

    @Override
    protected AbstractApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("classpath:META-INF/spring/cdms-profile-ctx.xml",
                "classpath:test-cdms-profile-infra-ctx.xml");
    }
}