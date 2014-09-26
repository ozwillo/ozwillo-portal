package org.oasis_eu.portal.config;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.oasis_eu.portal.dialect.CMSDialect;
import org.oasis_eu.spring.kernel.security.TokenRefreshInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ITemplateResolver;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;

/**
 * User: schambon
 * Date: 6/11/14
 */
@Configuration
public class OasisWebConfiguration extends WebMvcConfigurerAdapter {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ITemplateResolver defaultTemplateResolver;

    @Autowired
    CMSDialect cmsDialect;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
//        interceptor.setParamName("lang");
//        registry.addInterceptor(interceptor);
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
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine springTemplateEngine = new SpringTemplateEngine();
        springTemplateEngine.setTemplateResolver(defaultTemplateResolver);
        Set<IDialect> additionalDialects = new HashSet<IDialect>();
        additionalDialects.add(cmsDialect);
        springTemplateEngine.setAdditionalDialects(additionalDialects);
        return springTemplateEngine;
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
}
