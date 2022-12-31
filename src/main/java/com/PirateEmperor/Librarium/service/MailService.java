package com.PirateEmperor.Librarium.service;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.PirateEmperor.Librarium.model.User;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailService {
	@Autowired
	private JavaMailSender mailSender;

	@Value("${spring.mail.username}")
	private String springMailUsername;

	@Value("${front.end.url}")
	private String frontEndUrl;

	public void sendOrderInfoEmail(String username, String emailTo, Long orderId, String password)
			throws MessagingException, UnsupportedEncodingException {
		String toAddress = emailTo;
		String fromAddress = springMailUsername;
		String senderName = "No reply";
		String subject = "Your order information";
		String content = "Dear [[name]],<br><br>"
				+ "You have just made an order in our bookstore!<br> Using the following order number and password you can get your order information on the website:<br>"
				+ "<h4>ORDER NUMBER: [[ORDERID]]</h4>" + "<h4>ORDER PASSWORD: [[ORDERPASS]]</h4>"
				+ "If you have any questions or you want to change order information please contact us through the email spotted on the website footer<br><br>"
				+ "Thank you for choosing us,<br>" + "AXOS inc.";

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom(fromAddress, senderName);
		helper.setTo(toAddress);
		helper.setSubject(subject);

		content = content.replace("[[name]]", username);

		content = content.replace("[[ORDERID]]", orderId.toString());

		content = content.replace("[[ORDERPASS]]", password);

		helper.setText(content, true);

		mailSender.send(message);
	}

	public void sendVerificationEmail(User user) throws MessagingException, UnsupportedEncodingException {
		String toAddress = user.getEmail();
		String fromAddress = springMailUsername;
		String senderName = "No reply";
		String subject = "Bookstore verification link";
		String content = "Dear [[name]],<br><br>"
				+ "This is the automatically generated message, please don't reply. To verify your bookstore account click the link below:<br><br>"
				+ "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFY BOOKSTORE ACCOUNT</a></h3>" + "Thank you,<br>"
				+ "AXOS inc.";
		String endpoint = "/?token=";

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom(fromAddress, senderName);
		helper.setTo(toAddress);
		helper.setSubject(subject);

		content = content.replace("[[name]]", user.getUsername());
		String mainURL = frontEndUrl + endpoint + user.getVerificationCode();

		content = content.replace("[[URL]]", mainURL);

		helper.setText(content, true);

		mailSender.send(message);
	}

	public void sendPasswordEmail(User user, String password) throws MessagingException, UnsupportedEncodingException {
		String toAddress = user.getEmail();
		String fromAddress = springMailUsername;
		String senderName = "No reply";
		String subject = "Reset password";
		String content = "Dear [[name]],<br><br>" + "Here is your new password for your bookstore account:<br><br>"
				+ "<h3>[[PASSWORD]]</h3>" + "Thank you,<br>" + "AXOS inc.";

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom(fromAddress, senderName);
		helper.setTo(toAddress);
		helper.setSubject(subject);

		content = content.replace("[[name]]", user.getUsername());

		content = content.replace("[[PASSWORD]]", password);

		helper.setText(content, true);

		mailSender.send(message);
	}

	public void sendOrderEmailChanged(String firstname, String emailTo, Long orderId)
			throws MessagingException, UnsupportedEncodingException {
		String toAddress = emailTo;
		String fromAddress = springMailUsername;
		String senderName = "No reply";
		String subject = "Your order email changed";
		String content = "Dear [[name]],<br><br>"
				+ "Your order email address was changed!<br>From now on you will be receiving all information via this email address<br> Using the following order number you can get your order information on the website:<br>"
				+ "<h4>ORDER NUMBER: [[ORDERID]]</h4>"
				+ "If you have any questions or you want to change inforamtion please contact us through the email spotted on the website footer<br><br>"
				+ "Thank you for choosing us,<br>" + "AXOS inc.";

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom(fromAddress, senderName);
		helper.setTo(toAddress);
		helper.setSubject(subject);

		content = content.replace("[[name]]", firstname);

		content = content.replace("[[ORDERID]]", orderId.toString());

		helper.setText(content, true);

		mailSender.send(message);
	}

	public void sendStatusChangeEmail(String firstname, String emailTo, Long orderId, String status)
			throws MessagingException, UnsupportedEncodingException {
		String toAddress = emailTo;
		String fromAddress = springMailUsername;
		String senderName = "No reply";
		String subject = "Your order status has changed";
		String content = "Dear [[name]],<br><br>"
				+ "Your order status has changed!<br>Your current order status is now: <h5>[[status]]</h5><br> Using the following order number you can get your order information on the website:<br>"
				+ "<h4>ORDER NUMBER: [[ORDERID]]</h4>"
				+ "If you have any questions or you want to change inforamtion please contact us through the email spotted on the website footer<br><br>"
				+ "Thank you for choosing us,<br>" + "AXOS inc.";

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom(fromAddress, senderName);
		helper.setTo(toAddress);
		helper.setSubject(subject);

		content = content.replace("[[name]]", firstname);

		content = content.replace("[[status]]", status);

		content = content.replace("[[ORDERID]]", orderId.toString());

		helper.setText(content, true);

		mailSender.send(message);
	}
}
