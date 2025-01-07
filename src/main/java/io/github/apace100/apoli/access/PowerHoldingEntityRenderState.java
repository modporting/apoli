package io.github.apace100.apoli.access;

import io.github.apace100.apoli.component.PowerHolderComponent;

import java.util.Optional;

public interface PowerHoldingEntityRenderState {
    void apoli$setPowerHolder(Optional<PowerHolderComponent> component);
    Optional<PowerHolderComponent> apoli$getPowerHolder();
}
