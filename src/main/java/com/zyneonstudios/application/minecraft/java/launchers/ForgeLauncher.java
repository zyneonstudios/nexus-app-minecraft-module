package com.zyneonstudios.application.minecraft.java.launchers;

import com.zyneonstudios.application.MinecraftJavaAddon;
import com.zyneonstudios.application.minecraft.java.integrations.zyndex.LocalInstance;
import fr.flowarg.openlauncherlib.NoFramework;
import fr.theshark34.openlauncherlib.minecraft.GameFolder;

import java.nio.file.Path;

public class ForgeLauncher {

    private final MinecraftJavaAddon addon;
    private final byte[] authKey;

    public ForgeLauncher(MinecraftJavaAddon addon, byte[] authKey) {
        this.addon = addon;
        this.authKey = authKey;
    }

    public void launch(LocalInstance instance) {
        launch(instance.getMinecraftVersion(), instance.getForgeVersion(), instance.getMemory(),instance.getPath(),instance.getId());
    }

    public void launch(String minecraftVersion, String forgeVersion, int ram, Path instancePath, String id) {
        if (addon.getAuthState()!= MinecraftJavaAddon.AuthState.LOGGED_IN) {
            return;
        }
        if (ram < 512) {
            ram = 512;
        }
        if (!forgeVersion.startsWith(minecraftVersion)) {
            forgeVersion = minecraftVersion + "-" + forgeVersion;
        }
        NoFramework framework = new NoFramework(
                instancePath,
                addon.getAuthenticator(authKey).getAuthInfos(),
                GameFolder.FLOW_UPDATER
        );
        if (minecraftVersion.equals("1.7.10")) {
            framework.setCustomModLoaderJsonFileName("1.7.10-Forge" + forgeVersion + ".json");
        }
        framework.getAdditionalVmArgs().add("-Xms"+ram+"M");
        framework.getAdditionalVmArgs().add("-Xmx" + ram + "M");

        try {
            Process game = framework.launch(minecraftVersion, forgeVersion, NoFramework.ModLoader.FORGE);
        } catch (Exception e) {}
    }
}