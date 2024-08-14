package com.zyneonstudios.application;

import com.zyneonstudios.application.frame.web.ApplicationFrame;
import com.zyneonstudios.application.main.ApplicationStorage;
import com.zyneonstudios.application.main.NexusApplication;
import com.zyneonstudios.application.minecraft.java.JavaConnector;
import com.zyneonstudios.application.minecraft.java.JavaStorage;
import com.zyneonstudios.application.minecraft.java.authentication.MicrosoftAuthenticator;
import com.zyneonstudios.application.minecraft.java.integrations.curseforge.CurseForgeCategories;
import com.zyneonstudios.application.minecraft.java.integrations.zyndex.ZyndexIntegration;
import com.zyneonstudios.application.modules.ApplicationModule;
import live.nerotv.shademebaby.file.Config;
import live.nerotv.shademebaby.logger.Logger;
import live.nerotv.shademebaby.utils.FileUtil;
import live.nerotv.shademebaby.utils.StringUtil;

import javax.crypto.KeyGenerator;
import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.CompletableFuture;

public class MinecraftJavaAddon extends ApplicationModule {

    private MicrosoftAuthenticator authenticator = null;
    private AuthState authState = AuthState.LOGGED_OUT;
    private final byte[] key;

    public MinecraftJavaAddon(NexusApplication application, String id, String name, String version, String authors) {
        super(application, id, name, version, authors);
        try {
            KeyGenerator keyGenerator;
            keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            key = keyGenerator.generateKey().getEncoded();
        } catch (NoSuchAlgorithmException e) {
            onDisable();
            throw new RuntimeException(e);
        }
    }

    public MicrosoftAuthenticator getAuthenticator(byte[] key) {
        if(key==this.key) {
            if (authenticator == null) {
                authenticator = createNewAuthenticator();
            }
            return authenticator;
        } else {
            return null;
        }
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
        load();
    }

    private void load() {
        NexusApplication.getLogger().log(" ");
        NexusApplication.getLogger().log("==(GAME MODULE)===================================================================");
        NexusApplication.getLogger().log("Module: "+getName()+" (id: "+getId()+")");
        NexusApplication.getLogger().log("Authors: "+getAuthors());
        NexusApplication.getLogger().log("Version: "+getVersion());
        NexusApplication.getLogger().log("--------------------------------------------------------------------(NEXUS Team)--");
        String prefix = "["+getName()+"] ";
        NexusApplication.getLogger().log(prefix+"Loading...");

        NexusApplication.getLogger().log(prefix+ "Building module storage...");
        JavaStorage.init(getId());

        NexusApplication.getLogger().log(prefix+ "Building UI...");
        update();

        NexusApplication.getLogger().log(prefix+ "Initializing CurseForge categories...");
        CurseForgeCategories.init();

        NexusApplication.getLogger().log(prefix+"Setting module connector to new JavaConnector...");
        setConnector(new JavaConnector(this,key));

        NexusApplication.getLogger().log(prefix+ "Caching NEX...");
        ZyndexIntegration.searchModpacks("",(ApplicationFrame)getApplication().getFrame());

        CompletableFuture.runAsync(()->{
            NexusApplication.getLogger().log(prefix+ "Loading Microsoft authentication...");
            createNewAuthenticator();
            if(authenticator.isLoggedIn()) {
                NexusApplication.getLogger().log(prefix+ "Logged in as "+authenticator.getAuthInfos().getUsername()+": "+authenticator.getAuthInfos().getUuid());
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
        CompletableFuture.runAsync(this::enable);
    }

    public void enable() {
        NexusApplication.getLogger().log(" ");
        NexusApplication.getLogger().log("==(GAME MODULE)===================================================================");
        NexusApplication.getLogger().log("Module: "+getName()+" (id: "+getId()+")");
        NexusApplication.getLogger().log("Authors: "+getAuthors());
        NexusApplication.getLogger().log("Version: "+getVersion());
        NexusApplication.getLogger().log("--------------------------------------------------------------------(NEXUS Team)--");
        String prefix = "["+getName()+"] ";
        NexusApplication.getLogger().log(prefix+"Enabling...");
        NexusApplication.getLogger().log(prefix+"Enabled!");
        NexusApplication.getLogger().log(" ");
    }

    @Override
    public void onDisable() {
        NexusApplication.getLogger().log(" ");
        NexusApplication.getLogger().log("==(GAME MODULE)===================================================================");
        NexusApplication.getLogger().log("Module: "+getName()+" (id: "+getId()+")");
        NexusApplication.getLogger().log("Authors: "+getAuthors());
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
            if(new File(ApplicationStorage.getApplicationPath() + "temp/ui/").exists()) {
                logger.debug("[Minecraft] Deleted old ui files: "+new File(ApplicationStorage.getApplicationPath() + "temp/ui/").delete());
            }
            logger.debug("[Minecraft] Created new ui path: "+new File(ApplicationStorage.getApplicationPath() + "temp/ui/").mkdirs());
            FileUtil.extractResourceFile("html.zip",ApplicationStorage.getApplicationPath()+"temp/mje.zip", MinecraftJavaAddon.class);
            FileUtil.unzipFile(ApplicationStorage.getApplicationPath()+"temp/mje.zip", ApplicationStorage.getApplicationPath() + "temp/ui");
            logger.debug("[Minecraft] Deleted ui archive: "+new File(ApplicationStorage.getApplicationPath()+"temp/mje.zip").delete());
        } catch (Exception e) {
            logger.error("[Minecraft] Couldn't update application user interface: "+e.getMessage());
        }
        logger.debug("[Minecraft] Deleted old updatar json: "+new File(ApplicationStorage.getApplicationPath() + "updater.json").delete());
        logger.debug("[Minecraft] Deleted older updater json: "+new File(ApplicationStorage.getApplicationPath() + "version.json").delete());
    }

    public static void main(String[] args) {
        ArrayList<String> arguments = new ArrayList<>(Arrays.stream(args).toList());
        arguments.add("--test");
        arguments.add("--debug");
        arguments.add("--path:D:/Workspaces/IntelliJ/nexus-app/application-main/target/run/");
        arguments.add("--ui:file://D:/Workspaces/IntelliJ/nexus-app/application-ui/content/");
        args = arguments.toArray(new String[0]);
        NexusApplication application = new NexusApplication(args);
        NexusApplication.getLogger().setDebugEnabled(true);
        try {
            String v = new SimpleDateFormat("yyyy.M.d/HH-mm-ss").format(Calendar.getInstance().getTime());
            NexusApplication.getModuleLoader().loadModule(new MinecraftJavaAddon(application,"nexus-minecraft-module","Minecraft (Test)", v+"_"+StringUtil.generateAlphanumericString(4), "Zyneon Studios, Zyneon Nexus"));
        } catch (Exception ignore) {}
        application.launch();
    }
}
