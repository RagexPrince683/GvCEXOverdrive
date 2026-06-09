package handmadeguns.guide;

import java.awt.Color;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Reflection-only adapter for Guide-API.  This class must only be called after
 * Loader.isModLoaded("guideapi") has returned true.
 */
final class HMGGuideReflectionRegistrar {
    private HMGGuideReflectionRegistrar() {
    }

    static int register() throws Exception {
        Class<?> iPageClass = Class.forName("amerifrance.guideapi.api.abstraction.IPage");
        Class<?> entryAbstractClass = Class.forName("amerifrance.guideapi.api.abstraction.EntryAbstract");
        Class<?> categoryAbstractClass = Class.forName("amerifrance.guideapi.api.abstraction.CategoryAbstract");
        Class<?> entryBaseClass = Class.forName("amerifrance.guideapi.api.base.EntryBase");
        Class<?> categoryItemStackClass = Class.forName("amerifrance.guideapi.categories.CategoryItemStack");
        Class<?> pageTextClass = Class.forName("amerifrance.guideapi.pages.PageText");
        Class<?> pageUnlocTextClass = Class.forName("amerifrance.guideapi.pages.PageUnlocText");
        Class<?> pageUnlocItemStackClass = Class.forName("amerifrance.guideapi.pages.PageUnlocItemStack");
        Class<?> bookBuilderClass = Class.forName("amerifrance.guideapi.api.util.BookBuilder");
        Class<?> bookClass = Class.forName("amerifrance.guideapi.api.base.Book");
        Class<?> guideRegistryClass = Class.forName("amerifrance.guideapi.api.GuideRegistry");

        Constructor<?> entryCtor = entryBaseClass.getConstructor(List.class, String.class);
        Constructor<?> categoryCtor = categoryItemStackClass.getConstructor(List.class, String.class, net.minecraft.item.ItemStack.class);
        Constructor<?> pageTextCtor = pageTextClass.getConstructor(String.class);
        Constructor<?> pageUnlocTextCtor = pageUnlocTextClass.getConstructor(String.class);
        Constructor<?> pageUnlocItemCtor = pageUnlocItemStackClass.getConstructor(String.class, net.minecraft.item.ItemStack.class);
        Method setCategories = bookBuilderClass.getMethod("setCategories", List.class);
        Method setUnlocBookTitle = bookBuilderClass.getMethod("setUnlocBookTitle", String.class);
        Method setUnlocWelcomeMessage = bookBuilderClass.getMethod("setUnlocWelcomeMessage", String.class);
        Method setUnlocDisplayName = bookBuilderClass.getMethod("setUnlocDisplayName", String.class);
        Method setAuthor = bookBuilderClass.getMethod("setAuthor", String.class);
        Method setBookColor = bookBuilderClass.getMethod("setBookColor", Color.class);
        Method build = bookBuilderClass.getMethod("build");
        Method registerBook = guideRegistryClass.getMethod("registerBook", bookClass);

        List<HMGGuideBookData.CategorySpec> categorySpecs = HMGGuideBookData.createCategories();
        List<Object> categories = new ArrayList<Object>();
        for (HMGGuideBookData.CategorySpec categorySpec : categorySpecs) {
            List<Object> entries = new ArrayList<Object>();
            for (HMGGuideBookData.EntrySpec entrySpec : categorySpec.entries) {
                List<Object> pages = new ArrayList<Object>();
                for (HMGGuideBookData.PageSpec pageSpec : entrySpec.pages) {
                    Object page;
                    if (pageSpec.generated) {
                        page = pageTextCtor.newInstance(HMGGuideDynamicText.textFor(pageSpec.key));
                    } else if (pageSpec.stack != null) {
                        page = pageUnlocItemCtor.newInstance(pageSpec.key, pageSpec.stack.copy());
                    } else {
                        page = pageUnlocTextCtor.newInstance(pageSpec.key);
                    }
                    iPageClass.cast(page);
                    pages.add(page);
                }
                Object entry = entryCtor.newInstance(pages, entrySpec.key(categorySpec));
                entryAbstractClass.cast(entry);
                entries.add(entry);
            }
            Object category = categoryCtor.newInstance(entries, categorySpec.key(), categorySpec.icon.copy());
            categoryAbstractClass.cast(category);
            categories.add(category);
        }

        Object builder = bookBuilderClass.newInstance();
        setCategories.invoke(builder, categories);
        setUnlocBookTitle.invoke(builder, HMGGuideBookData.BOOK_TITLE_KEY);
        setUnlocWelcomeMessage.invoke(builder, HMGGuideBookData.BOOK_WELCOME_KEY);
        setUnlocDisplayName.invoke(builder, HMGGuideBookData.BOOK_DISPLAY_KEY);
        setAuthor.invoke(builder, "HMG-Overdrive");
        setBookColor.invoke(builder, new Color(46, 74, 92));
        Object book = build.invoke(builder);
        registerBook.invoke(null, book);
        return categories.size();
    }

}
