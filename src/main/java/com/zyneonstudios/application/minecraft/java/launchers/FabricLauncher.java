package com.zyneonstudios.application.minecraft.java.launchers;

import com.zyneonstudios.application.MinecraftJavaAddon;
import com.zyneonstudios.application.minecraft.java.integrations.zyndex.LocalInstance;
import fr.flowarg.openlauncherlib.NoFramework;
import fr.theshark34.openlauncherlib.minecraft.GameFolder;

import java.nio.file.Path;

public class FabricLauncher {

    private final MinecraftJavaAddon addon;
    private final byte[] authKey;

    public FabricLauncher(MinecraftJavaAddon addon, byte[] authKey) {
        this.addon = addon;
        this.authKey = authKey;
    }

    public void launch(LocalInstance instance) {
        launch(instance.getMinecraftVersion(), instance.getFabricVersion(), instance.getMemory(), instance.getPath(), instance.getId());
    }

    public void launch(String minecraftVersion, String fabricVersion, int ram, Path instancePath, String id) {
        if(addon.getAuthState()!=MinecraftJavaAddon.AuthState.LOGGED_IN) {
            return;
        }
        if (ram < 512) {
            ram = 512;
        }
        NoFramework framework = new NoFramework(
                instancePath,
                addon.getAuthenticator(authKey).getAuthInfos(),
                GameFolder.FLOW_UPDATER
        );
        framework.getAdditionalVmArgs().add("-Xms"+ram+"M");
        framework.getAdditionalVmArgs().add("-Xmx" + ram + "M");
        try {
            Process game = framework.launch(minecraftVersion, fabricVersion, NoFramework.ModLoader.FABRIC);
            game.onExit().thenRun(() -> {});
        } catch (Exception e) {}
    }
}