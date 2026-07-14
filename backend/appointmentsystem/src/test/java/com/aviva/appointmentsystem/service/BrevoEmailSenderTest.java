package com.aviva.appointmentsystem.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BrevoEmailSenderTest {

    @Test
    void sendPostsExpectedRequestToBrevo() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        BrevoEmailSender sender = new BrevoEmailSender(
                builder,
                "https://api.brevo.com/v3",
                "test-api-key",
                "clnicaviva@gmail.com",
                "Clinica Aviva"
        );

        server.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("api-key", "test-api-key"))
                .andExpect(content().json("""
                        {
                          "sender": {
                            "name": "Clinica Aviva",
                            "email": "clnicaviva@gmail.com"
                          },
                          "to": [{"email": "patient@example.com"}],
                          "subject": "Cita creada",
                          "textContent": "Tu cita fue creada"
                        }
                        """))
                .andRespond(withSuccess("{\"messageId\":\"test-id\"}", MediaType.APPLICATION_JSON));

        sender.send("patient@example.com", "Cita creada", "Tu cita fue creada");

        server.verify();
    }

    @Test
    void sendRejectsMissingApiKeyBeforeCallingBrevo() {
        BrevoEmailSender sender = new BrevoEmailSender(
                RestClient.builder(),
                "https://api.brevo.com/v3",
                "",
                "clnicaviva@gmail.com",
                "Clinica Aviva"
        );

        assertThrows(
                IllegalStateException.class,
                () -> sender.send("patient@example.com", "Asunto", "Mensaje")
        );
    }

    @Test
    void sendIncludesHtmlAndPlainTextWhenTemplateIsAvailable() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        BrevoEmailSender sender = new BrevoEmailSender(
                builder,
                "https://api.brevo.com/v3",
                "test-api-key",
                "clnicaviva@gmail.com",
                "Clinica Aviva"
        );

        server.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andExpect(content().json("""
                        {
                          "subject": "Cita creada",
                          "textContent": "Tu cita fue creada",
                          "htmlContent": "<html><body>Cita creada</body></html>"
                        }
                        """))
                .andRespond(withSuccess("{\"messageId\":\"test-id\"}", MediaType.APPLICATION_JSON));

        sender.send(
                "patient@example.com",
                "Cita creada",
                "Tu cita fue creada",
                "<html><body>Cita creada</body></html>"
        );

        server.verify();
    }
}
