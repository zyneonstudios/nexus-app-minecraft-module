package com.zyneonstudios.application.minecraft.java.authentication;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.zyneonstudios.application.MinecraftJavaAddon;
import com.zyneonstudios.application.frame.web.ApplicationFrame;
import com.zyneonstudios.application.main.ApplicationStorage;
import com.zyneonstudios.application.main.NexusApplication;
import com.zyneonstudios.application.minecraft.java.JavaStorage;
import live.nerotv.shademebaby.utils.GsonUtil;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AuthenticationResolver {

    private final MinecraftJavaAddon moduleInfo;

    public AuthenticationResolver(MinecraftJavaAddon moduleInfo) {
        this.moduleInfo = moduleInfo;
    }

    public void preAuth() {
        moduleInfo.setAuthState(MinecraftJavaAddon.AuthState.LOGGING_IN);
        ((ApplicationFrame)moduleInfo.getApplication().getFrame()).openCustomPage("Minecraft: Java Edition Login","mje-authentication", JavaStorage.getUrlBase()+"mje-login.html?id=1&enable=false");
    }

    public void postAuth(String username, String uuid) {
        JavaStorage.map.set("auth.username",username);
        JavaStorage.map.set("auth.uuid",UUID.fromString(uuid));
        moduleInfo.setAuthState(MinecraftJavaAddon.AuthState.LOGGED_IN);
        CompletableFuture.runAsync(()->{
            try {
                JsonArray team = GsonUtil.getObject("https://danieldieeins.github.io/ZyneonApplicationContent/i/team.json").getAsJsonArray("team");
                for(JsonElement element:team) {
                    if(element.getAsString().equals(uuid.replace("-",""))) {
                        NexusApplication.getLogger().debug("[Minecraft] (AuthResolver) This account does have access to Zyneon Drive.");
                        ApplicationStorage.enableDriveAccess();
                        break;
                    } else {
                        NexusApplication.getLogger().debug("[Minecraft] (AuthResolver) This account doesn't have access to Zyneon Drive.");
                    }
                }
            } catch (Exception e) {
                NexusApplication.getLogger().error("[Minecraft] (AuthResolver) Couldn't resolve Zyneon Drive access: "+e.getMessage());
            }
        });
        if(((ApplicationFrame)moduleInfo.getApplication().getFrame()).getBrowser().getURL().contains("mje-login.html")) {
            moduleInfo.getConnector().resolveFrameRequest("java.sync.library");
        }
    }
}
