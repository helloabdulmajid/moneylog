package in.abdulmajid.moneylog.auth.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private static final String PRIMARY = "#4c6ef5";
    private static final String PRIMARY_DARK = "#4263eb";
    private static final String BG = "#f4f6fb";
    private static final String CARD = "#ffffff";
    private static final String TEXT = "#1f2937";
    private static final String MUTED = "#6b7280";
    private static final String BORDER = "#e5e7eb";
    private static final String WARNING_BG = "#fff7f0";
    private static final String WARNING_BORDER = "#ffd7b0";

    private final JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:5173}")
    private String baseUrl;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.mail.log-fallback:false}")
    private boolean logFallback;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String recipientName, String recipientEmail, String token) {
        String link = baseUrl + "/verify-email?token=" + token;
        String body = emailShell(
                "Confirm your email address",
                "Take the next step with MoneyLog.",
                buildHeader("Email confirmation", "mail", null),
                hero(
                        "Confirm your email address",
                        "Hey " + recipientName + ", welcome to MoneyLog! To activate your account and start getting a clear picture of your money, please confirm your email address.",
                        null
                ),
                primaryCta("Confirm email address", link),
                fallbackLink("or paste this link in your browser", link),
                noteBox(
                        "This confirmation link will expire in <b>24 hours</b>. If you didn't create this account, you can safely ignore this email.",
                        "You can manage your email preferences anytime from your profile settings.",
                        "mail"
                )
        );
        send(recipientName, recipientEmail, "Confirm your email address", "Welcome to MoneyLog! Confirm your email to activate your account.\n\n" + link, body);
    }

    public void sendWelcomeEmail(String recipientName, String recipientEmail) {
        String appLink = baseUrl + "/app";
        String body = emailShell(
                "You're all set, " + recipientName + "!",
                "Your MoneyLog account is ready to go.",
                buildHeader("Welcome to MoneyLog", "check", null),
                hero(
                        "You're all set, " + recipientName + "!",
                        "Your account has been verified and is ready to use. Add your first expense, split a bill with friends, or organize your payments - it all takes less than a minute.",
                        null
                ),
                primaryCta("Start using MoneyLog", appLink),
                fallbackLink("or open the app in your browser", appLink),
                featuresRow() + noteBox(
                        "Take a moment to <b>review your profile settings</b> - you can set your preferred currency, timezone and theme any time.",
                        null,
                        "info"
                )
        );
        send(recipientName, recipientEmail, "Welcome to MoneyLog!", "Your MoneyLog account has been verified and is ready to use. Start tracking: " + appLink, body);
    }

    public void sendPasswordResetEmail(String recipientName, String recipientEmail, String token) {
        String link = baseUrl + "/reset-password?token=" + token;
        String body = emailShell(
                "Reset your password",
                "We'll have you signed in again in no time.",
                buildHeader("Password reset", "lock", null),
                hero(
                        "Reset your password",
                        "Hey " + recipientName + ", we received a request to reset the password for your MoneyLog account. If this was you, create a new password with the button below.",
                        null
                ),
                primaryCta("Reset password", link),
                fallbackLink("or paste this link in your browser", link),
                noteBox(
                        "This reset link will expire in <b>1 hour</b>. If you didn't request this, you can safely ignore this email - your password will remain unchanged.",
                        null,
                        "lock"
                )
        );
        send(recipientName, recipientEmail, "Reset your password", "Reset your MoneyLog password. This link expires in 1 hour: " + link, body);
    }

    private void send(String recipientName, String to, String subject, String plainText, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("MoneyLog <" + fromAddress + ">");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(plainText, htmlBody);
            mailSender.send(message);
        } catch (MailException | jakarta.mail.MessagingException e) {
            log.warn("Failed to send email to {}: {}", to, e.getMessage());
            if (logFallback) {
                log.info("=== MoneyLog Email (DEV FALLBACK) ===\nTo: {}\nSubject: {}\nBody:\n{}\n===============================",
                        to, subject, htmlBody);
            } else {
                throw new RuntimeException("Failed to send email", e);
            }
        }
    }

    private String emailShell(String title, String preheader, String headerHtml, String heroHtml, String ctaHtml, String linkRowHtml, String footerHtml) {
        return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\">"
                + "<head>"
                + "<meta charset=\"utf-8\">"
                + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">"
                + "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "<title>" + title + "</title>"
                + "<meta name=\"color-scheme\" content=\"light\">"
                + "<meta name=\"supported-color-schemes\" content=\"light\">"
                + "<style>"
                + "@media only screen and (max-width: 600px) {"
                + "  .wrapper { width: 100% !important; }"
                + "  .card { border-radius: 0 !important; }"
                + "  .features { display: inline-block !important; width: 100% !important; }"
                + "#preview { display: none !important; }"
                + "}"
                + "</style>"
                + "</head>"
                + "<body style=\"margin:0; padding:0; background:" + BG + ";\" bgcolor=\"" + BG + "\">"
                + "<div id=\"preview\" style=\"display:none; max-height:0; overflow:hidden;\">" + preheader + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div>"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" bgcolor=\"" + BG + "\" style=\"background:" + BG + ";\">"
                + "<tr><td align=\"center\" style=\"padding:32px 16px;\">"
                + "<table role=\"presentation\" class=\"wrapper\" width=\"600\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"width:600px; max-width:600px;\">"
                + "<tr><td style=\"padding:0 0 20px 0; text-align:center;\">"
                + brand()
                + "</td></tr>"
                + "<tr><td>"
                + "<table role=\"presentation\" class=\"card\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"background:" + CARD + "; border:1px solid " + BORDER + "; border-radius:16px; overflow:hidden;\" bgcolor=\"" + CARD + "\">"
                + headerHtml
                + "<tr><td style=\"padding:8px 40px 0 40px;\">"
                + heroHtml
                + "</td></tr>"
                + (ctaHtml != null ? "<tr><td style=\"padding:24px 40px 0 40px;\">" + ctaHtml + "</td></tr>" : "")
                + (linkRowHtml != null ? "<tr><td style=\"padding:14px 40px 0 40px;\">" + linkRowHtml + "</td></tr>" : "")
                + "<tr><td style=\"padding:8px 0 0 0;\">&nbsp;</td></tr>"
                + "</table>"
                + "</td></tr>"
                + "<tr><td style=\"padding:24px 16px 0 16px; text-align:center; font-family:Arial, Helvetica, sans-serif; font-size:12px; line-height:18px; color:" + MUTED + ";\">"
                + "You received this email because you're registered on MoneyLog.<br>"
                + "If you have questions, reply to this email and we'll get back to you."
                + "</td></tr>"
                + "</table>"
                + "</td></tr>"
                + "</table>"
                + "</body></html>";
    }

    private String brand() {
        return "<table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" align=\"center\">"
                + "<tr>"
                + "<td width=\"34\" height=\"34\" align=\"center\" valign=\"middle\" style=\"border-radius:9px; background:" + PRIMARY + "; font-family:Arial,Helvetica,sans-serif; font-size:17px; font-weight:bold; color:#ffffff;\" bgcolor=\"" + PRIMARY + "\">M</td>"
                + "<td style=\"padding-left:10px; font-family:Arial,Helvetica,sans-serif; font-size:18px; font-weight:bold; color:" + TEXT + ";\">Money<span style=\"color:" + PRIMARY + ";\">Log</span></td>"
                + "</tr>"
                + "</table>";
    }

    private String buildHeader(String eyebrow, String icon, String iconColor) {
        String badge = badge(icon);
        return "<tr><td style=\"padding:32px 40px 0 40px;\">"
                + (badge != null ? "<table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" align=\"center\"><tr>" + badgeCell(badge, iconColor) + "</tr></table>"
                        + "<div style=\"text-align:center; font-family:Arial,Helvetica,sans-serif; font-size:12px; font-weight:600; letter-spacing:1px; text-transform:uppercase; color:" + MUTED + "; margin-top:16px;\">" + eyebrow + "</div>" : "")
                + "</td></tr>";
    }

    private String hero(String heading, String body, String extraHtml) {
        return "<div style=\"text-align:center; font-family:Arial,Helvetica,sans-serif;\">"
                + "<div style=\"font-size:24px; line-height:32px; font-weight:bold; color:" + TEXT + ";\">" + heading + "</div>"
                + "<div style=\"font-size:15px; line-height:23px; color:" + MUTED + "; margin-top:14px;\">" + body + "</div>"
                + (extraHtml != null ? extraHtml : "")
                + "</div>";
    }

    private String primaryCta(String label, String link) {
        return "<table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" align=\"center\">"
                + "<tr><td align=\"center\" style=\"border-radius:10px; background:" + PRIMARY + ";\" bgcolor=\"" + PRIMARY + "\">"
                + "<a href=\"" + link + "\" style=\"display:inline-block; padding:13px 30px; font-family:Arial,Helvetica,sans-serif; font-size:15px; font-weight:bold; color:#ffffff; text-decoration:none; border-radius:10px; background:" + PRIMARY + ";\">" + label + "</a>"
                + "</td></tr>"
                + "</table>";
    }

    private String fallbackLink(String intro, String link) {
        return "<div style=\"text-align:center; font-family:Arial,Helvetica,sans-serif; font-size:12px; color:" + MUTED + ";\">"
                + intro + "<br>"
                + "<a href=\"" + link + "\" style=\"color:" + PRIMARY_DARK + "; text-decoration:none; word-break:break-all; font-weight:bold;\">" + link + "</a>"
                + "</div>";
    }

    private String noteBox(String messageHtml, String extraHtml, String icon) {
        return "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"margin-top:30px; background:" + WARNING_BG + "; border:1px solid " + WARNING_BORDER + "; border-radius:12px;\" bgcolor=\"" + WARNING_BG + "\">"
                + "<tr><td style=\"padding:16px 18px; font-family:Arial,Helvetica,sans-serif; font-size:13px; line-height:20px; color:#7c4a03;\">"
                + (icon != null ? iconText(icon) + " " : "")
                + messageHtml
                + (extraHtml != null ? "<div style=\"margin-top:8px;\">" + extraHtml + "</div>" : "")
                + "</td></tr>"
                + "</table>";
    }

    private String featuresRow() {
        String[][] items = {
                {"Track expenses", "Record spending the moment it happens."},
                {"Split bills", "Share costs with friends easily."},
                {"Stay organized", "Payments, cards and categories in one place."}
        };
        StringBuilder row = new StringBuilder();
        row.append("<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"margin-top:28px; border-top:1px solid " + BORDER + "; border-bottom:1px solid " + BORDER + ";\" >");
        row.append("<tr>");
        boolean first = true;
        for (String[] item : items) {
            row.append("<td class=\"features\" width=\"33%\" valign=\"top\" style=\"padding:18px 12px; font-family:Arial,Helvetica,sans-serif; text-align:center;");
            if (!first) row.append(" border-left:1px solid " + BORDER + ";");
            row.append("\">");
            row.append("<div style=\"width:32px; height:32px; margin:0 auto 10px auto; border-radius:8px; background:" + PRIMARY + "; color:#ffffff; font-size:17px; font-weight:bold; text-align:center; line-height:32px;\">" + "&#10004;" + "</div>");
            row.append("<div style=\"font-size:13px; font-weight:bold; color:" + TEXT + ";\">" + item[0] + "</div>");
            row.append("<div style=\"font-size:11px; line-height:16px; color:" + MUTED + "; margin-top:4px;\">" + item[1] + "</div>");
            row.append("</td>");
            first = false;
        }
        row.append("</tr></table>");
        return row.toString();
    }

    private String badge(String icon) {
        if (icon == null) return null;
        String glyph;
        switch (icon) {
            case "mail": glyph = "&#9993;"; break;
            case "check": glyph = "&#10003;"; break;
            case "lock": glyph = "&#128274;"; break;
            default: return null;
        }
        return glyph;
    }

    private String badgeCell(String glyph, String iconColor) {
        String bg = iconColor != null ? iconColor : PRIMARY;
        return "<td width=\"56\" height=\"56\" align=\"center\" style=\"border-radius:14px; background:" + bg + "; font-family:Arial,Helvetica,sans-serif; font-size:26px; line-height:56px; color:#ffffff;\" bgcolor=\"" + bg + "\">" + glyph + "</td>";
    }

    private String iconText(String icon) {
        switch (icon) {
            case "mail": return "";
            case "lock": return "";
            default: return "";
        }
    }
}