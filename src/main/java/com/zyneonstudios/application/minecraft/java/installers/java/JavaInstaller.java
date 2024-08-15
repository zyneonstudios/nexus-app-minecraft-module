package com.zyneonstudios.application.minecraft.java.installers.java;

import com.zyneonstudios.application.main.ApplicationStorage;
import com.zyneonstudios.application.main.NexusApplication;
import com.zyneonstudios.application.minecraft.java.installers.Installer;
import live.nerotv.shademebaby.file.OnlineConfig;
import live.nerotv.shademebaby.utils.FileUtil;

import java.io.File;

public class JavaInstaller implements Installer {

    private Java runtimeVersion;
    private OperatingSystem operatingSystem;
    private Architecture architecture;

    public JavaInstaller() {
        runtimeVersion = null;
        operatingSystem = null;
        architecture = null;
    }

    public JavaInstaller(Java runtimeVersion, OperatingSystem operatingSystem, Architecture architecture) {
        this.runtimeVersion = runtimeVersion;
        this.operatingSystem = operatingSystem;
        this.architecture = architecture;
    }

    public String getVersionString() {
        String os = "null-";
        if(operatingSystem!=null) {
            os = operatingSystem.toString().toLowerCase()+"-";
        }
        String a = "null_";
        if(architecture!=null) {
            a = architecture.toString().toLowerCase()+"_";
        }
        String jre = "jre-null";
        if(runtimeVersion!=null) {
            if(runtimeVersion.equals(Java.Runtime_8)) {
                jre = "jre-8";
            } else if(runtimeVersion.equals(Java.Runtime_11)) {
                jre = "jre-11";
            } else {
                jre = "jre-21";
            }
        }
        return os+a+jre;
    }

    public Architecture getArchitecture() {
        return architecture;
    }

    public OperatingSystem getOperatingSystem() {
        return operatingSystem;
    }

    public Java getRuntimeVersion() {
        return runtimeVersion;
    }

    public void setArchitecture(Architecture architecture) {
        this.architecture = architecture;
    }

    public void setOperatingSystem(OperatingSystem operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public void setRuntimeVersion(Java runtimeVersion) {
        this.runtimeVersion = runtimeVersion;
    }

    @Override
    public boolean install() {
        try {
            String versionString = getVersionString();
            if (versionString.contains("null")) {
                throw new NullPointerException("Couldn't find such a java version");
            } else {
                NexusApplication.getLogger().debug("[INSTALLER] (JAVA) Gathering java information...");
                OnlineConfig index = new OnlineConfig("https://raw.githubusercontent.com/danieldieeins/ZyneonApplicationContent/main/l/application.json");
                String download = index.getString("runtime." + versionString);
                String zipPath = ApplicationStorage.getApplicationPath() + "libs/" + runtimeVersion + ".zip";
                NexusApplication.getLogger().debug("[INSTALLER] (JAVA) Starting download from " + download + " to " + zipPath + "...");
                FileUtil.downloadFile(download, zipPath);
                FileUtil.unzipFile(zipPath, ApplicationStorage.getApplicationPath() + "libs/");
                NexusApplication.getLogger().debug("[INSTALLER] (JAVA) Deleted zip-File: " + new File(zipPath).delete());
                NexusApplication.getLogger().log("[INSTALLER] (JAVA) Installed Java Runtime: " + versionString + "!");
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}