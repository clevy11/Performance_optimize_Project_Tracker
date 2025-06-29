package com.example.clb.projecttracker.service;


/**
 * Service interface for sending emails.
 */
public interface EmailService {


    void sendSimpleMessage(String to, String subject, String body);


    void sendHtmlMessage(String to, String subject, String htmlBody);
}