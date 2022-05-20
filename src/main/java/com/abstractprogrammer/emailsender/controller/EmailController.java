package com.abstractprogrammer.emailsender.controller;

import com.abstractprogrammer.emailsender.entity.Email;
import com.abstractprogrammer.emailsender.entity.ICalInvite;
import com.abstractprogrammer.emailsender.service.EmailSender;
import freemarker.template.TemplateException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
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
        email.setCalendar(createInvite().getICalendar());
        return emailSender.sendMail(email);
    }

    ICalInvite createInvite() {
        ICalInvite iCalInvite = new ICalInvite();
        iCalInvite.setSummary("Test invite");
        iCalInvite.setCategory("Meeting invites");
        iCalInvite.setDescription("This is a test meeting invite.");
        iCalInvite.setStartDateTime(ZonedDateTime.now().toInstant());
        iCalInvite.setEndDateTime(ZonedDateTime.now().toInstant());
        Duration alarmBefore = Duration.ofMinutes(10);
        iCalInvite.setAlarmBefore(alarmBefore);
        iCalInvite.setAttendees("emailDomain@domain.com");
        iCalInvite.setLocation("India");
        iCalInvite.setOrganizer("AbstractProgrammers");
        return iCalInvite;
    }
}
