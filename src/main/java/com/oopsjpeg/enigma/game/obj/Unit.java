package com.oopsjpeg.enigma.game.obj;

import com.oopsjpeg.enigma.Command;
import com.oopsjpeg.enigma.game.GameObject;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.unit.*;
import com.oopsjpeg.enigma.util.Util;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;

public abstract class Unit extends GameObject {
    private static final Unit[] values = {
            new Berserker(), new Thief(), new Warrior(),
            new Duelist(), new Gunslinger(), new Assassin(),
            new Phasebreaker(), new Blademaster(), new Bloodreaper()
    };

    public static Unit[] values() {
        return values;
    }

    public static Unit fromName(String name) {
        if (name.equalsIgnoreCase("random"))
            return values()[Util.RANDOM.nextInt(values().length)];
        for (Unit u : values)
            if (name.equalsIgnoreCase(u.getName()) || (name.length() >= 3
                    && u.getName().toLowerCase().startsWith(name.toLowerCase()))) {
                try {
                    return u.getClass().getConstructor().newInstance();
                } catch (IllegalAccessException | InstantiationException
                        | NoSuchMethodException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        return null;
    }

    public abstract String getName();

    public abstract String getDescription();

    public Command[] getCommands() {
        return new Command[0];
    }

    public abstract Color getColor();

    public abstract Stats getStats();

    @Override
    public String toString() {
        return getName();
    }
}
