package handmadeguns.items;

import net.minecraft.item.Item;

public class HMGItemAttachmentBase extends Item {
    public float slowdownrate;

    /** Pack model name supplied by attach3dmodel, or null for legacy attachments. */
    public String attach3dmodel;
    /** Pack texture name supplied by 3dmodeltex, resolved at attachment finalization. */
    public String model3dTexture;
    /** Slot-specific pack model and texture names. Index zero is intentionally unused. */
    public final String[] attach3dmodels = new String[6];
    public final String[] model3dTextures = new String[6];
    /** Inventory-only multiplier applied to the attachment renderer's base scale. */
    public float inventoryScale = 1.0F;
    /** Inventory-only model offsets; installed attachment placement does not use these. */
    public float inventoryOffsetX;
    public float inventoryOffsetY;
    public float inventoryOffsetZ;

    public boolean has3dModel() {
        return attach3dmodel != null && !attach3dmodel.isEmpty();
    }

    public boolean has3dModel(int slot) {
        return get3dModel(slot) != null;
    }

    public String get3dModel(int slot) {
        if (slot >= 1 && slot <= 5 && attach3dmodels[slot] != null && !attach3dmodels[slot].isEmpty())
            return attach3dmodels[slot];
        return has3dModel() ? attach3dmodel : null;
    }

    public String get3dModelTexture(int slot) {
        if (slot >= 1 && slot <= 5 && model3dTextures[slot] != null && !model3dTextures[slot].isEmpty())
            return model3dTextures[slot];
        return model3dTexture;
    }

    public int getStandalone3dModelSlot() {
        if (has3dModel()) return 0;
        for (int slot = 1; slot <= 5; slot++) if (attach3dmodels[slot] != null && !attach3dmodels[slot].isEmpty()) return slot;
        return -1;
    }
}
