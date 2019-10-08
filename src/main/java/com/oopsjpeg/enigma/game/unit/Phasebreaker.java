package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Unit;
import com.oopsjpeg.enigma.util.Command;
import com.oopsjpeg.enigma.util.Stacker;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;

import java.awt.*;

public class Phasebreaker extends Unit {
    public static final int FLARE_STACKS = 3;
    public static final float PASSIVE_AP = 0.3f;
    public static final int PHASE_1_AP = 7;
    public static final float PHASE_2_SHIELD = 0.6f;
    public static final int PHASE_2_AP = 10;

    private int phase = 0;
    private Stacker flare = new Stacker(FLARE_STACKS);
    private boolean flared = false;
    private int bonusAp = 0;

    public int getPhase() {
        return phase;
    }

    public void setPhase(int phase) {
        this.phase = phase;
    }

    public Stacker getFlare() {
        return flare;
    }

    public void setFlare(Stacker flare) {
        this.flare = flare;
    }

    public boolean getFlared() {
        return flared;
    }

    public void setFlared(boolean flared) {
        this.flared = flared;
    }

    public int getBonusAp() {
        return bonusAp;
    }

    public void setBonusAp(int bonusAp) {
        this.bonusAp = bonusAp;
    }

    @Override
    public String onTurnStart(Game.Member member) {
        flared = false;
        phase++;
        if (phase > 3) phase = 1;
        return "";
    }

    @Override
    public DamageEvent onBasicAttack(DamageEvent event) {
        flare.stack();
        if (flared && phase == 1) {
            bonusAp += PHASE_1_AP;
            flare.stack();
            event.actor.getStats().add(Stats.ABILITY_POWER, PHASE_1_AP);
        }
        return event;
    }

    @Override
    public DamageEvent onDamage(DamageEvent event) {
        event.damage += event.actor.getStats().get(Stats.ABILITY_POWER) * PASSIVE_AP;
        if (flared) {
            switch (phase) {
                case 2:
                    // Shield
                    float ap = event.actor.getStats().get(Stats.ABILITY_POWER) / (PHASE_2_AP * 100);
                    event.output.add(event.actor.shield(event.total() * (PHASE_2_SHIELD + ap)));
                    break;
                case 3:
                    // Ignore resist
                    float ignore = 1 + event.target.getStats().get(Stats.RESIST) + (event.target.isDefensive() ? 0.2f : 0);
                    event.damage *= ignore;
                    event.bonus *= ignore;
                    // Double passive
                    event.bonus += event.actor.getStats().get(Stats.ABILITY_POWER) * PASSIVE_AP;
                    break;
            }
        }
        return event;
    }

    @Override
    public String getName() {
        return "Phasebreaker";
    }

    @Override
    public String getDescription() {
        return "Basic attacks deal **" + Util.percent(PASSIVE_AP) + " AP** bonus damage."
                + "\n\nEvery turn, **Phase** goes up by **1**, resetting after **3**."
                + "\nBasic attacks build **Flare**. At **" + FLARE_STACKS + "** stacks, using `>flare` grants special effects for a single turn based on **Phase**:"
                + "\n**1**. Basic attacks permanently increase ability power by **" + PHASE_1_AP + "** and grant double **Flare**."
                + "\n**2**. Attacks shield for **" + Util.percent(PHASE_2_SHIELD) + "** (+1% per " + PHASE_2_AP + " AP) of damage."
                + "\n**3**. Passive AP damage is doubled and attacks ignore resist.";
    }

    @Override
    public Command[] getCommands() {
        return new Command[]{new FlareCommand()};
    }

    @Override
    public Color getColor() {
        return new Color(0, 255, 191);
    }

    @Override
    public Stats getStats() {
        return new Stats()
                .put(Stats.ENERGY, 125)
                .put(Stats.MAX_HEALTH, 750)
                .put(Stats.DAMAGE, 20)
                .put(Stats.ABILITY_POWER, bonusAp);
    }

    @Override
    public Stats getPerTurn() {
        return new Stats()
                .put(Stats.HEALTH, 12);
    }

    public class FlareCommand implements Command {
        @Override
        public void execute(Message message, String alias, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();
            Game.Member member = game.getMember(author);

            if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                message.delete().block();
                if (game.getGameState() == 0)
                    Util.sendFailure(channel, "You cannot use **Flare** until the game has started.");
                else
                    member.act(game.new FlareAction());
            }
        }

        @Override
        public String getName() {
            return "flare";
        }
    }
}