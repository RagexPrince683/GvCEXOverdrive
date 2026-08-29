package com.glowingfederal.combatives.client;

import net.minecraft.util.MovementInput;

public class MovementInputStorage extends MovementInput {
    public int sprintToggleTimer;
    public boolean isFlying;
    public boolean isSprinting;

    public void copyFrom(MovementInput movement) {
        this.moveStrafe = movement.moveStrafe;
        this.moveForward = movement.moveForward;
        this.jump = movement.jump;
        this.sneak = movement.sneak;
    }
}
