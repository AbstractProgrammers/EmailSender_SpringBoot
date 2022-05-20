package com.abstractprogrammer.emailsender.controller;

import com.abstractprogrammer.emailsender.entity.Email;
import com.abstractprogrammer.emailsender.service.EmailSender;
import freemarker.template.TemplateException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.HashMap;

@RestController
public class EmailController {

    final
    EmailSender emailSender;

    public EmailController(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @GetMapping("/send")
    private boolean sendMail() throws MessagingException, TemplateException, IOException {
        Email email = new Email();
        email.setFrom("demoMail@gmail.com");
        email.setSendTo("email@domain.com");
        email.setSubject("Test Mail");
        email.setTemplateName("email.ftlh"); //template should be present in src/resources/templates
        HashMap<String, String> map = new HashMap<>();
        map.put("name", "world");
        email.setTemplateParams(map);
        return emailSender.sendMail(email);
    }
}
