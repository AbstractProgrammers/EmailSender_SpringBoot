package com.abstractprogrammer.emailsender.entity;

import lombok.Data;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class Email {
    private Set<String> sendTo;
    private Set<String> ccTo;
    private Set<String> bccTo;
    private String subject;
    private String from;
    private List<File> attachments;
    private String templateName;
    private Map<String, String> templateParams;

    @Override
    public String toString() {
        return String.format("EmailContent{sendTo=%s, ccTo=%s, subject='%s', mailTemplate=%s}",
                sendTo,
                ccTo,
                subject,
                templateName);
    }

    public void setSendTo(Set<String> sendTo) {
        this.sendTo = sendTo;
    }

    public void setSendTo(String sendTo) {
        this.sendTo = new HashSet<>();
        this.sendTo.add(sendTo);
    }
}
