package handmadeguns.client.camera;

import cpw.mods.fml.common.Loader;
import handmadeguns.HandmadeGunsCore;
import net.minecraft.client.Minecraft;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class AngelicaCameraCompat {
    private static final String MOD_ID = "angelica";
    private static boolean checked;
    private static boolean loaded;
    private static boolean warned;
    private static Object bobviewSetting;
    private static Object dynamicFovSetting;

    private AngelicaCameraCompat() {}

    public static boolean isActive() {
        if (!HandmadeGunsCore.cfg_ClientCamera_AngelicaCompatEnabled) return false;
        if (!checked) discover();
        return loaded;
    }

    public static boolean shouldOwnBobbing() {
        return HandmadeGunsCore.cfg_ClientCamera_MasterEnabled && isActive()
                && HandmadeGunsCore.cfg_ClientCamera_PreferHmgBobbingWhenAngelicaLoaded;
    }

    public static void apply(Minecraft mc) {
        if (!isActive() || mc == null || mc.gameSettings == null) return;
        if (HandmadeGunsCore.cfg_ClientCamera_DisableAngelicaViewBobbing) {
            mc.gameSettings.viewBobbing = false;
            setOption(bobviewSetting, "DEFAULT");
        }
        if (HandmadeGunsCore.cfg_ClientCamera_DisableAngelicaDynamicFovWhenHmgFovEnabled
                && HandmadeGunsCore.cfg_ClientCamera_MasterEnabled) {
            setOption(dynamicFovSetting, "NONE");
        }
    }

    private static void discover() {
        checked = true;
        loaded = Loader.isModLoaded(MOD_ID);
        if (!loaded) return;
        try {
            Class<?> settings = Class.forName("jss.notfine.core.Settings");
            bobviewSetting = Enum.valueOf((Class<Enum>) settings.asSubclass(Enum.class), "BOBVIEW_MODE");
            dynamicFovSetting = Enum.valueOf((Class<Enum>) settings.asSubclass(Enum.class), "DYNAMIC_FOV");
            debug("Angelica camera compat found NotFine Settings hooks.");
        } catch (Throwable t) {
            softWarn("Angelica camera compat could not find NotFine Settings hooks: " + t.getClass().getSimpleName());
        }
    }

    private static void setOption(Object setting, String enumName) {
        if (setting == null) return;
        try {
            Field optionField = setting.getClass().getField("option");
            Object option = optionField.get(setting);
            Object current = invoke(option, "getStore");
            if (!(current instanceof Enum)) return;
            Enum<?> target = Enum.valueOf((Class<Enum>) current.getDeclaringClass(), enumName);
            if (current == target) return;
            Method setValue = option.getClass().getMethod("setValue", Object.class);
            setValue.invoke(option, target);
            Method applyChanges = option.getClass().getMethod("applyChanges");
            applyChanges.invoke(option);
            debug("Angelica camera compat forced " + setting + " to " + enumName + ".");
        } catch (Throwable t) {
            softWarn("Angelica camera compat could not force " + setting + ": " + t.getClass().getSimpleName());
        }
    }

    private static Object invoke(Object target, String method) throws Exception {
        Method m = target.getClass().getMethod(method);
        return m.invoke(target);
    }

    private static void softWarn(String message) {
        if (!warned) {
            warned = true;
            debug(message);
        }
    }

    private static void debug(String message) {
        if (HandmadeGunsCore.cfg_ClientCamera_DebugCamera) {
            System.out.println("[HMG Overdrive Camera] " + message);
        }
    }
}
