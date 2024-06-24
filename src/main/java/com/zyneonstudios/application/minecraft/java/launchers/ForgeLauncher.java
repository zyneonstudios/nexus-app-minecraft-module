package com.zyneonstudios.application.minecraft.java.launchers;

import com.zyneonstudios.application.MinecraftJavaAddon;
import com.zyneonstudios.application.minecraft.java.integrations.zyndex.LocalInstance;
import fr.flowarg.flowupdater.versions.ForgeVersionType;
import fr.flowarg.openlauncherlib.NoFramework;
import fr.theshark34.openlauncherlib.minecraft.GameFolder;

import java.nio.file.Path;

public class ForgeLauncher {

    private final MinecraftJavaAddon addon;

    public ForgeLauncher(MinecraftJavaAddon addon) {
        this.addon = addon;
    }

    public void launch(LocalInstance instance) {
        launch(instance.getMinecraftVersion(), instance.getForgeVersion(), ForgeVersionType.valueOf(instance.getForgeType().toUpperCase()), instance.getMemory(),instance.getPath(),instance.getId());
    }

    public void launch(String minecraftVersion, String forgeVersion, ForgeVersionType forgeType, int ram, Path instancePath, String id) {
        if (forgeType.equals(ForgeVersionType.NEO_FORGE)||!addon.getAuthenticator().isLoggedIn()) {
            return;
        }
        if (ram < 512) {
            ram = 512;
        }
        if (forgeType.equals(ForgeVersionType.NEW)) {
            forgeVersion = forgeVersion.replace(minecraftVersion + "-", "");
        } else {
            if (!forgeVersion.startsWith(minecraftVersion)) {
                forgeVersion = minecraftVersion + "-" + forgeVersion;
            }
        }
        NoFramework.ModLoader forge;
        if (forgeType == ForgeVersionType.OLD) {
            forge = NoFramework.ModLoader.OLD_FORGE;
        } else {
            forge = NoFramework.ModLoader.FORGE;
        }
        NoFramework framework = new NoFramework(
                instancePath,
                addon.getAuthenticator().getAuthInfos(),
                GameFolder.FLOW_UPDATER
        );
        if (minecraftVersion.equals("1.7.10")) {
            framework.setCustomModLoaderJsonFileName("1.7.10-Forge" + forgeVersion + ".json");
        }
        framework.getAdditionalVmArgs().add("-Xms"+ram+"M");
        framework.getAdditionalVmArgs().add("-Xmx" + ram + "M");

        try {
            Process game = framework.launch(minecraftVersion, forgeVersion, forge);
        } catch (Exception e) {}
    }
}