package net.steinkopf.tuerauf;

import org.lightadmin.api.config.LightAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

@SpringBootApplication
@EnableGlobalMethodSecurity(securedEnabled = true)
//@Configuration
//@EnableAutoConfiguration
//@ComponentScan
public class TueraufApplication extends SpringBootServletInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {

        // see also class UserAdministration

        LightAdmin.configure(servletContext)
                .basePackage("net.steinkopf.tuerauf")
                .baseUrl("/admin")
                .security(false)
                .backToSiteUrl("http://lightadmin.org");
        super.onStartup(servletContext);
    }

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


    @Order(Ordered.HIGHEST_PRECEDENCE)
    @Configuration
    protected static class AuthenticationSecurity extends
            GlobalAuthenticationConfigurerAdapter {

        @Override
        public void init(AuthenticationManagerBuilder auth) throws Exception {
            auth.inMemoryAuthentication()
                    .withUser("admin").password("admin").roles("ADMIN", "USER").and()
                    .withUser("user").password("user").roles("USER");
        }
    }

    @Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
    protected static class ApplicationSecurity extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http    .authorizeRequests()
                    .anyRequest().fullyAuthenticated()
                    .and().httpBasic();
        }
    }

    // see http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#boot-features-security
    //@Configuration
    //@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
    protected static class XXApplicationSecurity extends WebSecurityConfigurerAdapter {

        @Autowired
        private SecurityProperties security;

        //@Override
        protected void XXconfigure(HttpSecurity http) throws Exception {
            http    .authorizeRequests()
                    .antMatchers("/frontend/**").access("hasRole('ROLE_USER')")
                    .antMatchers("/**").access("hasRole('ROLE_ADMIN')")
                    .and().httpBasic();
        }

        //@Override
        public void XXconfigure(AuthenticationManagerBuilder auth) throws Exception {
            auth
                    .inMemoryAuthentication()
                    .withUser("admin").password("admin").roles("ADMIN", "USER").and()
                    .withUser("user").password("user").roles("USER");
        }
    }
}
