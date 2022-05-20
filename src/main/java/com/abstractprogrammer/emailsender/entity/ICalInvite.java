package com.abstractprogrammer.emailsender.entity;

import biweekly.ICalendar;
import biweekly.component.VAlarm;
import biweekly.component.VEvent;
import biweekly.parameter.ParticipationLevel;
import biweekly.property.*;
import lombok.Data;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Data
public class ICalInvite {
    private Instant startDateTime;
    private Instant endDateTime;
    private String summary = "";
    private String description = "";
    private String location = "";
    private String organizer = "";
    private TimeZone timeZone;
    private String language = "en-us";
    private String url = "";
    private Set<String> attendees = new HashSet<>();
    private String method = Method.REQUEST;
    private Conference conference = new Conference(url);
    private Duration alarmBefore;
    private String category = "";

    public ICalInvite() {

    }

    public void setAttendees(Set<String> attendees) {
        this.attendees = attendees;
    }

    public void setAttendees(String attendee) {
        this.attendees.add(attendee);
    }

    public ICalInvite(Instant startDateTime, Instant endDateTime, String summary, String description, String location, String organizer, TimeZone timeZone, String language, String url, Set<String> attendees, String method, Conference conference, Duration alarmBefore, String category) {
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.summary = summary;
        this.description = description;
        this.location = location;
        this.organizer = organizer;
        this.timeZone = timeZone;
        this.language = language;
        this.url = url;
        this.attendees = attendees;
        this.method = method;
        this.conference = conference;
        this.alarmBefore = alarmBefore;
        this.category = category;
    }

    public ICalendar getICalendar() {
        ICalendar iCalendar = new ICalendar();
        iCalendar.setMethod(method);

        VEvent event = new VEvent();
        event.setDateStart(Date.from(startDateTime), true);
        event.setDateEnd(Date.from(endDateTime), true);

        Summary eventSummary = event.setSummary(summary);
        eventSummary.setLanguage(language);
        event.setSummary(eventSummary);
        event.setDescription(description);
        attendees.forEach(event::addAttendee);
        List<Attendee> attendeeList = event.getAttendees();
        for (Attendee a : attendeeList) {
            a.setParticipationLevel(ParticipationLevel.REQUIRED);
            a.setRsvp(true);
        }
        event.setLocation(location);
        ;
        Trigger trigger = new Trigger(Date.from(startDateTime.minus(alarmBefore)));
        VAlarm alarm = new VAlarm(Action.display(), trigger);
        alarm.setDescription("Reminder");
        event.addAlarm(alarm);
        event.setStatus(Status.confirmed());
        Organizer eventOrganizer = new Organizer("organizer", this.organizer);
        eventOrganizer.setParameter("Role", "CHAIR");
        event.setOrganizer(eventOrganizer);
        event.setPriority(5);
        event.setClassification("Public");
        event.setTransparency(Transparency.opaque());
        event.setSequence(0);
        iCalendar.addEvent(event);
        return iCalendar;
    }
}
