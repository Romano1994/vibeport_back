package com.vibeport.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.util.Properties;

@Component
public class MailSMTP {

    @Value("${mail.smtp.host}")
    private String smtpHost;

    @Value("${mail.smtp.port}")
    private String smtpPort;

    @Value("${mail.smtp.from}")
    private String smtpFrom;

    @Value("${mail.smtp.pwd}")
    private String smtpPwd;

    @Autowired
    private ResourceLoader resourceLoader;

    public void sendVerificationEmail(String email, String verificationCode) {

        Properties props = new Properties();

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpFrom, smtpPwd);
            }
        });
    }
}
