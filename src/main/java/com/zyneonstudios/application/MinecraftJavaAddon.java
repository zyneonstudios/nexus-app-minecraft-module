package com.zyneonstudios.application;

import com.zyneonstudios.application.main.ApplicationConfig;
import com.zyneonstudios.application.main.NexusApplication;
import com.zyneonstudios.application.minecraft.java.JavaConnector;
import com.zyneonstudios.application.minecraft.java.JavaStorage;
import com.zyneonstudios.application.minecraft.java.authentication.MicrosoftAuthenticator;
import com.zyneonstudios.application.modules.ApplicationModule;
import live.nerotv.shademebaby.file.Config;
import live.nerotv.shademebaby.logger.Logger;
import live.nerotv.shademebaby.utils.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class MinecraftJavaAddon extends ApplicationModule {

    private MicrosoftAuthenticator authenticator = null;
    private AuthState authState = AuthState.LOGGED_OUT;

    public MinecraftJavaAddon(NexusApplication application) {
        super(application, "minecraft-java-edition", "Minecraft: Java Edition", "2024.5-alpha.6", "Zyneon Studios & NEXUS Team: nerotvlive");
    }

    public MicrosoftAuthenticator getAuthenticator() {
        if(authenticator==null) {
            authenticator = createNewAuthenticator();
        }
        return authenticator;
    }

    public AuthState getAuthState() {
        return authState;
    }

    public void setAuthState(AuthState authState) {
        this.authState = authState;
    }

    public MicrosoftAuthenticator createNewAuthenticator() {
        if(authenticator!=null) {
            File old = authenticator.getSaveFile();
            authenticator.destroy();
            authenticator = null;
            System.gc();
            if(!old.delete()) {
                NexusApplication.getLogger().error("[Minecraft: Java Edition] Couldn't delete old auth file...");
                Config oldAuth = new Config(old);
                try {
                    oldAuth.delete("op");
                    oldAuth.delete("opapi");
                } catch (Exception e) {
                    NexusApplication.getLogger().error("[Minecraft: Java Edition] Couldn't reset auth credentials...");
                    return null;
                }
            }
        }
        authenticator = new MicrosoftAuthenticator(this);
        return authenticator;
    }

    @Override
    public void onLoad() {
        NexusApplication.getLogger().log(" ");
        NexusApplication.getLogger().log("==(GAME MODULE)===================================================================");
        NexusApplication.getLogger().log("Module: "+getName()+" (id: "+getId()+")");
        NexusApplication.getLogger().log("Authors: "+getAuthor());
        NexusApplication.getLogger().log("Version: "+getVersion());
        NexusApplication.getLogger().log("====================================================================(NEXUS Team)==");
        String prefix = "["+getName()+"] ";
        NexusApplication.getLogger().log(prefix+"Loading...");

        NexusApplication.getLogger().log(prefix+ "Building module storage...");
        JavaStorage.init(getId());

        NexusApplication.getLogger().log(prefix+ "Building UI...");
        update();

        authState = AuthState.LOGGING_IN;
        CompletableFuture.runAsync(()->{
            NexusApplication.getLogger().log(prefix+ "Loading Microsoft authentication...");
            if(getAuthenticator().isLoggedIn()) {
                NexusApplication.getLogger().log(prefix+ "Logged in as "+getAuthenticator().getAuthInfos().getUsername()+": "+getAuthenticator().getAuthInfos().getUuid());
                authState = AuthState.LOGGED_IN;
            } else {
                NexusApplication.getLogger().log(prefix+ "Not logged in...");
                authState = AuthState.LOGGED_OUT;
            }
        });

        NexusApplication.getLogger().log(prefix+"Loaded!");
        NexusApplication.getLogger().log(" ");
    }

    @Override
    public void onEnable() {
        NexusApplication.getLogger().log(" ");
        NexusApplication.getLogger().log("==(GAME MODULE)===================================================================");
        NexusApplication.getLogger().log("Module: "+getName()+" (id: "+getId()+")");
        NexusApplication.getLogger().log("Authors: "+getAuthor());
        NexusApplication.getLogger().log("Version: "+getVersion());
        NexusApplication.getLogger().log("====================================================================(NEXUS Team)==");
        String prefix = "["+getName()+"] ";
        NexusApplication.getLogger().log(prefix+"Enabling...");
        NexusApplication.getLogger().log(prefix+"Setting module connector to new JavaConnector...");
        setConnector(new JavaConnector(this));
        NexusApplication.getLogger().log(prefix+"Enabled!");
        NexusApplication.getLogger().log(" ");
    }

    @Override
    public void onDisable() {
        NexusApplication.getLogger().log(" ");
        NexusApplication.getLogger().log("==(GAME MODULE)===================================================================");
        NexusApplication.getLogger().log("Module: "+getName()+" (id: "+getId()+")");
        NexusApplication.getLogger().log("Authors: "+getAuthor());
        NexusApplication.getLogger().log("Version: "+getVersion());
        NexusApplication.getLogger().log("====================================================================(NEXUS Team)==");
        String prefix = "["+getName()+"] ";
        NexusApplication.getLogger().log(prefix+"Disabling...");
        NexusApplication.getLogger().log(prefix+"Disabled!");
        NexusApplication.getLogger().log(" ");
    }

    public static void main(String[] args) {
        ArrayList<String> arguments = new ArrayList<>(Arrays.stream(args).toList());
        arguments.add("--test");
        arguments.add("--path:A:/Sync/OneDrive/Projekte/Code/Zyneon-Application/application-main/target/run/");
        arguments.add("--ui:A:/Sync/OneDrive/Projekte/Code/Zyneon-Application/application-ui/content/");
        args = arguments.toArray(new String[0]);
        new ApplicationConfig(args);
        NexusApplication application = new NexusApplication();
        application.getModuleLoader().loadModule(new MinecraftJavaAddon(application));
        application.launch();
    }

    public enum AuthState {
        LOGGING_IN,
        LOGGED_IN,
        LOGGED_OUT
    }

    private void update() {
        Logger logger = NexusApplication.getLogger();
        try {
            if(new File(ApplicationConfig.getApplicationPath() + "temp/ui/mje/").exists()) {
                logger.debug("[APP] Deleted old ui files: "+new File(ApplicationConfig.getApplicationPath() + "temp/ui/mje/").delete());
            }
            logger.debug("[APP] Created new ui path: "+new File(ApplicationConfig.getApplicationPath() + "temp/ui/mje/").mkdirs());
            FileUtil.extractResourceFile("html.zip",ApplicationConfig.getApplicationPath()+"temp/mje.zip", MinecraftJavaAddon.class);
            FileUtil.unzipFile(ApplicationConfig.getApplicationPath()+"temp/mje.zip", ApplicationConfig.getApplicationPath() + "temp/ui/mje");
            logger.debug("[APP] Deleted ui archive: "+new File(ApplicationConfig.getApplicationPath()+"temp/mje.zip").delete());
        } catch (Exception e) {
            logger.error("[APP] Couldn't update application user interface: "+e.getMessage());
        }
        logger.debug("[APP] Deleted old updatar json: "+new File(ApplicationConfig.getApplicationPath() + "updater.json").delete());
        logger.debug("[APP] Deleted older updater json: "+new File(ApplicationConfig.getApplicationPath() + "version.json").delete());
    }
}
