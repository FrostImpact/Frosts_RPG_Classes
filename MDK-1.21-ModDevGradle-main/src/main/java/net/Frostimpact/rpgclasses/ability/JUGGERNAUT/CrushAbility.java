package net.Frostimpact.rpgclasses.ability.JUGGERNAUT;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public class CrushAbility extends Ability {

    private static final int CHARGE_COST = 10;

    public CrushAbility() {
        super("crush");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        // 1. Check Costs
        int charge = rpgData.getJuggernautCharge();
        boolean isShieldMode = rpgData.isJuggernautShieldMode();

        // Check if in SHATTER mode with enough charge
        if (!isShieldMode && charge >= CHARGE_COST) {
            rpgData.removeJuggernautCharge(CHARGE_COST);
            rpgData.setCrushPowered(true);
        } else {
            rpgData.setCrushPowered(false);
        }

        // 2. Set State
        rpgData.setCrushActive(true);

        // 3. LEVITATE / LAUNCH (Reduced height)
        Vec3 currentVel = player.getDeltaMovement();
        // Lowered Y velocity from 1.2 to 0.8 for a more controlled hop
        player.setDeltaMovement(currentVel.x, 0.8, currentVel.z);
        player.hurtMarked = true;

        // 4. Sound (Launch)
        player.level().playSound(
                null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TRIDENT_RIPTIDE_3,
                SoundSource.PLAYERS,
                1.0f, 0.5f
        );

        // Consume resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }
}