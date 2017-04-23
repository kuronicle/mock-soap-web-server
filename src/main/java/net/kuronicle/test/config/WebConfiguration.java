package net.kuronicle.test.config;

import javax.servlet.Servlet;

import net.kuronicle.test.servlet.MockSoapServlet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class WebConfiguration {

    @Value("${data.store.base.dir}")
    private String dataStoreBaseDir;

    @Bean
    ServletRegistrationBean statsServlet() {
        MockSoapServlet servlet = new MockSoapServlet();
        servlet.setDataStoreBaseDir(dataStoreBaseDir);
        return new ServletRegistrationBean(servlet, "/mock-soap/*");
    }
    
}
