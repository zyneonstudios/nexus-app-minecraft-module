package com.zyneonstudios.application.minecraft.java.integrations.zyndex;

import com.zyneonstudios.application.main.NexusApplication;
import com.zyneonstudios.nexus.instance.Instance;
import fr.flowarg.flowupdater.versions.ForgeVersionType;
import live.nerotv.shademebaby.file.Config;
import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;

public class LocalInstance implements Instance {

    // OBJECT STUFF
    private final File directory;
    private final Config settings;
    private final Config config;

    // INFO STUFF
    private ArrayList<String> info_authors = new ArrayList<>();
    private String info_name = null;
    private String info_summary = null;
    private String info_version = null;

    // META STUFF
    private ArrayList<String> meta_changelogs = new ArrayList<>();
    private String meta_description = null;
    private String meta_download = null;
    private ForgeVersionType meta_forgeType = null;
    private String meta_id = null;
    private String meta_infoCard = null;;
    private String meta_infoText = null;
    private boolean meta_isEditable = false;
    private boolean meta_isHidden = true;
    private String meta_location = null;
    private String meta_origin = null;
    private ArrayList<String> meta_tags = new ArrayList<>();
    private ArrayList<String> meta_versions = new ArrayList<>();

    // RESOURCES STUFF
    private String resources_background = null;
    private String resources_icon = null;
    private String resources_logo = null;
    private String resources_thumbnail = null;

    // VERSIONS STUFF
    private String versions_fabric = null;
    private String versions_forge = null;
    private String versions_minecraft = null;
    private String versions_neoforge = null;
    private String versions_quilt = null;

    // OTHER JSON STUFF
    private String scheme = null;
    private String modloader = null;

    private void init() {
        if(config.get("instance.info.authors")!=null) {
            info_authors = (ArrayList<String>)config.get("instance.info.authors");
        } else if(config.get("instance.info.author")!=null) {
            info_authors = new ArrayList<>();
            info_authors.add(config.getString("instance.info.author"));
        } else {
            info_authors = new ArrayList<>();
            info_authors.add("Anonymous");
        }
        if(config.get("instance.info.name")!=null) {
            info_name = config.getString("instance.info.name");
        } else {
            info_name = "Unknown";
        }
        if(config.get("instance.info.summary")!=null) {
            info_summary = config.getString("instance.info.summary");
        } else if(config.get("instance.info.description")!=null) {
            info_summary = config.getString("instance.info.description");
        } else {
            info_summary = "No summary...";
        }
        if(config.get("instance.info.version")!=null) {
            info_version = config.getString("instance.info.version");
        } else {
            info_version = "-1";
        }

        if(config.get("instance.meta.changelogs")!=null) {
            meta_changelogs = (ArrayList<String>)config.get("instance.meta.changelogs");
        } else {
            meta_changelogs = new ArrayList<>();
        }
        if(config.get("instance.meta.description")!=null) {
            meta_description = config.getString("instance.meta.description");
        } else {
            meta_description = "No description...";
        }
        if(config.get("instance.meta.download")!=null) {
            meta_download = config.getString("instance.meta.download");
        } else {
            meta_download = "No download...";
        }
        if(config.get("instance.meta.forgeType")!=null) {
            meta_forgeType = ForgeVersionType.valueOf(config.getString("instance.meta.forgeType"));
        } else {
            meta_forgeType = null;
        }
        if(config.get("instance.meta.id")!=null) {
            meta_id = config.getString("instance.meta.id");
        } else {
            meta_id = "unknown";
        }
        if(config.get("instance.meta.infoCard")!=null) {
            meta_infoCard = config.getString("instance.meta.infoCard");
        } else {
            meta_infoCard = null;
        }
        if(config.get("instance.meta.infoText")!=null) {
            meta_infoText = config.getString("instance.meta.infoText");
        } else {
            meta_infoText = null;
        }
        if(config.get("instance.meta.isEditable")!=null) {
            meta_isEditable = config.getBoolean("instance.meta.isEditable");
        } else {
            meta_isEditable = false;
        }
        if(config.get("instance.meta.isHidden")!=null) {
            meta_isHidden = config.getBoolean("instance.meta.isHidden");
        } else {
            meta_isHidden = true;
        }
        if(config.get("instance.meta.location")!=null) {
            meta_location = config.getString("instance.meta.location");
        } else {
            meta_location = null;
        }
        if(config.get("instance.meta.origin")!=null) {
            meta_origin = config.getString("instance.meta.origin");
        } else {
            meta_origin = null;
        }
        if(config.get("instance.meta.tags")!=null) {
            meta_tags = (ArrayList<String>)config.get("instance.meta.tags");
        } else {
            meta_tags = new ArrayList<>();
        }
        if(config.get("instance.meta.versions")!=null) {
            meta_versions = (ArrayList<String>)config.get("instance.meta.versions");
        } else {
            meta_versions = new ArrayList<>();
        }

        if(config.get("instance.resources.background")!=null) {
            resources_background = config.getString("instance.resources.background");
        } else {
            resources_background = null;
        }
        if(config.get("instance.resources.icon")!=null) {
            resources_icon = config.getString("instance.resources.icon");
        } else {
            resources_icon = null;
        }
        if(config.get("instance.resources.logo")!=null) {
            resources_logo = config.getString("instance.resources.logo");
        } else {
            resources_logo = null;
        }
        if(config.get("instance.resources.thumbnail")!=null) {
            resources_thumbnail = config.getString("instance.resources.thumbnail");
        } else {
            resources_thumbnail = null;
        }

        if(config.get("instance.versions.fabric")!=null) {
            versions_fabric = config.getString("instance.versions.fabric");
        } else {
            versions_fabric = null;
        }
        if(config.get("instance.versions.forge")!=null) {
            versions_forge = config.getString("instance.versions.forge");
        } else {
            versions_forge = null;
        }
        if(config.get("instance.versions.minecraft")!=null) {
            versions_minecraft = config.getString("instance.versions.minecraft");
        } else {
            versions_minecraft = null;
        }
        if(config.get("instance.versions.neoforge")!=null) {
            versions_neoforge = config.getString("instance.versions.neoforge");
        } else {
            versions_neoforge = null;
        }
        if(config.get("instance.versions.quilt")!=null) {
            versions_quilt = config.getString("instance.versions.quilt");
        } else {
            versions_quilt = null;
        }

        if(versions_quilt!=null) {
            modloader = "Quilt";
        } else if(versions_fabric!=null) {
            modloader = "Fabric";
        } else if(meta_forgeType!=null&&versions_forge!=null) {
            modloader = "Forge";
        } else if(versions_neoforge!=null) {
            modloader = "NeoForge";
        } else {
            modloader = "Vanilla";
        }

        if(config.get("scheme")!=null) {
            scheme = config.getString("scheme");
        } else {
            scheme = null;
        }
    }

