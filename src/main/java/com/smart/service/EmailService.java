package com.smart.service;

import java.util.Properties;

import org.springframework.stereotype.Service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

	//This method is responsible to send email
			public  boolean sendEmail(String message, String subject, String to) {
				// TODO Auto-generated method stub
				boolean f=false;
				String from="roshnigupta202003@gmail.com";

				//Variable for gmail
				String host="smtp.gmail.com";
				
				//Get system properties
				Properties properties = System.getProperties();
				System.out.println("PROPERTIES :"+properties);
				
				//Setting important information to properties object
				
				//host set
				properties.put("mail.smtp.host",host);
				properties.put("mail.smtp.port", "465");
				properties.put("mail.smtp.ssl.enable", "true");
				properties.put("mail.smtp.auth", "true");
				
				//Step1 : To get session object
				Session session=Session.getInstance(properties,new Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication("roshnigupta202003@gmail.com","opsppdqcioltbwnb" );
					}
					
				});
				
				session.setDebug(true);
				//Step 2:compose the message
				MimeMessage m=new MimeMessage(session);
				
				try {
					m.setFrom(from);
					m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
					m.setSubject(subject);
					//m.setText(message);
					m.setContent(message, "text/html");
					//Step 3 :send message using Transport class
					Transport.send(m);
					System.out.println("Mail sent .Success..........");
					f=true;
				} catch (Exception e) {
					// TODO: handle exception
				}
				return f;
			}

}
