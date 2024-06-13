package com.zyneonstudios.application;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zyneonstudios.application.main.ApplicationConfig;
import com.zyneonstudios.application.main.NexusApplication;
import com.zyneonstudios.application.minecraft.java.JavaConnector;
import com.zyneonstudios.application.minecraft.java.JavaStorage;
import com.zyneonstudios.application.minecraft.java.authentication.MicrosoftAuthenticator;
import com.zyneonstudios.application.modules.ApplicationModule;
import com.zyneonstudios.nexus.index.Zyndex;
import fr.theshark34.openlauncherlib.external.ExternalLaunchProfile;
import fr.theshark34.openlauncherlib.external.ExternalLauncher;
import fr.theshark34.openlauncherlib.minecraft.*;
import live.nerotv.shademebaby.file.Config;
import live.nerotv.shademebaby.logger.Logger;
import live.nerotv.shademebaby.utils.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class MinecraftJavaAddon extends ApplicationModule {

    public static final File properties = FileUtil.getResourceFile("nexus.json", MinecraftJavaAddon.class);
    private static String version ="Unknown";
    private static String id ="nexus-minecraft-module";
    private static String name ="Minecraft";
    private static String authors ="Zyneon Studios & NEXUS Team: nerotvlive";
    private MicrosoftAuthenticator authenticator = null;
    private AuthState authState = AuthState.LOGGED_OUT;

    public MinecraftJavaAddon(NexusApplication application) {
        super(application, id, name, version, authors);
        try {
            JsonObject properties = new Gson().fromJson(new BufferedReader(new InputStreamReader(new FileInputStream(MinecraftJavaAddon.properties))), JsonObject.class).getAsJsonArray("modules").get(0).getAsJsonObject();
            version = properties.get("version").getAsString();
            id = properties.get("id").getAsString();
            name = properties.get("name").getAsString();
            StringBuilder authors = new StringBuilder();
            for(JsonElement name : properties.getAsJsonArray("authors")) {
                String author = name.getAsString();
                if(authors.isEmpty()) {
                    authors = new StringBuilder(author);
                } else {
                    authors.append(" ,").append(author);
                }
            }
            MinecraftJavaAddon.authors = authors.toString();
        } catch (Exception e) {
            NexusApplication.getLogger().error("[Minecraft] Couldn't parse nexus.json properties: "+e.getMessage());
        }
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override @Deprecated
    public String getAuthor() {
        return authors;
    }

    public String getAuthors() {
        return authors;
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
                NexusApplication.getLogger().error("[Minecraft] Couldn't delete old auth file...");
                Config oldAuth = new Config(old);
                try {
                    oldAuth.delete("op");
                    oldAuth.delete("opapi");
                } catch (Exception e) {
                    NexusApplication.getLogger().error("[Minecraft] Couldn't reset auth credentials...");
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
        NexusApplication.getLogger().log("--------------------------------------------------------------------(NEXUS Team)--");
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
        NexusApplication.getLogger().log("--------------------------------------------------------------------(NEXUS Team)--");
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
        NexusApplication.getLogger().log("--------------------------------------------------------------------(NEXUS Team)--");
        String prefix = "["+getName()+"] ";
        NexusApplication.getLogger().log(prefix+"Disabling...");
        NexusApplication.getLogger().log(prefix+"Disabled!");
        NexusApplication.getLogger().log(" ");
    }

    public enum AuthState {
        LOGGING_IN,
        LOGGED_IN,
        LOGGED_OUT
    }

    private void update() {
        Logger logger = NexusApplication.getLogger();
        try {
            if(new File(ApplicationConfig.getApplicationPath() + "temp/ui/").exists()) {
                logger.debug("[Minecraft] Deleted old ui files: "+new File(ApplicationConfig.getApplicationPath() + "temp/ui/").delete());
            }
            logger.debug("[Minecraft] Created new ui path: "+new File(ApplicationConfig.getApplicationPath() + "temp/ui/").mkdirs());
            FileUtil.extractResourceFile("html.zip",ApplicationConfig.getApplicationPath()+"temp/mje.zip", MinecraftJavaAddon.class);
            FileUtil.unzipFile(ApplicationConfig.getApplicationPath()+"temp/mje.zip", ApplicationConfig.getApplicationPath() + "temp/ui");
            logger.debug("[Minecraft] Deleted ui archive: "+new File(ApplicationConfig.getApplicationPath()+"temp/mje.zip").delete());
        } catch (Exception e) {
            logger.error("[Minecraft] Couldn't update application user interface: "+e.getMessage());
        }
        logger.debug("[Minecraft] Deleted old updatar json: "+new File(ApplicationConfig.getApplicationPath() + "updater.json").delete());
        logger.debug("[Minecraft] Deleted older updater json: "+new File(ApplicationConfig.getApplicationPath() + "version.json").delete());
    }

    public static void main(String[] args) {
        /*ArrayList<String> arguments = new ArrayList<>(Arrays.stream(args).toList());
        arguments.add("--test");
        arguments.add("--debug");
        arguments.add("--path:B:/Workspaces/IntelliJ/Zyneon-Application/application-main/target/run/");
        arguments.add("--ui:B:/Workspaces/IntelliJ/Zyneon-Application/application-ui/content/");
        args = arguments.toArray(new String[0]);
        new ApplicationConfig(args);
        NexusApplication application = new NexusApplication();
        NexusApplication.getLogger().setDebugEnabled(true);
        try {
            application.getModuleLoader().loadModule(new MinecraftJavaAddon(application));
        } catch (Exception ignore) {}
        application.launch();*/
    }
}
