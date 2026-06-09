package amerifrance.guideapi.api.util;

import amerifrance.guideapi.gui.GuiBase;
import net.minecraft.client.gui.FontRenderer;

import java.util.ArrayList;
import java.util.List;

/** Shared Guide-API page bounds and wrapping helpers. */
public final class PageLayoutHelper {
    public static final int PAGE_LEFT_MARGIN = 39;
    public static final int PAGE_TOP_MARGIN = 12;
    public static final int PAGE_BODY_TOP_MARGIN = 14;
    public static final int PAGE_BOTTOM_RESERVED = 42;
    public static final int ITEM_ICON_X = 75;
    public static final int ITEM_ICON_Y = 20;
    public static final int ITEM_ICON_SCALE = 3;
    public static final int ITEM_TEXT_TOP_MARGIN = 72;
    public static final int LINE_HEIGHT = 10;

    private PageLayoutHelper() {
    }

    public static int contentLeft(int guiLeft) {
        return guiLeft + PAGE_LEFT_MARGIN;
    }

    public static int contentTop(int guiTop) {
        return guiTop + PAGE_BODY_TOP_MARGIN;
    }

    public static int contentBottom(GuiBase guiBase, int guiTop) {
        return guiTop + guiBase.ySize - PAGE_BOTTOM_RESERVED;
    }

    public static int contentWidth(GuiBase guiBase) {
        return (3 * guiBase.xSize / 5);
    }

    public static int featuredItemTextTop(int guiTop) {
        return guiTop + ITEM_TEXT_TOP_MARGIN;
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

    @SuppressWarnings("unchecked")
    public static List<String> wrapText(FontRenderer fontRenderer, String text, int width) {
        List<String> wrapped = new ArrayList<String>();
        String normalized = normalizeText(text);
        String[] paragraphs = normalized.split("\n", -1);
        for (int i = 0; i < paragraphs.length; i++) {
            String paragraph = paragraphs[i];
            if (paragraph.length() == 0) {
                wrapped.add("");
            } else {
                wrapped.addAll(fontRenderer.listFormattedStringToWidth(paragraph, width));
            }
        }
        return wrapped;
    }

    public static int maxLines(GuiBase guiBase, int startY, int guiTop) {
        int available = contentBottom(guiBase, guiTop) - startY;
        return Math.max(0, available / LINE_HEIGHT);
    }

    public static void drawWrappedText(FontRenderer fontRenderer, String text, int x, int y, int width, int bottomY, int color) {
        List<String> lines = wrapText(fontRenderer, text, width);
        int drawY = y;
        for (String line : lines) {
            if (drawY + LINE_HEIGHT > bottomY) {
                break;
            }
            fontRenderer.drawString(line, x, drawY, color);
            drawY += LINE_HEIGHT;
        }
    }

    public static void drawPageText(FontRenderer fontRenderer, String text, int guiLeft, int guiTop, GuiBase guiBase, int yOffset, int color) {
        int startY = guiTop + PAGE_TOP_MARGIN + Math.max(0, yOffset);
        drawWrappedText(fontRenderer, text, contentLeft(guiLeft), startY, contentWidth(guiBase), contentBottom(guiBase, guiTop), color);
    }

    public static void drawFeaturedItemText(FontRenderer fontRenderer, String text, int guiLeft, int guiTop, GuiBase guiBase, int color) {
        drawWrappedText(fontRenderer, text, contentLeft(guiLeft), featuredItemTextTop(guiTop), contentWidth(guiBase), contentBottom(guiBase, guiTop), color);
    }
}
