package com.biblioteca2020.models.service;

import java.io.ByteArrayInputStream;
import java.util.List;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

import com.biblioteca2020.models.entity.Libro;
import com.biblioteca2020.models.entity.Prestamo;
import com.biblioteca2020.models.entity.Usuario;
import com.biblioteca2020.view.pdf.GenerarReportePDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service("emailSenderService")
public class EmailSenderService {

	private JavaMailSender javaMailSender;

	@Autowired
	public EmailSenderService(JavaMailSender javaMailSender) {
		this.javaMailSender = javaMailSender;
	}

	public void setMailSender(JavaMailSender mailSender) {
		this.javaMailSender = mailSender;
	}

	// USADO
	@Async
	public void sendMail(final String from, final String to, final String subject, final String msg) {
		try {
			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setFrom(from);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(msg, true);
			helper.addInline("logo-biblioteca2020", new ClassPathResource("static/img/logo.jpg"));
			javaMailSender.send(message);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	// USADO
	@Async
	public void sendMailPrestamosWithCron(List<Prestamo> prestamos, String fecha, final String from, final String to,
			final String subject, final String msg) {
		try {
			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setFrom(from);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(msg, true);
			helper.addInline("logo-biblioteca2020", new ClassPathResource("static/img/logo.jpg"));

			ByteArrayInputStream bis = GenerarReportePDF.generarPDFPrestamos("Reporte de préstamos del último mes",
					prestamos);
			DataSource dataSource = new ByteArrayDataSource(bis, "application/pdf");

			helper.addAttachment("reporte-prestamos-" + fecha + ".pdf", dataSource);

			javaMailSender.send(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// USADO
	@Async
	public void sendMailUsuariosWithCron(List<Usuario> usuarios, String fecha, final String from, final String to,
			final String subject, final String msg) {
		try {
			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setFrom(from);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(msg, true);
			helper.addInline("logo-biblioteca2020", new ClassPathResource("static/img/logo.jpg"));

			/*
			 * ByteArrayInputStream bis =
			 * GenerarReportePDF.generarPDFUsuarios("Reporte de usuarios del último mes",
			 * usuarios); DataSource dataSource = new ByteArrayDataSource(bis,
			 * "application/pdf");
			 */
			if (usuarios.size() != 0) {
				ByteArrayInputStream bis = GenerarReportePDF.generarPDFUsuarios("Reporte de usuarios del último mes",
						usuarios);
				DataSource dataSource = new ByteArrayDataSource(bis, "application/pdf");
				helper.addAttachment("reporte-usuarios-" + fecha + ".pdf", dataSource);
			}
			javaMailSender.send(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// USADO
	@Async
	public void sendMailLibrosWithCron(Model model, List<Libro> libros, String fecha, final String from, final String to,
			final String subject, final String msg) {
		try {
			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setFrom(from);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(msg, true);
			helper.addInline("logo-biblioteca2020", new ClassPathResource("static/img/logo.jpg"));

			ByteArrayInputStream bis = GenerarReportePDF
					.generarPDFLibros(model, "Reporte de libros - stock menor a 20 unidades", libros);
			DataSource dataSource = new ByteArrayDataSource(bis, "application/pdf");

			helper.addAttachment("reporte-libros-" + fecha + ".pdf", dataSource);

			javaMailSender.send(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
