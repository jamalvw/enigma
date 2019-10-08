package com.oopsjpeg.enigma.game.effect;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.util.Util;

public class Divinity extends Effect {
    public static final String NAME = "Divinity";
    private final float power;

    public Divinity(float power) {
        this.power = power;
    }

    @Override
    public DamageEvent wasCrit(DamageEvent event) {
        event.critMul -= power;
        return event;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDesc() {
        return "Reduces damage taken from crits by **" + Util.percent(power) + "**.";
    }

    @Override
    public float getPower() {
        return power;
    }
}