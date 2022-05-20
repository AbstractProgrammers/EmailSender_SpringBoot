package com.abstractprogrammer.emailsender.service;

import biweekly.Biweekly;
import biweekly.ICalendar;
import com.abstractprogrammer.emailsender.entity.Email;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Objects;
import java.util.Properties;

@Service
public class EmailSender {
    private static final Logger logger = LogManager.getLogger(EmailSender.class);
    private final Configuration configuration;

    @Value("${spring.mail.username}")
    private String serverUserName;
    @Value("${spring.mail.password}")
    private String serverPassword;
    @Value("${spring.mail.port}")
    private String serverPort;
    @Value("${spring.mail.host}")
    private String serverHost;

    public EmailSender(Configuration configuration) {
        this.configuration = configuration;
    }

    private Session getSession() {
        // Get the Session object.
        return Session.getInstance(getProperties(),
                new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {

                        return new PasswordAuthentication(serverUserName, serverPassword);
                    }
                });
    }

    private Properties getProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", false);
        props.put("mail.smtp.starttls.enable", false);
        props.put("mail.smtp.host", serverHost);
        props.put("mail.smtp.port", serverPort);
        return props;
    }

    public boolean sendMail(Email email) throws MessagingException, TemplateException, IOException {
        if (Objects.isNull(email) || !StringUtils.hasLength(email.getFrom())) {
            logger.error("Email is null or sender is empty");
            return false;
        }
        try {
            javax.mail.Message message = new MimeMessage(getSession());
            message.setFrom(new InternetAddress(email.getFrom()));
            if (email.getSendTo() != null && !email.getSendTo().isEmpty()) {
                String sendTo = email.getSendTo().toString();
                sendTo = sendTo.substring(1, sendTo.length() - 1);
                logger.info("Send To :- {}", sendTo);
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(sendTo));
                if (email.getCcTo() != null && !email.getCcTo().isEmpty()) {
                    String ccSendTo = email.getCcTo().toString();
                    ccSendTo = ccSendTo.substring(1, ccSendTo.length() - 1);
                    logger.info("Send Cc :- {}", ccSendTo);
                    message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ccSendTo));
                }
                if (email.getBccTo() != null && !email.getBccTo().isEmpty()) {
                    String bccSendTo = email.getBccTo().toString();
                    bccSendTo = bccSendTo.substring(1, bccSendTo.length() - 1);
                    logger.info("Send Bcc :- {}", bccSendTo);
                    message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bccSendTo));
                }
                message.setSubject(email.getSubject());
                if (email.getCalendar() != null) {
                    ICalendar iCalendar = email.getCalendar();
                    iCalendar.getEvents().get(0).setDescription(getEmailContent(email));
                    MimeBodyPart calBodyPart = new MimeBodyPart();

                    calBodyPart.setHeader("Content-Class", "urn:content-classes:calendarmessage");
                    calBodyPart.setHeader("Content-ID", "calendar_message");
                    calBodyPart.setDataHandler(new DataHandler(
                            new ByteArrayDataSource(Biweekly.write(iCalendar).go(),
                                    String.format("text/calendar;method=%s;name=\"invite.ics\"",
                                            "REQUEST"))));

                    MimeMultipart multipart = new MimeMultipart();
                    BodyPart messageBodyPart = new MimeBodyPart();
                    messageBodyPart.setContent(getEmailContent(email), "text/html; charset=utf-8");
                    multipart.addBodyPart(messageBodyPart);
                    multipart.addBodyPart(calBodyPart);
                    message.setContent(multipart);
                }
                else if (email.getAttachments() != null && !email.getAttachments().isEmpty()) {
                    Multipart multipart = new MimeMultipart();
                    BodyPart messageBodyPart = new MimeBodyPart();
                    messageBodyPart.setContent(getEmailContent(email), "text/html; charset=utf-8");
                    email.getAttachments().forEach(attachment -> {
                        MimeBodyPart attachmentPart = new MimeBodyPart();
                        try {
                            attachmentPart.attachFile(attachment);
                            multipart.addBodyPart(attachmentPart);
                        } catch (IOException | MessagingException e) {
                            logger.info("Exception while attaching file {} :- {}", attachment.getName(), e.getMessage());
                        }

                    });
                    multipart.addBodyPart(messageBodyPart);
                    message.setContent(multipart);
                } else {
                    message.setContent(getEmailContent(email), "text/html; charset=utf-8");
                }
                Transport.send(message);
                logger.info("Email Sent Successfully.");
                return true;
            } else {
                logger.error("Email Send Failed. No Recipient Found.");
                return false;
            }
        } catch (MessagingException | IOException | TemplateException e) {
            e.printStackTrace();
            logger.error("Email Send Failed. {}", e.getMessage());
            throw e;
        }

    }

    String getEmailContent(Email email) throws IOException, TemplateException {
        StringWriter stringWriter = new StringWriter();
        configuration.getTemplate(email.getTemplateName()).process(
                email.getTemplateParams(), stringWriter);
        return stringWriter.getBuffer().toString();
    }
}
