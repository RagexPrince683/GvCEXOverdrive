package handmadeguns.client.camera;

import handmadeguns.HandmadeGunsCore;
import handmadeguns.camera.CameraConfig;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraftforge.client.event.FOVUpdateEvent;

public class FOVController {
    private boolean initialized;
    private float currentFov;

    public void reset() {
        initialized = false;
        currentFov = 0.0F;
    }

    public void onFovUpdate(FOVUpdateEvent event, EntityClientPlayerMP player) {
        if (!CameraConfig.masterEnabled || !CameraConfig.fovEnabled || player == null) {
            initialized = false;
            return;
        }

        float target = event.newfov;
        if (player.isSprinting()) {
            target += CameraConfig.sprintFovBoost;
        }

        if (!initialized) {
            currentFov = target;
            initialized = true;
        }

        float speed = HandmadeGunsCore.Key_ADS(player) ? CameraConfig.adsFovSpeed : CameraConfig.fovLerpSpeed;
        currentFov = CameraMath.lerp(currentFov, target, CameraMath.clamp(speed, 0.01F, 1.0F));
        event.newfov = currentFov;
    }
}