    public LocalInstance(File file) {
        config = new Config(file);
        this.directory = file.getParentFile();
        String path = getPath().toString();
        if(path.endsWith("/")) {
            path = path + "meta/instanceSettings.json";
        } else {
            path = path + "/meta/instanceSettings.json";
        }
        settings = new Config(path);
        init();
    }

    public File getInstanceFile() {
        return config.getJsonFile();
    }

    public File getDirectory() {
        return directory;
    }

    public Path getPath() {
        return Path.of(URLDecoder.decode(directory.getAbsolutePath().replace("\\\\","\\").replace("\\","/"), StandardCharsets.UTF_8));
    }

    public Config getSettings() {
        return settings;
    }

    public int getMemory() {
        if(settings.get("settings.memory")!=null) {
            try {
                return settings.getInt("settings.memory");
            } catch (Exception e) {
                NexusApplication.getLogger().error("[Minecraft] Couldn't read memory int ("+settings.getPath()+"): "+e.getMessage());
            }
        }
        return 1024;
    }


    // INFO GETTER
    @Override
    public String getAuthor() {
        return info_authors.toString().replace("[","").replace("]","");
    }

    @Override
    public ArrayList<String> getAuthors() {
        return info_authors;
    }

    @Override
    public String getName() {
        return info_name;
    }

    @Override
    public String getSummary() {
        return info_summary;
    }

    @Override
    public String getVersion() {
        return info_version;
    }


    // META GETTER
    @Override
    public ArrayList<String> getChangelogs() {
        return meta_changelogs;
    }

    @Override
    public String getDescription() {
        return meta_description;
    }

    @Override
    public String getDownloadUrl() {
        return meta_download;
    }

    @Override
    public String getForgeType() {
        return meta_forgeType.toString();
    }

    public ForgeVersionType getForgeVersionType() {
        return meta_forgeType;
    }

    @Override
    public String getId() {
        return meta_id;
    }

    @Override
    public String getIndexUrl() {
        return meta_origin;
    }

    @Override
    public String getInfoCard() {
        return meta_infoCard;
    }

    @Override
    public String getInfoText() {
        return meta_infoText;
    }

    @Override
    public Boolean isEditable() {
        return meta_isEditable;
    }

    @Override
    public Boolean isHidden() {
        return meta_isHidden;
    }

    @Override
    public String getLocation() {
        return meta_location;
    }

    @Override
    public String getOrigin() {
        return config.getJsonFile().getAbsolutePath();
    }

    @Override
    public ArrayList<String> getTags() {
        return meta_tags;
    }

    @Override
    public String getTagString() {
        return meta_tags.toString().replace("[","").replace("]","");
    }

    @Override
    public ArrayList<String> getVersions() {
        return meta_versions;
    }


    // RESOURCES GETTER
    @Override
    public String getBackgroundUrl() {
        return resources_background;
    }

    @Override
    public String getIconUrl() {
        return resources_icon;
    }

    @Override
    public String getLogoUrl() {
        return resources_logo;
    }

    @Override
    public String getThumbnailUrl() {
        return resources_thumbnail;
    }


    // VERSIONS GETTER
    @Override
    public String getFabricVersion() {
        return versions_fabric;
    }

    @Override
    public String getForgeVersion() {
        return versions_forge;
    }

    @Override
    public String getMinecraftVersion() {
        return versions_minecraft;
    }

    @Override
    public String getNeoForgeVersion() {
        return versions_neoforge;
    }

    @Override
    public String getQuiltVersion() {
        return versions_quilt;
    }


    // GETTER
    @Override
    public String getModloader() {
        return modloader;
    }

    @Override
    public String getSchemeVersion() {
        return scheme;
    }
}