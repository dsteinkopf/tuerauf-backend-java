package net.steinkopf.tuerauf;

import org.lightadmin.api.config.LightAdmin;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

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
}
