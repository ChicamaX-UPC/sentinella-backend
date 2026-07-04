package com.chicamax.sentinella.shared.infrastructure.mail;

import java.util.Arrays;
import java.util.stream.Collectors;

/** Layout HTML transaccional alineado al tema oscuro / acento naranja del frontend Sentinella. */
public final class SentinellaEmailTemplate {

    private static final String BG = "#120d0a";
    private static final String CARD = "#16110e";
    private static final String BORDER = "#2a211c";
    private static final String ACCENT = "#ff8c42";
    private static final String TEXT = "#f8fafc";
    private static final String MUTED = "#94a3b8";

    private SentinellaEmailTemplate() {
    }

    public static String render(String headline, String bodyHtml, String ctaLabel, String ctaHref) {
        String ctaBlock = "";
        if (ctaLabel != null && !ctaLabel.isBlank() && ctaHref != null && !ctaHref.isBlank()) {
            ctaBlock = """
                    <tr>
                      <td style="padding:28px 32px 8px;">
                        <a href="%s" style="display:inline-block;background:%s;color:#1a0f08;font-weight:600;text-decoration:none;padding:12px 22px;border-radius:12px;font-size:14px;">%s</a>
                      </td>
                    </tr>
                    """.formatted(escapeHtml(ctaHref), ACCENT, escapeHtml(ctaLabel));
        }

        return """
                <!DOCTYPE html>
                <html lang="es">
                <head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
                <body style="margin:0;padding:0;background:%s;font-family:Inter,Segoe UI,Roboto,Helvetica,Arial,sans-serif;">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background:%s;padding:32px 16px;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width:560px;background:%s;border:1px solid %s;border-radius:16px;overflow:hidden;">
                          <tr>
                            <td style="padding:20px 32px;border-bottom:1px solid %s;">
                              <span style="font-size:11px;font-weight:700;letter-spacing:0.18em;text-transform:uppercase;color:%s;">Sentinella</span>
                              <span style="display:block;margin-top:6px;font-size:13px;color:%s;">Monitoreo de relaves</span>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:28px 32px 8px;">
                              <h1 style="margin:0;font-size:20px;line-height:1.35;color:%s;font-weight:600;">%s</h1>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:8px 32px 24px;color:%s;font-size:15px;line-height:1.65;">%s</td>
                          </tr>
                          %s
                          <tr>
                            <td style="padding:8px 32px 28px;border-top:1px solid %s;">
                              <p style="margin:0;font-size:12px;line-height:1.5;color:%s;">ChicamaX · Plataforma Sentinella</p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(
                BG, BG, CARD, BORDER, BORDER, ACCENT, MUTED,
                TEXT, escapeHtml(headline),
                TEXT, bodyHtml,
                ctaBlock,
                BORDER, MUTED
        );
    }

    public static String paragraphs(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            return "";
        }
        return Arrays.stream(plainText.split("\\R\\R+"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .map(line -> "<p style=\"margin:0 0 14px;\">" + escapeHtml(line).replace("\n", "<br/>") + "</p>")
                .collect(Collectors.joining());
    }

    public static String detailRow(String label, String value) {
        return """
                <p style="margin:0 0 10px;"><span style="color:%s;">%s:</span> <strong style="color:%s;">%s</strong></p>
                """.formatted(MUTED, escapeHtml(label), TEXT, escapeHtml(value));
    }

    public static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
