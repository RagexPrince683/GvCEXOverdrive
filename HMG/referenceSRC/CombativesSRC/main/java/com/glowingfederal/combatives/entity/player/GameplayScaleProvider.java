package com.glowingfederal.combatives.entity.player;

/**
 * Gameplay-only scale boundary.  MPM+ size and body-part scales are rendering
 * properties and therefore never implement or feed this contract.
 */
public interface GameplayScaleProvider {
    float getGameplayScaleX();
    float getGameplayScaleY();
}
