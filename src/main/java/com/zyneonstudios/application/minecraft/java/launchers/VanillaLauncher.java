package com.zyneonstudios.application.minecraft.java.launchers;

import com.zyneonstudios.application.MinecraftJavaAddon;
import com.zyneonstudios.application.minecraft.java.integrations.zyndex.LocalInstance;
import fr.flowarg.openlauncherlib.NoFramework;
import fr.theshark34.openlauncherlib.minecraft.GameFolder;

import java.nio.file.Path;

public class VanillaLauncher {

    private final MinecraftJavaAddon addon;

    public VanillaLauncher(MinecraftJavaAddon addon) {
        this.addon = addon;
    }

    public void launch(LocalInstance instance) {
        // Zyndex TODO: Instance updater;
        launch(instance.getMinecraftVersion(), instance.getMemory(), instance.getPath(), instance.getId());
    }

    public void launch(String version, int ram, Path instancePath, String id) {
        if(!addon.getAuthenticator().isLoggedIn()) {
            return;
        }
        if (ram < 512) {
            ram = 512;
        }
        NoFramework framework = new NoFramework(
                instancePath,
                addon.getAuthenticator().getAuthInfos(),
                GameFolder.FLOW_UPDATER
        );
        framework.getAdditionalVmArgs().add("-Xms"+ram+"M");
        framework.getAdditionalVmArgs().add("-Xmx"+ram+"M");
        // macOS TODO: framework.getAdditionalVmArgs().add("-XstartOnFirstThread");
        try {
            Process game = framework.launch(version, "", NoFramework.ModLoader.VANILLA);
            game.onExit().thenRun(() -> {

            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}