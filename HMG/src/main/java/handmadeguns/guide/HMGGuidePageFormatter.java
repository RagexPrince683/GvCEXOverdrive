package handmadeguns.guide;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import java.util.ArrayList;
import java.util.List;

/**
 * Registration-time pagination helper that mirrors the safe Guide-API page
 * bounds used by the renderer.  It intentionally uses conservative character
 * widths because the actual FontRenderer is only available client-side.
 */
public final class HMGGuidePageFormatter {
    public static final int TEXT_ONLY_MAX_LINES = 13;
    public static final int FEATURED_ITEM_MAX_LINES = 7;
    public static final int APPROX_CHARS_PER_LINE = 20;

    private HMGGuidePageFormatter() {
    }

    public static List<FormattedPage> pagesFor(HMGGuideBookData.PageSpec pageSpec) {
        String text = pageSpec.generated ? HMGGuideDynamicText.textFor(pageSpec.key) : StatCollector.translateToLocal(pageSpec.key);
        return paginate(text, pageSpec.stack, pageSpec.stack != null ? FEATURED_ITEM_MAX_LINES : TEXT_ONLY_MAX_LINES);
    }

    private static List<FormattedPage> paginate(String text, ItemStack featuredStack, int firstPageMaxLines) {
        List<String> lines = wrapLines(normalizeText(text));
        List<FormattedPage> pages = new ArrayList<FormattedPage>();
        int index = 0;
        boolean firstPage = true;
        while (index < lines.size() || pages.isEmpty()) {
            int maxLines = firstPage ? firstPageMaxLines : TEXT_ONLY_MAX_LINES;
            StringBuilder pageText = new StringBuilder();
            int consumed = 0;
            while (index < lines.size() && consumed < maxLines) {
                if (pageText.length() > 0) {
                    pageText.append('\n');
                }
                pageText.append(lines.get(index));
                index++;
                consumed++;
            }
            pages.add(new FormattedPage(pageText.toString(), firstPage ? featuredStack : null));
            firstPage = false;
        }
        return pages;
    }

    public static String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\r\\n", "\n")
                .replace("\\n", "\n")
                .replace("\\r", "\n")
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .replace('&', '\u00a7');
    }

    private static List<String> wrapLines(String text) {
        List<String> result = new ArrayList<String>();
        String[] sourceLines = text.split("\n", -1);
        for (int i = 0; i < sourceLines.length; i++) {
            wrapLine(sourceLines[i], result);
        }
        return result;
    }

    private static void wrapLine(String line, List<String> result) {
        if (line.length() == 0) {
            result.add("");
            return;
        }
        String remaining = line;
        while (remaining.length() > APPROX_CHARS_PER_LINE) {
            int split = findSplit(remaining);
            result.add(remaining.substring(0, split).trim());
            remaining = remaining.substring(split).trim();
        }
        result.add(remaining);
    }

    private static int findSplit(String line) {
        int max = Math.min(APPROX_CHARS_PER_LINE, line.length());
        int split = line.lastIndexOf(' ', max);
        if (split <= 0) {
            split = max;
        }
        return split;
    }

    public static final class FormattedPage {
        public final String text;
        public final ItemStack featuredStack;

        private FormattedPage(String text, ItemStack featuredStack) {
            this.text = text;
            this.featuredStack = featuredStack;
        }
    }
}
