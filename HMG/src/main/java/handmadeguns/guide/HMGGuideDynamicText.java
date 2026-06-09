package handmadeguns.guide;

import handmadeguns.HandmadeGunsCore;
import handmadeguns.items.GunInfo;
import net.minecraft.util.StatCollector;

public final class HMGGuideDynamicText {
    private HMGGuideDynamicText() {
    }

    public static String textFor(String localizationKey) {
        String base = StatCollector.translateToLocal(localizationKey);
        GunInfo defaults = new GunInfo();
        if ("hmg.guide.page.weapon_basics.generated".equals(localizationKey)) {
            return base + "\n\n" + StatCollector.translateToLocalFormatted("hmg.guide.generated.weapon_stats",
                    HandmadeGunsCore.MAXGUNSINV,
                    HandmadeGunsCore.cfg_muzzleflash,
                    HandmadeGunsCore.cfg_blockdestroy,
                    HandmadeGunsCore.cfg_defaultknockback,
                    HandmadeGunsCore.cfg_defaultknockbacky,
                    HandmadeGunsCore.cfg_ThreadHitCheck,
                    HandmadeGunsCore.cfg_ThreadHitCheck_split_length,
                    defaults.recoil,
                    defaults.recoil_sneak,
                    defaults.ads_spread_cof,
                    defaults.attackDamage);
        }
        if ("hmg.guide.page.handling.generated".equals(localizationKey)) {
            return base + "\n\n" + StatCollector.translateToLocalFormatted("hmg.guide.generated.handling",
                    HandmadeGunsCore.cfg_ADS_Toggle,
                    HandmadeGunsCore.cfg_ADS_Sneaking,
                    HandmadeGunsCore.cfg_Sneak_ByADSKey,
                    HandmadeGunsCore.manualGunPickupRange,
                    HandmadeGunsCore.manualGunPickupRequiresLineOfSight);
        }
        if ("hmg.guide.page.explosives.generated".equals(localizationKey)) {
            return base + "\n\n" + StatCollector.translateToLocalFormatted("hmg.guide.generated.explosives",
                    HandmadeGunsCore.cfg_exprotion,
                    HandmadeGunsCore.cfg_blockdestroy,
                    HandmadeGunsCore.cfg_defgravitycof,
                    defaults.under_gl_power,
                    defaults.under_gl_bure,
                    defaults.under_gl_recoil,
                    defaults.damagerange,
                    defaults.fuse);
        }
        if ("hmg.guide.page.server_config.generated".equals(localizationKey)) {
            return base + "\n\n" + StatCollector.translateToLocalFormatted("hmg.guide.generated.server_config",
                    HandmadeGunsCore.enableHMGGuideBook,
                    HandmadeGunsCore.enableManualGunPickup,
                    HandmadeGunsCore.manualGunPickupOnlyGuns,
                    HandmadeGunsCore.enableGunGroundPhysicsRender,
                    HandmadeGunsCore.enableVBOModelRendering);
        }
        return base;
    }
}
