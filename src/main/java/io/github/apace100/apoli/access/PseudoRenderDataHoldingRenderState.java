package io.github.apace100.apoli.access;

public interface PseudoRenderDataHoldingRenderState extends PseudoRenderDataHolder{
    int apoli$getPseudoDeathTicks();
    int apoli$getPseudoFallFlyingTicks();
    void apoli$setPseudoDeathTicks(int ticks);
    void apoli$setPseudoFallFlyingTicks(int ticks);
}
