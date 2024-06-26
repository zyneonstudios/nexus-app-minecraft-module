package com.zyneonstudios.application.minecraft.java.authentication;

import com.zyneonstudios.application.MinecraftJavaAddon;
import com.zyneonstudios.application.frame.web.ApplicationFrame;
import com.zyneonstudios.application.minecraft.java.JavaStorage;

public class AuthenticationResolver {

    private final MinecraftJavaAddon moduleInfo;

    public AuthenticationResolver(MinecraftJavaAddon moduleInfo) {
        this.moduleInfo = moduleInfo;
    }

    public void preAuth() {
        moduleInfo.setAuthState(MinecraftJavaAddon.AuthState.LOGGING_IN);
        ((ApplicationFrame)moduleInfo.getApplication().getFrame()).openCustomPage("Minecraft: Java Edition Login","mje-authentication",JavaStorage.getUrlBase()+"mje-login.html?enable=false");
    }

    public void postAuth(String username, String uuid) {
        moduleInfo.setAuthState(MinecraftJavaAddon.AuthState.LOGGED_IN);
        if(((ApplicationFrame)moduleInfo.getApplication().getFrame()).getBrowser().getURL().contains("mje-login.html")) {
            moduleInfo.getConnector().resolveFrameRequest("java.sync.library");
        }
    }
}
