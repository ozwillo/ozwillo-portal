package org.oasis_eu.portal.config;

import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;

import org.oasis_eu.spring.kernel.security.TokenRefreshInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.MimeMappings;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.boot.context.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.thymeleaf.extras.conditionalcomments.dialect.ConditionalCommentsDialect;

import ch.qos.logback.access.tomcat.LogbackValve;
import de.javakaffee.web.msm.MemcachedBackupSessionManager;

/**
 * User: schambon
 * Date: 6/11/14
 */
@Configuration
public class OasisWebConfiguration extends WebMvcConfigurerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(OasisWebConfiguration.class);

    @Autowired
    ApplicationContext applicationContext;

    @Value("${ha.enabled:false}") private boolean highAvailability; // are we in HA mode?
    @Value("${ha.nodes:}")   private String  nodes;            // memcached nodes eg n1:oasis-portal-1:11211,n2:oasis-portal-2:11211
    @Value("${ha.failover:}") private String  failover;         // failover node, should be the node corresponding to localhost
    
    @Value("${web.maxUploadSize:5000}") private int maxUploadSize; // only for 64x64 pngs which should be mostly below 2000B

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new OasisLocaleInterceptor());
        registry.addInterceptor(tokenRefreshInterceptor());
    }

    @Bean
    public TokenRefreshInterceptor tokenRefreshInterceptor() {
        return new TokenRefreshInterceptor();
    }

    @Bean
    public LocaleResolver localeResolver() {
        return new OasisLocaleResolver();
    }

    @Bean
    // NB. this fct name is required see http://stackoverflow.com/questions/27050018/spring-file-upload-getting-expected-multiparthttpservletrequest-is-a-multipar
    public MultipartResolver multipartResolver(){
        CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
        ///commonsMultipartResolver.setDefaultEncoding("utf-8");
        commonsMultipartResolver.setMaxUploadSize(maxUploadSize); // only for 64x64 pngs which should be mostly below 2000B
        return commonsMultipartResolver;
    }
    
    @Bean
    public ConditionalCommentsDialect conditionalCommentsDialect() {
       return new ConditionalCommentsDialect();
    }

    @Bean
    public ServletContextInitializer servletContextInitializer() {
        return (ServletContext servletContext) -> {
            final CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
            characterEncodingFilter.setEncoding("UTF-8");
            characterEncodingFilter.setForceEncoding(true);
            servletContext.addFilter("characterEncodingFilter", characterEncodingFilter).addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");
        };
    }


    @Bean
    public EmbeddedServletContainerCustomizer containerCustomizer() {
        if (highAvailability) {
            logger.info("Setting up high availability configuration");
            return factory -> {
                logger.info("Customizing Tomcat container");

                TomcatEmbeddedServletContainerFactory containerFactory = (TomcatEmbeddedServletContainerFactory) factory;
                TomcatContextCustomizer tomcatContextCustomizer = context -> {
                    context.setSessionTimeout(30);
                    context.setManager(new MemcachedBackupSessionManager() {{
                        setMemcachedNodes(nodes);
                        setFailoverNodes(failover);
                        setRequestUriIgnorePattern(".*\\.(ico|png|gif|jpg|css|js)$");
                    }});
                };
                containerFactory.setTomcatContextCustomizers(Arrays.asList(tomcatContextCustomizer));

                if (new File("./config/logback-access.xml").exists()) {
                    LogbackValve accessLogValve = new LogbackValve();
                    accessLogValve.setFilename("./config/logback-access.xml");
                    containerFactory.addContextValves(accessLogValve);
                }
                setMimeMappings(factory);
            };
        } else {
            logger.info("Skipping HA configuration");
            return factory -> {
                if (new File("./config/logback-access.xml").exists()) {
                    LogbackValve accessLogValve = new LogbackValve();
                    accessLogValve.setFilename("./config/logback-access.xml");
                    ((TomcatEmbeddedServletContainerFactory) factory).addContextValves(accessLogValve);
                }

                setMimeMappings(factory);
            };
        }
    }

    private void setMimeMappings(ConfigurableEmbeddedServletContainer factory) {
        MimeMappings mm = new MimeMappings();
        MimeMappings.DEFAULT.getAll().forEach(mapping -> mm.add(mapping.getExtension(), mapping.getMimeType()));

        mm.add("woff", "application/font-woff");
        factory.setMimeMappings(mm);
    }

}
