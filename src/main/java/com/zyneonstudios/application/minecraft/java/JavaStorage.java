package com.zyneonstudios.application.minecraft.java;

import com.google.gson.JsonArray;
import com.zyneonstudios.application.main.ApplicationConfig;
import com.zyneonstudios.application.main.NexusApplication;
import com.zyneonstudios.application.minecraft.java.integrations.zyndex.LocalInstance;
import com.zyneonstudios.application.minecraft.java.integrations.zyndex.LocalZyndex;
import live.nerotv.shademebaby.file.Config;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public record JavaStorage() {

    private static String lastInstance = null;
    private static String modulePath = ApplicationConfig.getApplicationPath()+"modules/shared/";
    private static String urlBase = ApplicationConfig.getApplicationPath()+"temp/ui/";
    //private static String urlBase = "file:///home/nerotvlive/Dokumente/Workspaces/IntelliJ/nexus-app-minecraft-module/src/main/html/";

    private static LocalZyndex zyndex = null;
    private static Config config = null;

    private static String id = "shared";

    public static int memory = 1024;

    public static void init(String id) {
        id=id;
        modulePath = ApplicationConfig.getApplicationPath()+"modules/"+id+"/";

        config = new Config(modulePath + "config.json");
        config.checkEntry("settings.zyndex.local.paths",new JsonArray());
        if(config.get("settings.values.last.instance")!=null) {
            lastInstance = config.getString("settings.values.last.instance");
        }

        if(ApplicationConfig.language.equals("de")) {
            Strings.notLoggedIn = "Nicht angemeldet";
            Strings.loggingIn = "Wird angemeldet";
            Strings.login = "Anmelden";
            Strings.logout = "Abmelden";
            Strings.pleaseWait = "Bitte warten";
            Strings.local = "Lokaler";
            Strings.library = "Bibliothek";
            Strings.instance = "Instanz";
            Strings.instances = "Instanzen";
            Strings.addInstance = "Instanz hinzufügen";
            Strings.refreshInstances = "Instanzen neuladen";
        } else {
            Strings.notLoggedIn = "Not logged in";
            Strings.loggingIn = "Logging in";
            Strings.login = "Log in";
            Strings.logout = "Log out";
            Strings.pleaseWait = "Please wait";
            Strings.local = "Local";
            Strings.library = "Library";
            Strings.instance = "Instance";
            Strings.instances = "Instances";
            Strings.addInstance = "Add instance";
            Strings.refreshInstances = "Refresh instances";
            Strings.aboutMinecraftModule = "About this module";
        }
    }

    private static boolean reloading = false;
    @SuppressWarnings("unchecked")
    public static boolean reloadLocalZyndex() {
        if(!reloading) {
            try {
                reloading = true;
                try {
                    Config old = new Config(getApplicationConfigPath());
                    String oldPath = old.getString("settings.path.instances");
                    if(oldPath!=null) {
                        if(!oldPath.endsWith("/instances")&&!oldPath.endsWith("/instances/")) {
                            if(oldPath.endsWith("/")) {
                                oldPath = oldPath+"instances/";
                            } else {
                                oldPath = oldPath+"/instances/";
                            }
                        }
                        String officialPath = oldPath+"official/";
                        String zyneonPath = officialPath+"zyneonplus/";
                        ArrayList<String> instancePaths = (ArrayList<String>)config.get("settings.zyndex.local.paths");
                        if(!instancePaths.contains(oldPath)) {
                            instancePaths.add(oldPath);
                        }
                        if(!instancePaths.contains(officialPath)) {
                            instancePaths.add(officialPath);
                        }
                        if(!instancePaths.contains(zyneonPath)) {
                            instancePaths.add(zyneonPath);
                        }
                        config.set("settings.zyndex.local.paths",instancePaths);
                    }
                } catch (Exception e) {
                    NexusApplication.getLogger().error("Couldn't check old path: "+e.getMessage());
                }
                Config index = new Config(modulePath + "zyndex/index.json");
                index.set("name", Strings.local + " Zyndex");
                index.set("url", "file://" + URLDecoder.decode(index.getJsonFile().getAbsolutePath().replace("\\\\", "\\").replace("\\", "/"), StandardCharsets.UTF_8));
                index.set("owner", "Zyneon Studios NEXUS Application");
                index.set("instances", new JsonArray());
                zyndex = new LocalZyndex(index);
                scanInstances();
                reloading = false;
                return true;
            } catch (Exception e) {
                NexusApplication.getLogger().error("[Minecraft] Couldn't reload local zyndex: " + e.getMessage());
            }
        }
        reloading = false;
        return false;
    }

    @SuppressWarnings("unchecked")
    private static void scanInstances() {
        NexusApplication.getLogger().debug("[Minecraft] Scanning Java Edition instance paths...");
        ArrayList<String> instancePaths = (ArrayList<String>)config.get("settings.zyndex.local.paths");
        for(String instancePath_ : instancePaths) {
            File instancePath = new File(instancePath_);
            instancePath_ = instancePath.getAbsolutePath().replace("\\\\","\\").replace("\\","/");
            if(instancePath.exists()) {
                if(instancePath.isDirectory()) {
                    NexusApplication.getLogger().debug("[Minecraft]   -> Scanning "+instancePath_+"...");
                    try {
                        for (File file : Objects.requireNonNull(instancePath.listFiles())) {
                            if(file.isDirectory()) {
                                NexusApplication.getLogger().debug("[Minecraft]   -> Scanning "+file.getAbsolutePath().replace("\\\\","\\").replace("\\","/")+"...");
                                try {
                                    for (File instance : Objects.requireNonNull(file.listFiles())) {
                                        if(instance.getAbsolutePath().replace("\\\\","\\").replace("\\","/").endsWith("/zyneonInstance.json")) {
                                            scanFile(instance);
                                        }
                                    }
                                } catch (Exception ignore) {}
                            } else if(file.getAbsolutePath().replace("\\\\","\\").replace("\\","/").endsWith("/zyneonInstance.json")) {
                                scanFile(file);
                            }
                        }
                    } catch (Exception ignore) {}
                } else if(instancePath_.endsWith("/zyneonInstance.json")) {
                    scanFile(instancePath);
                }
            }
        }
    }

    private static void scanFile(File file) {
        String path = file.getAbsolutePath().replace("\\\\","\\").replace("\\","/");
        NexusApplication.getLogger().debug("[Minecraft]   -> Checking file "+path+"...");
        try {
            Config instance = new Config(file);
            LocalInstance zynstance = new LocalInstance(instance.getJsonFile());
            NexusApplication.getLogger().debug("[Minecraft]     -> Found instance "+zynstance.getName()+" v"+zynstance.getVersion()+" by "+zynstance.getAuthor()+"...");
            zyndex.addInstance(zynstance,path);
        } catch (Exception ignore) {}
    }

    public static void asyncReloadLocalZyndex() {
        CompletableFuture.runAsync(JavaStorage::reloadLocalZyndex);
    }

    public static LocalZyndex getLocalZyndex() {
        return zyndex;
    }

    public static String getModulePath() {
        return modulePath;
    }

    public static String getUrlBase() {
        return urlBase+ApplicationConfig.language+"/";
    }

    public static Config getConfig() {
        return config;
    }

    public static String getId() {
        return id;
    }

    public record Strings() {

        public static String notLoggedIn = "Not logged in";
        public static String loggingIn = "Logging in";
        public static String login = "Log in";
        public static String logout = "Log out";
        public static String pleaseWait = "Please wait";
        public static String local = "Local";
        public static String library = "Library";
        public static String instance = "Instance";
        public static String instances = "Instances";
        public static String addInstance = "Add instance";
        public static String refreshInstances = "Refresh instances";
        public static String aboutMinecraftModule = "About this module";

    }

    private static String getApplicationConfigPath() {
        String folderName = "Zyneon/Application";
        String appData;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            appData = System.getenv("LOCALAPPDATA");
        } else if (os.contains("mac")) {
            appData = System.getProperty("user.home") + "/Library/Application Support";
        } else {
            appData = System.getProperty("user.home") + "/.local/share";
        }
        Path folderPath = Paths.get(appData, folderName);
        try {
            Files.createDirectories(folderPath);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return URLDecoder.decode(folderPath + "/config.json", StandardCharsets.UTF_8);
    }

    public static String getLastInstance() {
        return config.getString("settings.values.last.instance");
    }
}