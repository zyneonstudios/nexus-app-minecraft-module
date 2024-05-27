package com.zyneonstudios.application.minecraft.java;

import com.google.gson.JsonArray;
import com.zyneonstudios.application.main.ApplicationConfig;
import com.zyneonstudios.nexus.index.Zyndex;
import live.nerotv.shademebaby.file.Config;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public record JavaStorage() {

    private static String modulePath = ApplicationConfig.getApplicationPath()+"modules/shared/";
    private static String urlBase = ApplicationConfig.getApplicationPath()+"temp/ui/mje/";

    private static Zyndex zyndex = null;

    private static String id = "shared";

    public static void init(String id) {
        id=id;
        modulePath = ApplicationConfig.getApplicationPath()+"modules/"+id+"/";
        if(ApplicationConfig.language.equals("de")) {
            Strings.notLoggedIn = "Nicht angemeldet";
            Strings.loggingIn = "Wird angemeldet";
            Strings.login = "Anmelden";
            Strings.logout = "Abmelden";
            Strings.pleaseWait = "Bitte warten";
            Strings.local = "Lokaler";
            Strings.library = "Bibliothek";
        } else {
            Strings.notLoggedIn = "Not logged in";
            Strings.loggingIn = "Logging in";
            Strings.login = "Log in";
            Strings.logout = "Log out";
            Strings.pleaseWait = "Please wait";
            Strings.local = "Local";
            Strings.library = "Library";
        }
        Config index = new Config(modulePath+"zyndex/index.json");
        index.set("name",Strings.local+" Zyndex");
        index.set("url","file://"+ URLDecoder.decode(index.getJsonFile().getAbsolutePath().replace("\\\\","\\").replace("\\","/"), StandardCharsets.UTF_8));
        index.set("owner","Zyneon Studios NEXUS Application");
        index.checkEntry("instances",new JsonArray());
        CompletableFuture.runAsync(()-> zyndex = new Zyndex(index));
    }

    public static Zyndex getZyndex() {
        return zyndex;
    }

    public static String getModulePath() {
        return modulePath;
    }

    public static String getUrlBase() {
        return urlBase+ApplicationConfig.language+"/";
    }

    public record Strings() {

        public static String notLoggedIn = "Not logged in";
        public static String loggingIn = "Logging in";
        public static String login = "Log in";
        public static String logout = "Log out";
        public static String pleaseWait = "Please wait";
        public static String local = "Local";
        public static String library = "Library";

    }
}