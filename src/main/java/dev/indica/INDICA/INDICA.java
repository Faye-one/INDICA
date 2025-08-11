package dev.indica.INDICA;

import dev.indica.INDICA.modules.OminousVaultESP;
import dev.indica.INDICA.commands.CommandExample;
import dev.indica.INDICA.hud.HudExample;
import dev.indica.INDICA.hud.KillEffectsHud;
import dev.indica.INDICA.modules.ShulkerFrameESP;
import dev.indica.INDICA.modules.MapDuplicator;
import dev.indica.INDICA.modules.KillEffects;
import dev.indica.INDICA.modules.RespawnPointBlocker;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;

public class INDICA extends MeteorAddon {
    public static final Category CATEGORY = new Category("Example");
    public static final HudGroup HUD_GROUP = new HudGroup("Example");
    public static final Category INDICA_CATEGORY = new Category("INDICA");

    @Override
    public void onInitialize() {
        Modules.get().add(new OminousVaultESP());
        Modules.get().add(new ShulkerFrameESP());
        Modules.get().add(new KillEffects());
        Modules.get().add(new RespawnPointBlocker());
        Modules.get().add(new MapDuplicator());

        // Commands
        Commands.add(new CommandExample());

        // HUD
        Hud.get().register(HudExample.INFO);
        Hud.get().register(KillEffectsHud.INFO);
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
        Modules.registerCategory(INDICA_CATEGORY);
    }

    @Override
    public String getPackage() {
        return "dev.indica.INDICA";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("Faye_one", "INDICA");
    }
}
