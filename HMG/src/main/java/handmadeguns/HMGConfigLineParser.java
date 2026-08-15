package handmadeguns;

/** Parsing compatibility for the three post-legacy attachment keys only. */
public final class HMGConfigLineParser {
    private HMGConfigLineParser() {}

    public static String[] parseAttachmentExtensionLine(String line) {
        String[] comma = HMGGunMaker.splitComma(line);
        String candidate = comma.length == 0 ? "" : comma[0].trim();
        if (isExtensionKey(candidate)) return trim(comma);

        int equals = line.indexOf('=');
        if (equals < 0) return comma;
        String key = line.substring(0, equals).trim();
        if (!isExtensionKey(key)) return comma;
        String[] values = HMGGunMaker.splitComma(line.substring(equals + 1));
        String[] result = new String[values.length + 1];
        result[0] = key;
        for (int i = 0; i < values.length; i++) result[i + 1] = values[i].trim();
        return result;
    }

    private static boolean isExtensionKey(String key) {
        return "attach3dmodel".equals(key) || "3dmodeltex".equals(key)
                || "attachmentlocation".equals(key);
    }

    private static String[] trim(String[] values) {
        for (int i = 0; i < values.length; i++) values[i] = values[i].trim();
        return values;
    }
}
