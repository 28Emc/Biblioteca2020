package com.biblioteca2020.util.scheduler;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.biblioteca2020.models.entity.Prestamo;
import com.biblioteca2020.models.service.EmailSenderService;
import com.biblioteca2020.models.service.IPrestamoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// CLASE QUE PERMITE PROGRAMAR EVENTOS REPETITIVOS
@Component
@EnableScheduling
public class Scheduler {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    // private static final String correoDiego = "luis290613@gmail.com";

    @Autowired
    private IPrestamoService prestamoService;

    @Autowired
    private EmailSenderService emailSenderService;

    // PRUEBA 1: MOSTRAR HORA ACTUAL EN CONSOLA CADA SEGUNDO
    @Scheduled(cron = "* * * ? * *", zone = "America/Lima")
    public void prueba1() {
        System.out.println("Hora actual: " + dateFormat.format(new Date()));
    }

    // PRUEBA 2: ENVIAR CORREO DE PRÉSTAMOS TOTALES CADA 3 MINUTOS
    @Scheduled(cron = "0 */3 * ? * *", zone = "America/Lima")
    public void prueba2() {
        System.out.println("APAREZCO CADA 3 MINUTOS!!!");
    }

    // ENVIAR CORREO DE PRÉSTAMOS TOTALES CADA MES AL ADMIN
    // SE ENVÍA CADA ÚLTIMO DIA DEL MES A LAS 11 PM
    @Scheduled(cron = "0 0 8 L * ?", zone = "America/Lima")
    public void enviarEmailPrestamosTotalesMensuales() {
        // ESTABLECER DATASOURCE
        List<Prestamo> prestamos = prestamoService.fetchWithLibroWithUsuarioWithEmpleado();

        if (prestamos.size() > 0) {

            // FILTRAR SOLO LOS RESULTADOS DEL ULTIMO MES
            // O MEJOR DICHO, DEJO SOLAMENTE LOS RESULTADOS DEL ULTIMO MES
            Locale esp = new Locale("es", "PE");
            Calendar calUltimoDiaMes = Calendar.getInstance(esp);
            calUltimoDiaMes.set(Calendar.DAY_OF_MONTH, calUltimoDiaMes.getActualMaximum(Calendar.DAY_OF_MONTH));
            calUltimoDiaMes.add(Calendar.MONTH, -1);
            System.out.println(
                    "FECHA DEL ULTIMO DIA DEL MES ANTERIOR: " + calUltimoDiaMes.getTime().toString().toUpperCase());

            for (int i = 0; i < prestamos.size(); i++) {
                prestamos.removeIf(n -> n.getFecha_despacho().before(calUltimoDiaMes.getTime()));
                if (prestamos.size() == 0) {
                    System.out.println("NO HAY PRÉSTAMOS DE "
                            + LocalDate.now().getMonth().getDisplayName(TextStyle.FULL, esp).toUpperCase() + " "
                            + LocalDate.now().getYear());
                } else {
                    System.out.println("ID DE PRESTAMO DE "
                            + LocalDate.now().getMonth().getDisplayName(TextStyle.FULL, esp).toUpperCase() + " "
                            + LocalDate.now().getYear() + ": " + prestamos.get(i).getId());
                }
            }

            List<Prestamo> prestamosMesAnterior = prestamos;

            System.out.println("NRO DE PRESTAMOS DE "
                    + LocalDate.now().getMonth().getDisplayName(TextStyle.FULL, esp).toUpperCase() + " "
                    + LocalDate.now().getYear() + ": " + prestamosMesAnterior.size());

            // CREAR EMAIL Y ENVIAR AL SYSADMIN
            try {
                String message = "<html><head>" + "<meta charset='UTF-8' />"
                        + "<meta name='viewport' content='width=device-width, initial-scale=1.0' />"
                        + "<title>Reporte de Préstamos del mes de "
                        + LocalDate.now().getMonth().getDisplayName(TextStyle.FULL, esp).toUpperCase() + " "
                        + LocalDate.now().getYear() + " | Biblioteca2020</title>" + "</head>" + "<body>"
                        + "<div class='container' style='padding-top: 1rem;'>"
                        + "<img src='cid:logo-biblioteca2020' alt='logo-biblioteca2020' />"
                        + "<div class='container' style='padding-top: 5rem;'>" + "<p>Saludos, durante el mes de "
                        + LocalDate.now().getMonth().getDisplayName(TextStyle.FULL, esp).toUpperCase() + " "
                        + LocalDate.now().getYear()
                        + ", el total de préstamos registrado en nuestra base de datos es de: " + prestamos.size()
                        + ", distribuidos en todos los locales anexos.</p><br/>"
                        + "<p>Para mayor detalle, revisar el archivo adjunto en formato PDF.</p><br/>"
                        + "<p>Si usted no estaba al corriente de dicha acción, favor de notificarlo al local donde realizó la orden.</p><br/>"
                        + "<p>Si usted no es el destinatario a quien se dirige el presente correo, "
                        + "favor de contactar al remitente respondiendo al presente correo y eliminar el correo original "
                        + "incluyendo sus archivos, así como cualquier copia del mismo.</p>" + "</div>" + "</div>"
                        + "</body>"
                        + "<div class='footer' style='padding-top: 5rem; padding-bottom:1rem;'>Biblioteca ©2020</div>"
                        + "</html>";
                String fecha = LocalDate.now().getMonth().getDisplayName(TextStyle.FULL, esp) + "-"
                        + LocalDate.now().getYear();
                emailSenderService.sendMailWithCron(prestamosMesAnterior, fecha, "Biblioteca2020 <edmech25@gmail.com>",
                        "edi@live.it", "Reporte de Préstamos del Mes | Biblioteca2020", message);
                System.out.println("EMAIL ENVIADO!! EL DIA " + LocalDate.now().getDayOfMonth() + " DE "
                        + LocalDate.now().getMonth().getDisplayName(TextStyle.FULL, esp).toUpperCase() + " "
                        + LocalDate.now().getYear());
            } catch (MailException ex) {
                System.out.println(ex.getMessage());
            }

        } else {
            System.out.println("NRO DE PRESTAMOS TOTALES: " + prestamos.size());
        }
    }
}