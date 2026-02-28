package de.scholle.mclc.common;

import java.util.Map;

public final class ServerListTextService {

    public String normalizeMotd(String motd) {
        if (motd == null) {
            return "";
        }
        return motd.replace("\\n", "\n");
    }

    public String applyPlaceholders(String text, Map<String, String> placeholders) {
        if (text == null || text.isEmpty() || placeholders == null || placeholders.isEmpty()) {
            return text;
        }

        String result = text;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String replacement = entry.getValue() == null ? "" : entry.getValue();
            result = result.replace(entry.getKey(), replacement);
        }
        return result;
    }
}
