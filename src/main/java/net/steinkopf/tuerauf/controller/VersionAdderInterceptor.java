package net.steinkopf.tuerauf.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.jar.Manifest;


/**
 * Adds version info to map in every request model map.
 */
@Component
public class VersionAdderInterceptor extends HandlerInterceptorAdapter {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(VersionAdderInterceptor.class);

    @Value("${tuerauf.git-revision-hash}")
    private String gitRevisionHash;

    @Value("${tuerauf.build.timestamp}")
    private String buildTimestamp;

    @Value("${tuerauf.build.timestamp.format}")
    private String buildTimestampFormat;


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
            throws Exception {

        // if performance was an issue in this project, this should be only done once and be cached:

        final Map<String, Object> model = modelAndView.getModel();

        ServletContext context = request.getSession().getServletContext();
        // this is wrong: InputStream manifestStream = TueraufApplication.class.getClassLoader().getResourceAsStream("META-INF/MANIFEST.MF");
        InputStream manifestStream = context.getResourceAsStream("META-INF/MANIFEST.MF");
        if (manifestStream != null) {
            Manifest manifest = new Manifest(manifestStream);

            model.put("implementationTitle", manifest.getMainAttributes().getValue("Implementation-Title"));
            model.put("implementationVersion", manifest.getMainAttributes().getValue("Implementation-Version"));
            model.put("implementationJdk", manifest.getMainAttributes().getValue("Build-Jdk"));
            model.put("implementationBuild", manifest.getMainAttributes().getValue("Implementation-Build"));
            model.put("implementationBuildTime", manifest.getMainAttributes().getValue("Implementation-Build-Time"));
        }
        else {
            // fallback implementation when application is not started from war/jar:
            Date buildDate = new Date(Long.valueOf(buildTimestamp));
            SimpleDateFormat sdf = new SimpleDateFormat(buildTimestampFormat); // the format of your date
            // sdf.setTimeZone(TimeZone.getTimeZone("GMT+1")); // give a timezone reference for formatting (see comment at the bottom
            String buildDateFormatted = sdf.format(buildDate);

            model.put("implementationBuild", gitRevisionHash);
            model.put("implementationBuildTime", buildDateFormatted);
        }
    }
}

