package net.steinkopf.tuerauf;

import com.fasterxml.classmate.TypeResolver;
import net.steinkopf.tuerauf.controller.VersionAdderInterceptor;
import net.steinkopf.tuerauf.rest.AppsecretChecker;
import net.steinkopf.tuerauf.rest.FrontendAPIRestController;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.velocity.VelocityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.schema.WildcardType;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.time.LocalDate;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static springfox.documentation.schema.AlternateTypeRules.newRule;


@SpringBootApplication
@EnableGlobalMethodSecurity(securedEnabled = true)
//@Configuration
@EnableAutoConfiguration(exclude = {VelocityAutoConfiguration.class}) // suppress warnings about missing velocity templates.
//@ComponentScan
@Order(HIGHEST_PRECEDENCE)
@EnableSwagger2
public class TueraufApplication extends SpringBootServletInitializer {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(TueraufApplication.class);

/*
    // for deploying as a web module to servlet container
    public void onStartup(ServletContext servletContext) throws ServletException {

        LightAdmin.configure(servletContext)
                .basePackage("net.steinkopf.tuerauf")
                .baseUrl("/admin")
                .security(false)
                .backToSiteUrl("https://github.com/dsteinkopf/tuerauf");
        super.onStartup(servletContext);
    }
*/

/*
    // Used for running in "embedded" mode
    @Bean
    public ServletContextInitializer servletContextInitializer() {
        return new ServletContextInitializer() {
            @Override
            public void onStartup(ServletContext servletContext) throws ServletException {

                LightAdmin.configure(servletContext)
                        .basePackage("net.steinkopf.tuerauf")
                        .baseUrl("/admin")
                        .security(false)
                        .backToSiteUrl("https://github.com/dsteinkopf/tuerauf") // still a dummy
                ;

                new LightAdminWebApplicationInitializer().onStartup(servletContext);
            }
        };
    }
*/

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(TueraufApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(TueraufApplication.class, args);
    }

    @Bean
    public ApplicationSecurity applicationSecurity() {
        return new ApplicationSecurity();
    }

    //@Bean
    public WebMvcConfigurerAdapter myWebMvcConfigurerAdapter() {
        return new MyWebMvcConfigurerAdapter();
    }


    @Order(Ordered.HIGHEST_PRECEDENCE)
    @Configuration
    protected static class AuthenticationSecurity extends
            GlobalAuthenticationConfigurerAdapter {

        @Value("${tuerauf.admin-password}")
        private String adminPassword;

        @Value("${tuerauf.user-password:}")
        private String userPassword;


        @Override
        public void init(AuthenticationManagerBuilder auth) throws Exception {
            auth.inMemoryAuthentication()
                    .withUser("admin").password(adminPassword).roles("ADMIN", "USER");
            if (StringUtils.isNotEmpty(userPassword)) {
                auth.inMemoryAuthentication()
                        .withUser("user").password(userPassword).roles("USER");

            }
        }
    }

    @Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
    protected static class ApplicationSecurity extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {

            http
                    .authorizeRequests()
                            // does not work here to set ROLE_ADMIN as default. Results in any request required to be ADMIN: .antMatchers("/**").access("hasRole('ROLE_ADMIN')")
                    .antMatchers(FrontendAPIRestController.FRONTEND_URL_PATTERN).anonymous() // restricted by AppsecretChecker
                    .antMatchers(FrontendAPIRestController.FRONTEND_URL_PATTERN).permitAll() // restricted by AppsecretChecker
                    .antMatchers("/admin/**").access("hasRole('ROLE_ADMIN')")
                    .antMatchers("/users/**").access("hasRole('ROLE_ADMIN')")
                    .anyRequest().fullyAuthenticated()
                    .and().httpBasic();

            // see http://stackoverflow.com/questions/29149931/spring-boot-csrf-filter
            // completely disable CSRF. Do we need it in our case?
            http
                    .csrf().disable();
        }
    }

    // see http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#boot-features-security
    //@Configuration
    //@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
    protected static class XXApplicationSecurity extends WebSecurityConfigurerAdapter {

        @Value("${tuerauf.admin-password}")
        private String adminPassword;


        @Autowired
        private SecurityProperties security;

        //@Override
        protected void XXconfigure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                    .antMatchers("/frontend/**").access("hasRole('ROLE_USER')")
                    .antMatchers("/**").access("hasRole('ROLE_ADMIN')")
                    .and().httpBasic();
        }

        //@Override
        public void XXconfigure(AuthenticationManagerBuilder auth) throws Exception {
            auth
                    .inMemoryAuthentication()
                    .withUser("admin").password(adminPassword).roles("ADMIN", "USER");
            //.and()
            //.withUser("user").password("user").roles("USER");
        }
    }

    @Component
    protected static class MyWebMvcConfigurerAdapter extends WebMvcConfigurerAdapter {

        @Autowired
        public AppsecretChecker appsecretChecker;

        @Autowired
        public VersionAdderInterceptor versionAdderInterceptor;

        @Bean
        public AppsecretChecker appsecretChecker() {
            return appsecretChecker;
        }

        @Bean
        public VersionAdderInterceptor versionAdderInterceptor() {
            return versionAdderInterceptor;
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(appsecretChecker());
            /*registry.addInterceptor(versionAdderInterceptor());*/
        }
    }

    /**
     * Things for Swagger !
     */
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2).select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any()).build().pathMapping("/")
                .directModelSubstitute(LocalDate.class, String.class)
                .genericModelSubstitutes(ResponseEntity.class)
                .alternateTypeRules(newRule(typeResolver.resolve(DeferredResult.class, typeResolver.resolve(ResponseEntity.class, WildcardType.class)), typeResolver.resolve(WildcardType.class)))
                .useDefaultResponseMessages(false)
                .globalResponseMessage(RequestMethod.GET, newArrayList(new ResponseMessageBuilder().code(500).message("500 message").responseModel(new ModelRef("Error")).build()))
                .securitySchemes(newArrayList(apiKey())).securityContexts(newArrayList(securityContext()));
    }

    @Autowired
    private TypeResolver typeResolver;

    private ApiKey apiKey() {
        return new ApiKey("mykey", "api_key", "header");
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder().securityReferences(defaultAuth()).forPaths(PathSelectors.regex("/anyPath.*")).build();
    }

    List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return newArrayList(new SecurityReference("mykey", authorizationScopes));
    }

    @Bean
    SecurityConfiguration security() {
        return new SecurityConfiguration("test-app-client-id", "test-app-realm", "test-app", "apiKey");
    }

    @Bean
    UiConfiguration uiConfig() {
        return new UiConfiguration("validatorUrl");
    }
}
