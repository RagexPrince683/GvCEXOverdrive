package handmadeguns.items;

import net.minecraft.item.Item;

public class HMGItemAttachmentBase extends Item {
    public float slowdownrate;

    /** Pack model name supplied by attach3dmodel, or null for legacy attachments. */
    public String attach3dmodel;

    public boolean has3dModel() {
        return attach3dmodel != null && !attach3dmodel.isEmpty();
    }
}
