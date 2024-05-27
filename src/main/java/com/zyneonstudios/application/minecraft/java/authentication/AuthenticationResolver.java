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
        ((ApplicationFrame)moduleInfo.getApplication().getFrame()).executeJavaScript("mjePreAuth('"+ JavaStorage.Strings.loggingIn +"...','"+JavaStorage.Strings.pleaseWait+" <i class=\\'bx bx-loader-alt bx-spin\\'></i>');");
    }

    public void postAuth(String username, String uuid) {
        ((ApplicationFrame)moduleInfo.getApplication().getFrame()).executeJavaScript("mjeLogin('"+username+"','"+uuid+"','"+ JavaStorage.Strings.logout +"');");
    }
}
