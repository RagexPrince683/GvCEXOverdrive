package com.glowingfederal.combatives.movement;

public interface ICombativesMovementState {
    MovementSnapshot getCombativesMovementSnapshot();
    void setCombativesMovementSnapshot(MovementSnapshot snapshot);
}
