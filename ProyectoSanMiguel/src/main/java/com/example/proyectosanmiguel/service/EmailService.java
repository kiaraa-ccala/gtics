package com.example.proyectosanmiguel.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {
    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.sendgrid.api-key}")
    private String sendGridApiKey;

    public void enviarEmail(String destinatario, String asunto, String plantilla, Map<String, Object> variables) throws IOException {

        Context context = new Context();
        context.setVariables(variables);

        String contenidoHtml = templateEngine.process(plantilla, context);

        Email from = new Email("no-replay@deportesanmiguelpucp.online","Municipalidad de San Miguel - Deportes"); // remitente autorizado
        Email to = new Email(destinatario);
        Content content = new Content("text/html", contenidoHtml);
        Mail mail = new Mail(from, asunto, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);

            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody());
            System.out.println("Headers: " + response.getHeaders());

        } catch (IOException ex) {
            throw ex;
        }
    }

}
