package fr.cactus.service.global;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EmailService {

    @Inject
    Mailer mailer;

    public void sendText(
            String recipient,
            String subject,
            String content
    ) {
        mailer.send(
                Mail.withText(
                        recipient,
                        subject,
                        content
                )
        );
    }
}