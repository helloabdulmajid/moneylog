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

    /* ── Chai Ledger palette ── */
    private static final String PAPER = "#F6F1E6";
    private static final String PAPER_DEEP = "#EFE7D8";
    private static final String CARD = "#FFFDF6";
    private static final String FOREST = "#1E4637";
    private static final String FOREST_DARK = "#173B2E";
    private static final String PINE = "#3E7A63";
    private static final String MINT = "#E2ECE3";
    private static final String SAGE = "#ECF1E7";
    private static final String ROSE = "#F3E2D6";
    private static final String SIENNA = "#B4501E";
    private static final String INK = "#211B11";
    private static final String MUTED = "#6E675A";
    private static final String BORDER = "#E7E0D0";
    private static final String CREAM = "#F6F1E6";
    private static final String MINT_ON_FOREST = "#A7C4B8";
    private static final String DISPLAY = "'Space Grotesk','Segoe UI',Arial,Helvetica,sans-serif";
    private static final String LEDGER = "'IBM Plex Mono',Consolas,monospace";

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
                + "@import url('https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=IBM+Plex+Mono:wght@500&display=swap');"
                + "@media only screen and (max-width: 600px) {"
                + "  .wrapper { width: 100% !important; }"
                + "  .card { border-radius: 14px !important; }"
                + "  .features { display: inline-block !important; width: 100% !important; border-left: 0 !important; border-top: 1px solid " + BORDER + " !important; }"
                + "  .features-first { border-top: 0 !important; }"
                + "  .pad { padding-left: 24px !important; padding-right: 24px !important; }"
                + "#preview { display: none !important; }"
                + "}"
                + "</style>"
                + "</head>"
                + "<body style=\"margin:0; padding:0; background:" + PAPER + ";\" bgcolor=\"" + PAPER + "\">"
                + "<div id=\"preview\" style=\"display:none; max-height:0; overflow:hidden;\">" + preheader + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div>"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" bgcolor=\"" + PAPER + "\" style=\"background:" + PAPER + ";\">"
                + "<tr><td align=\"center\" style=\"padding:32px 16px;\">"
                + "<table role=\"presentation\" class=\"wrapper\" width=\"600\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"width:600px; max-width:600px;\">"
                + "<tr><td>"
                + "<table role=\"presentation\" class=\"card\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"background:" + CARD + "; border:1px solid " + BORDER + "; border-radius:18px; overflow:hidden;\" bgcolor=\"" + CARD + "\">"
                + brand()
                + headerHtml
                + "<tr><td class=\"pad\" style=\"padding:6px 40px 0 40px;\">"
                + heroHtml
                + "</td></tr>"
                + (ctaHtml != null ? "<tr><td class=\"pad\" style=\"padding:26px 40px 0 40px;\">" + ctaHtml + "</td></tr>" : "")
                + (linkRowHtml != null ? "<tr><td class=\"pad\" style=\"padding:16px 40px 0 40px;\">" + linkRowHtml + "</td></tr>" : "")
                + (footerHtml != null ? "<tr><td class=\"pad\" style=\"padding:10px 40px 4px 40px;\">" + footerHtml + "</td></tr>" : "")
                + "<tr><td style=\"padding:6px 0 0 0;\">&nbsp;</td></tr>"
                + "</table>"
                + "</td></tr>"
                + "<tr><td style=\"padding:24px 16px 0 16px; text-align:center; font-family:" + DISPLAY + "; font-size:12px; line-height:20px; color:" + MUTED + ";\">"
                + "You received this email because you're registered on MoneyLog.<br>"
                + "If you have questions, reply to this email and we'll get back to you."
                + "</td></tr>"
                + "<tr><td style=\"padding:14px 16px 0 16px; text-align:center; font-family:'IBM Plex Mono',Consolas,monospace; font-size:11px; letter-spacing:2px; text-transform:uppercase; color:" + MUTED + ";\">"
                + "Every rupee has a story. &middot; &copy; 2026 MoneyLog"
                + "</td></tr>"
                + "</table>"
                + "</td></tr>"
                + "</table>"
                + "</body></html>";
    }

    private String brand() {
        return "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" bgcolor=\"" + FOREST + "\" style=\"background:" + FOREST + ";\">"
                + "<tr><td align=\"center\" style=\"padding:30px 24px 26px 24px;\">"
                + "<table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" align=\"center\">"
                + "<tr><td width=\"52\" height=\"52\" align=\"center\" valign=\"middle\" style=\"border-radius:13px; background:" + FOREST + "; font-family:" + DISPLAY + "; font-size:28px; font-weight:700; color:" + CREAM + "; line-height:52px;\" bgcolor=\"" + FOREST + "\">M</td></tr>"
                + "</table>"
                + "<div style=\"margin-top:12px; font-family:" + DISPLAY + "; font-size:21px; font-weight:700; color:" + CREAM + ";\">MoneyLog</div>"
                + "<div style=\"display:inline-block; width:38px; height:3px; border-radius:999px; background:" + SIENNA + "; margin-top:10px;\"><div style=\"height:1px;\"></div></div>"
                + "<div style=\"font-family:" + LEDGER + "; font-size:11px; letter-spacing:2px; text-transform:uppercase; color:" + MINT_ON_FOREST + "; margin-top:10px;\">Every rupee has a story.</div>"
                + "</td></tr>"
                + "</table>";
    }

    private String buildHeader(String eyebrow, String icon, String iconColor) {
        String badge = badge(icon);
        return "<tr><td class=\"pad\" style=\"padding:32px 40px 0 40px;\">"
                + (badge != null ? "<table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" align=\"center\"><tr>" + badgeCell(badge, iconColor) + "</tr></table>" : "")
                + "<div style=\"text-align:center; margin-top:18px;\">"
                + "<span style=\"display:inline-block; font-family:" + LEDGER + "; font-size:11px; font-weight:500; letter-spacing:2px; text-transform:uppercase; color:" + MUTED + "; background:" + PAPER + "; border:1px solid " + BORDER + "; border-radius:999px; padding:7px 15px;\">"
                + "<span style=\"display:inline-block; width:6px; height:6px; border-radius:50%; background:" + FOREST + "; margin-right:7px; vertical-align:middle;\">&nbsp;</span>"
                + eyebrow
                + "</span>"
                + "</div>"
                + "</td></tr>";
    }

    private String hero(String heading, String body, String extraHtml) {
        return "<div style=\"text-align:center;\">"
                + "<div style=\"font-family:" + DISPLAY + "; font-size:24px; line-height:32px; font-weight:700; color:" + INK + ";\">" + heading + "</div>"
                + "<div style=\"display:inline-block; width:44px; height:3px; border-radius:999px; background:" + SIENNA + "; margin:14px auto 0 auto;\"><div style=\"height:1px;\"></div></div>"
                + "<div style=\"font-size:15px; line-height:23px; color:" + MUTED + "; margin-top:16px;\">" + body + "</div>"
                + (extraHtml != null ? extraHtml : "")
                + "</div>";
    }

    private String primaryCta(String label, String link) {
        return "<table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" align=\"center\">"
                + "<tr><td align=\"center\" style=\"border-radius:999px; background:" + FOREST + ";\" bgcolor=\"" + FOREST + "\">"
                + "<a href=\"" + link + "\" style=\"display:inline-block; padding:14px 32px; font-family:" + DISPLAY + "; font-size:15px; font-weight:700; color:#ffffff; text-decoration:none; border-radius:999px; background:" + FOREST + ";\">" + label + "</a>"
                + "</td></tr>"
                + "</table>";
    }

    private String fallbackLink(String intro, String link) {
        return "<div style=\"text-align:center; font-family:" + DISPLAY + "; font-size:12px; color:" + MUTED + ";\">"
                + intro + "<br>"
                + "<a href=\"" + link + "\" style=\"color:" + FOREST_DARK + "; text-decoration:none; word-break:break-all; font-weight:600;\">" + link + "</a>"
                + "</div>";
    }

    private String noteBox(String messageHtml, String extraHtml, String icon) {
        return "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"margin-top:30px; background:" + PAPER_DEEP + "; border:1px solid " + BORDER + "; border-radius:12px;\" bgcolor=\"" + PAPER_DEEP + "\">"
                + "<tr><td style=\"padding:15px 18px; font-family:" + DISPLAY + "; font-size:13px; line-height:20px; color:" + MUTED + ";\">"
                + "<span style=\"display:inline-block; width:7px; height:7px; border-radius:50%; background:" + SIENNA + "; margin-right:8px;\">&nbsp;</span>"
                + messageHtml
                + (extraHtml != null ? "<div style=\"margin-top:8px; padding-top:8px; border-top:1px solid " + BORDER + ";\">" + extraHtml + "</div>" : "")
                + "</td></tr>"
                + "</table>";
    }

    private String featuresRow() {
        String[][] items = {
                {"&#8377;", "Track expenses", "Record spending the moment it happens.", MINT, FOREST},
                {"&#8643;", "Split bills", "Share costs with friends easily.", SAGE, FOREST},
                {"&#10004;", "Stay organized", "Payments, cards and categories in one place.", ROSE, SIENNA}
        };
        StringBuilder row = new StringBuilder();
        row.append("<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"margin-top:28px; border-top:1px solid " + BORDER + "; border-bottom:1px solid " + BORDER + ";\" >");
        row.append("<tr>");
        boolean first = true;
        for (String[] item : items) {
            row.append("<td class=\"features" + (first ? " features-first" : "") + "\" width=\"33%\" valign=\"top\" style=\"padding:20px 12px; font-family:" + DISPLAY + "; text-align:center;");
            if (!first) {
                row.append(" border-left:1px solid " + BORDER + ";");
            }
            row.append("\">");
            row.append("<div style=\"width:34px; height:34px; margin:0 auto 10px auto; border-radius:10px; background:" + item[3] + "; color:" + item[4] + "; font-size:18px; font-weight:700; text-align:center; line-height:34px;\">" + item[0] + "</div>");
            row.append("<div style=\"font-size:13px; font-weight:700; color:" + INK + ";\">" + item[1] + "</div>");
            row.append("<div style=\"font-size:11px; line-height:16px; color:" + MUTED + "; margin-top:4px;\">" + item[2] + "</div>");
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
        String bg = "#E2ECE3";
        String fg = FOREST;
        if (iconColor != null) {
            bg = iconColor;
        }
        return "<td width=\"52\" height=\"52\" align=\"center\" style=\"border-radius:13px; background:" + bg + "; font-family:" + DISPLAY + "; font-size:24px; line-height:52px; color:" + fg + ";\" bgcolor=\"" + bg + "\">" + glyph + "</td>";
    }
}