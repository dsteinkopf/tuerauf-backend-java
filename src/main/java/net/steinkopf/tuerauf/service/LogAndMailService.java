package net.steinkopf.tuerauf.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Logging and sending admin mails.
 */
@Service
public class LogAndMailService {

    private static final Logger logger = LoggerFactory.getLogger(LogAndMailService.class);


    @Value("${tuerauf.admin-mail-address}")
    private String adminMailAddress;

    @Autowired
    private JavaMailSender javaMailSender;

    private ExecutorService executor;


    LogAndMailService() {

        executor = Executors.newCachedThreadPool();
        // where? executor.shutdown();
    }

    /**
     * Blocks until all tasks have completed execution
     * @throws InterruptedException
     */
    public void awaitTermination() throws InterruptedException {
        executor.awaitTermination(200L, TimeUnit.MILLISECONDS);
    }

    public void logAndMail(String format, Object... arguments) {

        final String message = String.format(format, arguments);
        logger.warn(message);

        CompletableFuture.supplyAsync(() -> {
            sendMail(adminMailAddress, "mail from tuerauf service", message);
            return null;
        }, executor)
        .exceptionally(ex -> {
            logger.error("exception caught when sending mail: " + ex.getMessage());
            return null;
        });
    }

    /**
     * This method will send compose and send the message
     * */
    public void sendMail(String to, String subject, String body)
    {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        javaMailSender.send(message);
    }
}
