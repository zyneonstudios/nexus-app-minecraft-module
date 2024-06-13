package com.zyneonstudios.application.minecraft.java.integrations.zyndex;

import com.zyneonstudios.application.main.NexusApplication;
import com.zyneonstudios.nexus.instance.Zynstance;
import live.nerotv.shademebaby.file.Config;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class LocalInstance extends Zynstance {

    private final File directory;
    private final Config config;

    public LocalInstance(File file) {
        super(file);
        this.directory = file.getParentFile();
        String path = getPath().toString();
        if(path.endsWith("/")) {
            path = path + "meta/instanceSettings.json";
        } else {
            path = path + "/meta/instanceSettings.json";
        }
        config = new Config(path);
    }

    public File getDirectory() {
        return directory;
    }

    public Path getPath() {
        return Path.of(URLDecoder.decode(directory.getAbsolutePath().replace("\\\\","\\").replace("\\","/"), StandardCharsets.UTF_8));
    }

    public Config getConfig() {
        return config;
    }

    public int getMemory() {
        if(config.get("settings.memory")!=null) {
            try {
                return config.getInt("settings.memory");
            } catch (Exception e) {
                NexusApplication.getLogger().error("[Minecraft] Couldn't read memory int ("+getConfig().getPath()+"): "+e.getMessage());
            }
        }
        return 1024;
    }
}