package dev.indica.INDICA.hud;

import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.Color;
import dev.indica.INDICA.modules.KillEffects;
import dev.indica.INDICA.INDICA;

public class KillEffectsHud extends HudElement {
    public static final HudElementInfo<KillEffectsHud> INFO = new HudElementInfo<>(
        INDICA.HUD_GROUP, "kill-effects", "Displays KillEffects module status.", KillEffectsHud::new
    );

    public KillEffectsHud() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        KillEffects killEffects = Modules.get().get(KillEffects.class);
        
        if (killEffects == null) {
            renderer.text("KillEffects: Module not found", x, y, Color.GRAY, true);
            setSize(renderer.textWidth("KillEffects: Module not found", true), renderer.textHeight(true));
            return;
        }

        if (!killEffects.isActive()) {
            renderer.text("KillEffects: Disabled", x, y, Color.RED, true);
            setSize(renderer.textWidth("KillEffects: Disabled", true), renderer.textHeight(true));
            return;
        }

        Color statusColor = Color.GREEN;
        String status = "Enabled";
        
        // Display module status
        renderer.text("KillEffects: " + status, x, y, statusColor, true);
        
        // Display current effect type
        String effectType = "Effect: " + killEffects.effectType.get().toString();
        renderer.text(effectType, x, y + renderer.textHeight(true), Color.WHITE, true);
        
        // Display effect-specific settings
        String effectSettings = "";
        if (killEffects.effectType.get() == KillEffects.EffectType.ENTITY) {
            effectSettings = killEffects.entityEffect.get().toString() + ": " + killEffects.entityAmount.get();
        } else if (killEffects.effectType.get() == KillEffects.EffectType.PARTICLE) {
            String particleInfo = killEffects.particleAmount.get() > 0 && !killEffects.particleTypes.get().isEmpty() ?
                killEffects.particleTypes.get().size() + " particles (" + killEffects.particleAmount.get() + ")" : "No particles";
            String soundInfo = !killEffects.soundEvents.get().isEmpty() ? 
                killEffects.soundEvents.get().size() + " sounds (" + killEffects.soundVolume.get() + "%)" : "No sound";
            effectSettings = particleInfo + " | " + soundInfo;
        }
        
        if (!effectSettings.isEmpty()) {
            renderer.text(effectSettings, x, y + renderer.textHeight(true) * 2, Color.GRAY, true);
        }
        
        // Display entity types
        String entityTypes = "Entities: ";
        if (killEffects.players.get()) entityTypes += "P";
        if (killEffects.hostileMobs.get()) entityTypes += "H";
        if (killEffects.passiveMobs.get()) entityTypes += "M";
        if (entityTypes.equals("Entities: ")) entityTypes += "None";
        
        renderer.text(entityTypes, x, y + renderer.textHeight(true) * 3, Color.GRAY, true);
        
        // Calculate total height
        int lines = 4; // Status, Effect, Settings, Entities
        setSize(
            Math.max(
                renderer.textWidth("KillEffects: " + status, true),
                Math.max(
                    renderer.textWidth(effectType, true),
                    Math.max(
                        renderer.textWidth(effectSettings, true),
                        renderer.textWidth(entityTypes, true)
                    )
                )
            ),
            renderer.textHeight(true) * lines
        );
    }
} 