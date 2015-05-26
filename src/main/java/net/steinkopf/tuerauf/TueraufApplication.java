package net.steinkopf.tuerauf;

import org.lightadmin.api.config.LightAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

@SpringBootApplication
//@Configuration
//@EnableAutoConfiguration
//@ComponentScan
public class TueraufApplication extends SpringBootServletInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
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


    @Configuration
    @Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
    protected static class ApplicationSecurity extends WebSecurityConfigurerAdapter {

        @Autowired
        private SecurityProperties security;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                    .antMatchers("/frontend/**").access("hasRole('ROLE_USER')")
                    .antMatchers("/**").access("hasRole('ROLE_ADMIN')")
                    .and().httpBasic();
        }

        @Override
        public void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.inMemoryAuthentication()
                    .withUser("admin").password("admin").roles("ADMIN", "USER").and()
                    .withUser("user").password("user").roles("USER");
        }

    }
}
