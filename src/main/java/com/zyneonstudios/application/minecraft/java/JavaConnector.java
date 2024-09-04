package com.zyneonstudios.application.minecraft.java;

import com.sun.management.OperatingSystemMXBean;
import com.zyneonstudios.application.MinecraftJavaAddon;
import com.zyneonstudios.application.frame.FrameConnector;
import com.zyneonstudios.application.frame.web.ApplicationFrame;
import com.zyneonstudios.application.main.ApplicationStorage;
import com.zyneonstudios.application.main.NexusApplication;
import com.zyneonstudios.application.minecraft.java.integrations.curseforge.CurseForgeIntegration;
import com.zyneonstudios.application.minecraft.java.integrations.modrinth.ModrinthIntegration;
import com.zyneonstudios.application.minecraft.java.integrations.zyndex.LocalInstance;
import com.zyneonstudios.application.minecraft.java.integrations.zyndex.ZyndexIntegration;
import com.zyneonstudios.application.minecraft.java.launchers.InstanceLauncher;
import com.zyneonstudios.application.modules.ModuleConnector;
import com.zyneonstudios.nexus.instance.Instance;
import com.zyneonstudios.nexus.instance.ReadableZynstance;
import com.zyneonstudios.verget.Verget;
import com.zyneonstudios.verget.minecraft.MinecraftVerget;

import java.lang.management.ManagementFactory;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class JavaConnector extends ModuleConnector {

    private final MinecraftJavaAddon module;
    private final ApplicationFrame frame;
    private final byte[] authKey;

    public JavaConnector(MinecraftJavaAddon module, byte[] authKey) {
        super(module);
        this.authKey = authKey;
        this.module = module;
        this.frame = (ApplicationFrame) this.module.getApplication().getFrame();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void resolveRequest(String request) {
        if (request.startsWith("sync.discover.search.nexus-minecraft-module_java")) {
            resolveSearchRequest(request.replaceFirst("sync.discover.search.nexus-minecraft-module_java", ""));
        } else if (request.startsWith("java.auth.")) {
            resolveAuthRequest(request.replaceFirst("java.auth.", ""));
        } else if (request.equals("sync.library.module.nexus-minecraft-module_java") || request.equals("sync.library.module.minecraft-java-edition")) {
            resolveInitRequest("library");
        } else if (request.startsWith("java.settings.")) {
            resolveSettingsRequest(request.replaceFirst("java.settings.", ""));
        } else if (request.startsWith("java.overlay")) {
            frame.executeJavaScript("enableOverlay('https://www.zyneonstudios.com');");
        } else if (request.startsWith("java.init.")) {
            resolveInitRequest(request.replaceFirst("java.init.", ""));
        } else if (request.startsWith("java.button.")) {
            resolveButtonRequest(request.replaceFirst("java.button.", ""));
        } else if (request.startsWith("java.sync.")) {
            resolveSyncRequest(request.replaceFirst("java.sync.", ""));
        } else if (request.startsWith("java.open.")) {
            resolveOpenRequest(request.replaceFirst("java.open.", ""));
        } else if (request.startsWith("java.searchFilter.")) {
            resolveSearchFilter(request.replaceFirst("java.searchFilter.", ""));
        } else if (request.startsWith("java.run.")) {
            resolveRunRequest(request.replaceFirst("java.run.", ""));
        } else if (request.equals("init.settings.modules")) {
            resolveInitRequest("settings.modules");
        } else if (request.startsWith("sync.library.module.nexus-minecraft-module_java.")) {
            resolveSyncRequest(request.replaceFirst("sync.library.module.nexus-minecraft-module_java.", ""));
        } else if (request.startsWith("sync.button.library.menu.group.mje-instances.")) {
            request = request.replaceFirst("sync.button.library.menu.group.mje-instances.", "");
            resolveButtonRequest("view." + request);
        } else if (request.equals("refresh.library")) {
            JavaStorage.reloadLocalZyndex();
            frame.getBrowser().reload();
        } else if (request.equals("init.library")) {
            frame.executeJavaScript("addModuleToList('Minecraft: Java Edition','" + module.getId() + "_java" + "');");
        } else if (request.equals("init.discover")) {
            frame.executeJavaScript("addModuleToList('Minecraft: Java Edition','" + module.getId() + "_java" + "');");
        } else if (request.startsWith("sync.language.")) {
            ApplicationStorage.language = request.replaceFirst("sync.language.", "");
            JavaStorage.init(module.getId());
        } else if (request.startsWith("sync.settings.")) {
            syncSettings(request.replaceFirst("sync.settings.", ""));
        }
    }

    private void syncSettings(String request) {
        if (request.equals("init")) {
            String group = "addSettingsGroup(\"Minecraft: Java Edition\",\"global\",\"global-mje\");";
            String ram = "addValueToGroup('Memory (RAM)','global-mje','mje-global-memory','java.settings.global.memory','" + JavaStorage.map.getInteger("settings.global.memory") + "MB');";
            String minimize = "addToggleToGroup('Minimize app on game start','global-mje','mje-global-minimize','java.settings.global.minimize'," + JavaStorage.map.getBoolean("settings.global.minimizeApp") + ");";
            frame.executeJavaScript(group + ram + minimize);
        }
    }

    public void resolveButtonRequest(String request) {
        if (request.startsWith("launch.")) {
            request = request.replaceFirst("launch.", "");
            LocalInstance instance = JavaStorage.getLocalZyndex().getLocalInstancesById().get(request);
            new InstanceLauncher(instance, module.getAuthenticator(authKey).getAuthInfos(), frame).launch();
        } else if (request.startsWith("view.")) {
            request = request.replaceFirst("view.", "");
            frame.executeJavaScript("showView(\"" + request + "\");");
        }
    }

    public void resolveInitRequest(String request) {
        if (request.equals("library")) {
            if (module.getAuthState() != MinecraftJavaAddon.AuthState.LOGGED_IN) {
                frame.openCustomPage("Minecraft: Java Edition Authentication...", "mje-authentication", JavaStorage.getUrlBase() + "mje-login.html");
                return;
            }
            String name = module.getAuthenticator(authKey).getAuthInfos().getUsername();
            frame.executeJavaScript("setMenuPanel(\"https://cravatar.eu/helmhead/" + name + "/64.png\",\"" + name + "\",\"Profile options\",true);");

            frame.executeJavaScript("addAction('" + JavaStorage.Strings.addInstance + "','bx bx-plus','connector(\\'java.init.instances.creator\\');','mje-add-instance'); addAction('" + JavaStorage.Strings.refreshInstances + "','bx bx-refresh',\"connector('refresh.library');\",'mje-refresh-instances'); addGroup('" + JavaStorage.Strings.instances + "','mje-instances');");
            frame.executeJavaScript("document.getElementById(\"select-game-module\").value = 'nexus-minecraft-module_java';");

            List<LocalInstance> instances = JavaStorage.getLocalZyndex().getLocalInstances();
            instances.sort(Comparator.comparing(Instance::getName));

            for (LocalInstance instance : instances) {
                try {
                    String title = instance.getName().replace("\"", "''");
                    String id = instance.getId().replace("\"", "");
                    String image = "";
                    if (instance.getIconUrl() != null) {
                        image = instance.getIconUrl().replace("\"", "'");
                    }
                    frame.executeJavaScript("addGroupEntry(\"mje-instances\",\"" + title + "\",\"" + id + "\",\"" + image + "\");");
                } catch (Exception e) {
                    NexusApplication.getLogger().err("[Minecraft] Couldn't index instance: " + e.getMessage());
                }
            }

            if (JavaStorage.getLastInstance() != null) {
                if(JavaStorage.getLocalZyndex().getLocalInstancesById().containsKey(JavaStorage.getLastInstance())) {
                    frame.executeJavaScript("showView(\"" + JavaStorage.getLastInstance() + "\");");
                }
            }
        } else if (request.startsWith("details.")) {
            request = request.replaceFirst("details.", "");
            if (request.startsWith("instance.")) {
                request = URLDecoder.decode(request.replaceFirst("instance.", ""), StandardCharsets.UTF_8);
                ReadableZynstance instance = new ReadableZynstance(request);
                frame.executeJavaScript("enableOverlay(\"" + FrameConnector.initDetails(instance.getName(), instance.getId(), "Minecraft: Java Edition instance", instance.getVersion(), instance.getDescription(), instance.getAuthor(), instance.isHidden(), "No tags", instance.getDescription(), "No changelogs", "No version history", "", "", instance.getBackgroundUrl(), instance.getIconUrl(), instance.getLogoUrl(), instance.getThumbnailUrl()).replace("%plus%", "+") + "\");");
            }
        } else if (request.startsWith("auth")) {
            frame.executeJavaScript("document.getElementById('library-button').classList.add('highlighted');");
            if (module.getAuthState() != MinecraftJavaAddon.AuthState.LOGGED_IN && module.getAuthState() != MinecraftJavaAddon.AuthState.LOGGING_IN) {
                frame.openCustomPage("Minecraft: Java Edition Login", "mje-authentication", JavaStorage.getUrlBase() + "mje-login.html?enable=true");
            } else if (module.getAuthState().equals(MinecraftJavaAddon.AuthState.LOGGED_IN)) {
                resolveSyncRequest("library");
            }
        } else if (request.equals("zyndex")) {
            JavaStorage.reloadLocalZyndex();
        } else if (request.equals("mje-settings")) {
            String settings = "file://" + JavaStorage.getUrlBase().replace("\\", "/") + "mje-settings.html?n=" + module.getName() + "&v=" + module.getVersion() + "&a=" + module.getAuthors();
            frame.executeJavaScript("setContent('settings-custom','minecraft.java-edition','" + settings + "');");
        } else if (request.equals("settings.modules")) {
            frame.executeJavaScript("addGroup(\"Minecraft\",\"mc-general\"); addModuleSetting('bx bx-cube',\"" + JavaStorage.Strings.aboutMinecraftModule + "\",'java.init.mje-settings','minecraft-about',false,'mc-general');");
        } else if (request.startsWith("library.")) {
            request = request.replaceFirst("library.", "");
            if (request.equals("select")) {
                frame.getBrowser().loadURL(ApplicationStorage.urlBase + ApplicationStorage.language + "/library.html?moduleId=-1");
            }
        } else if (request.startsWith("instances.")) {
            request = request.replaceFirst("instances.", "");
            if (request.equals("creator")) {
                frame.openCustomPage(JavaStorage.Strings.library + " - Minecraft: Java Edition", "mje-instance-creator", "file://" + JavaStorage.getUrlBase() + "mje-creator.html");
            }
        }
    }

    public void resolveSyncRequest(String request) {
        if (request.startsWith("library.")) {
            request = request.replaceFirst("library.", "");
            if (request.equals("add")) {
                frame.getBrowser().loadURL(ApplicationStorage.urlBase + ApplicationStorage.language + "/library.html?moduleId=-1");
            } else {
                frame.getBrowser().loadURL(ApplicationStorage.urlBase + ApplicationStorage.language + "/library.html?moduleId=" + request);
            }
        } else if (request.startsWith("view-settings.")) {
            frame.executeJavaScript("document.getElementById('mje-cog').onclick = function() { location.reload(); };");
            showInstanceSettings(JavaStorage.getLocalZyndex().getLocalInstancesById().get(request.replaceFirst("view-settings.", "")));
        } else if (request.startsWith("view.")) {
            showInstance(JavaStorage.getLocalZyndex().getLocalInstancesById().get(request.replaceFirst("view.", "")));
        } else if (request.equals("zyndex")) {
            JavaStorage.reloadLocalZyndex();
        } else if (request.equals("search")) {
            frame.executeJavaScript("location.href='discover.html?l=search';");
        } else if (request.equals("mje-settings")) {
            frame.executeJavaScript("setTitle('Minecraft: Java Edition'); highlight(document.getElementById('minecraft-about'));");
        } else if (request.equals("library")) {
            frame.getBrowser().loadURL(ApplicationStorage.urlBase + ApplicationStorage.language + "/library.html");
        } else if (request.startsWith("discoverHover.")) {
            request = request.replaceFirst("discoverHover.", "");
            if (request.equals("on")) {
                frame.executeJavaScript("if(!document.getElementById('discover-button').classList.contains('teased')) { document.getElementById('discover-button').classList.add('teased'); }");
            } else {
                frame.executeJavaScript("if(document.getElementById('discover-button').classList.contains('teased')) { document.getElementById('discover-button').classList.remove('teased'); }");
            }
        } else if (request.equals("creator")) {
            frame.executeJavaScript("document.getElementById('library-button').classList.add('highlighted'); document.getElementById('library-button').onclick = '';");
        } else if (request.equals("home")) {
            frame.getBrowser().loadURL(ApplicationStorage.urlBase + ApplicationStorage.language + "/start.html");
        } else if (request.equals("discover")) {
            frame.getBrowser().loadURL(ApplicationStorage.urlBase + ApplicationStorage.language + "/discover.html");
        } else if (request.equals("settings")) {
            frame.getBrowser().loadURL(ApplicationStorage.urlBase + ApplicationStorage.language + "/settings.html");
        } else if (request.startsWith("instances.")) {
            resolveInstanceSync(request.replaceFirst("instances.", ""));
        } else if (request.equals("instances")) {

        }
    }

    private void showInstance(LocalInstance instance) {
        String icon = "";
        String logo = "";
        String background = "";
        if(instance.getIconUrl()!=null) {
            icon = instance.getIconUrl();
        }
        if(instance.getLogoUrl()!=null) {
            logo = instance.getLogoUrl();
        }
        if(instance.getBackgroundUrl()!=null) {
            background = instance.getBackgroundUrl();
        }
        String command = "document.querySelector('.cnt').style.backgroundImage = \"url('"+background+"')\"; setOverlayContent(\"<div id='library-overlay-loader'><h3>Loading... <i class='bx bx-loader-alt bx-spin' ></i></h3></div>\"); setViewImage('"+logo+"'); setTitle(\""+icon+"\",\""+instance.getName().replace("\"","''")+"\",\"<h3 id='mje-cog'><i class='bx bxs-cog'></i></h3>\"); setLaunch('LAUNCH','bx bx-rocket','active','java.button.launch."+instance.getId()+"'); enableLaunch(); setViewDescription(\""+instance.getSummary().replace("\"","''")+"\"); document.getElementById('mje-cog').onclick = function() { toggleOverlay('mje-cog'); connector('java.sync.view-settings."+instance.getId()+"'); };";

        frame.executeJavaScript(command);
        JavaStorage.getConfig().set("settings.values.last.instance",instance.getId());
    }

    private void showInstanceSettings(LocalInstance instance) {
        String[] modList = instance.getModList();
        String modLoader = instance.getModloader().toLowerCase();
        String minecraftVersion = instance.getMinecraftVersion();
        String uuid = UUID.randomUUID().toString();
        String id = instance.getId();
        String settingsBase = "<div class='option-group'><h4 class='option'>Game type and version</h4>%</div>";
        String settingsGameType = "<h3 class='option'>Game type (modloader) <label><select id='"+uuid+"-type' onchange='connector(`java.settings."+id+".type."+uuid+".`+this.value);'><option value='vanilla'>Vanilla</option><option value='experimental'>Experimental</option><option value='fabric'>Fabric</option><option value='forge'>Forge</option><option value='neoforge'>NeoForge</option><option value='quilt'>Quilt</option></select></label></h3>";
        ArrayList<String> gameVersions = new ArrayList<>();
        try {
            gameVersions = switch (modLoader) {
                case "fabric" -> Verget.getFabricGameVersions(true);
                case "forge" -> Verget.getForgeGameVersions();
                case "neoforge" -> Verget.getNeoForgeGameVersions();
                case "quilt" -> Verget.getQuiltGameVersions(true);
                case "experimental" -> Verget.getMinecraftVersions(MinecraftVerget.Filter.EXPERIMENTAL);
                case "vanilla" -> Verget.getMinecraftVersions(MinecraftVerget.Filter.RELEASES);
                default -> Verget.getMinecraftVersions(MinecraftVerget.Filter.BOTH);
            };
        } catch (Exception e) {
            NexusApplication.getLogger().err("[Minecraft] (Connector) Couldn't fetch available versions for minecraft: "+e.getMessage());
        }
        StringBuilder settingsGameVersions = new StringBuilder();
        for(String version:gameVersions) {
            settingsGameVersions.append("<option value='").append(version).append("'>").append(version).append("</option>");
        }
        String settingsGameVersion = "<h3 class='option'>Game version <label><select id='"+uuid+"-game-version' onchange='connector(`java.settings."+id+".game-version."+uuid+".`+this.value);'>"+settingsGameVersions+"</select></label></h3>";

        ArrayList<String> loaderVersions = new ArrayList<>();
        try {
            loaderVersions = switch (modLoader) {
                case "fabric" -> Verget.getFabricVersions(true, minecraftVersion);
                case "forge" -> Verget.getForgeVersions(minecraftVersion);
                case "neoforge" -> Verget.getNeoForgeVersions(minecraftVersion);
                case "quilt" -> Verget.getQuiltVersions(minecraftVersion);
                default -> new ArrayList<>();
            };
        } catch (Exception e) {
            NexusApplication.getLogger().err("[Minecraft] (Connector) Couldn't fetch available versions for "+modLoader+": "+e.getMessage());
        }
        StringBuilder settingsLoaderVersions = new StringBuilder();
        for(String version : loaderVersions) {
            settingsLoaderVersions.append("<option value='").append(version).append("'>").append(version).append("</option>");
        }

        String settingsGameLoaderVersion = "<h3 class='option'>Modloader version <label><select id='"+uuid+"-loader-version' onchange='connector(`java.settings."+id+".loader-version."+uuid+".`+this.value);'>"+settingsLoaderVersions+"</select></label></h3>";
        String settingsGame = settingsGameType+settingsGameVersion+settingsGameLoaderVersion;

        String settingsJava = "<div class='option-group'><h4 class='option'>Java settings</h4>%</div>";
        String settingsJVMMemory = "<h3 class='option'>Memory (RAM) <span class='value-option'><span id='"+id+"-memory'><i class='bx bx-loader-alt bx-spin' ></i> Loading...</span> <i class='bx bxs-pencil' onclick='connector(`java.settings."+id+".memory`);'></i></span></h3>";
        String settingsJVMArgs = "<h3 class='option input-list' id='java.settings."+id+".jvm-arguments'>JVM Arguments <span class='input-list-field'><span class='list-input-content'></span><label><input placeholder='Type here...' class='list' type='text'></label></span></h3>";
        settingsJava = settingsJava.replace("%",settingsJVMMemory+settingsJVMArgs);

        String settings = settingsBase.replace("%",settingsGame)+settingsJava;


        String managementBase = "<div class='option-group'><h4 class='option'>Instance information</h4>%</div>";
        String managementName = "<h3 class='option'>Name <label><input class='text' id='"+uuid+"-name' type='text' value=\\\""+instance.getName().replace("\"","''")+"\\\"></label></h3>";
        String managementVersion = "<h3 class='option'>Version <label><input class='text' id='"+uuid+"-version' type='text' value=\\\""+instance.getVersion().replace("\"","''")+"\\\"></label></h3>";
        String managementDescription = "<h3 class='option'>Summary<br><label><textarea id='"+uuid+"-summary' onchange=\\\"log('Test');\\\">"+instance.getSummary().replace("\"","''")+"</textarea></label></h3>";

        String managementAppearance = "<div class='option-group'><h4 class='option'>Instance appearance</h4>%</div>";
        String appearanceIcon = "<h3 class='option'>Icon image</h3>";
        String appearanceLogo = "<h3 class='option'>Logo image</h3>";
        String appearanceBackground = "<h3 class='option'>Background image</h3>";

        String management = managementBase.replace("%",managementName+managementVersion+managementDescription)+managementAppearance.replace("%",appearanceIcon+appearanceLogo+appearanceBackground);

        /*StringBuilder contents = new StringBuilder("<div class='option-group'><h4 class='option'>Mods</h4>");
        for(String mod : modList) {
            contents.append("<h3 class='option'>").append(mod).append("</h3>");
        }
        contents.append("</div>");*/

        String content = "<div class='overlay-group' id='"+uuid+"-settings-content'>"+settings+"</div> <div class='overlay-group' id='"+uuid+"-management-content'>"+management+"</div> <div class='overlay-group' id='"+uuid+"-content-content'>"+/*contents+*/"</div>";
        String command = "setOverlayContent(\"<div class='tabs' id='"+uuid+"-tabs'></div> "+content+"\"); addTab('"+uuid+"-tabs','"+uuid+"-settings','Settings',''); addTab('"+uuid+"-tabs','"+uuid+"-management','Management',''); "/*addTab('"+uuid+"-tabs','"+uuid+"-content','Content','');*/ +"switchTab('"+uuid+"-tabs','"+uuid+"-settings');";
        frame.executeJavaScript(command); System.gc();

        String loaderVersion = switch(modLoader) {
            case "fabric" -> instance.getFabricVersion();
            case "forge" -> instance.getForgeVersion();
            case "neoforge" -> instance.getNeoForgeVersion();
            case "quilt" -> instance.getQuiltVersion();
            default -> "";
        };

        String syncSettingsType = "document.getElementById('"+uuid+"-type').value = '"+modLoader.toLowerCase()+"'; document.getElementById('"+uuid+"-game-version').value = '"+minecraftVersion+"'; document.getElementById('"+uuid+"-loader-version').value = '"+loaderVersion+"';";
        String syncSettingsJava = "document.getElementById('"+id+"-memory').innerText = '"+instance.getMemory()+"MB'; initializeListInput('java.settings."+id+".jvm-arguments'); ";
        try {
            StringBuilder args = new StringBuilder();
            for (String arg : (ArrayList<String>) instance.getSettings().get("settings.java.jvm-arguments")) {
                args.append("listInputs.get('java.settings.").append(id).append(".jvm-arguments').push('").append(arg).append("'); ");
            }
            args.append("syncListInput('java.settings.").append(id).append(".jvm-arguments'); ");
            syncSettingsJava += args.toString();
        } catch (Exception ex) {
            NexusApplication.getLogger().err("[Minecraft] (CONNECTOR) Couldn't resolve jvm arguments for "+id+": "+ex.getMessage());
        }
        String listeners = "document.getElementById('"+uuid+"-name').addEventListener('input', function() { clearTimeout(this.timeout); this.timeout = setTimeout(() => { connector('java.settings."+id+".name.'+this.value); }, 250); }); document.getElementById('"+uuid+"-version').addEventListener('input', function() { clearTimeout(this.timeout); this.timeout = setTimeout(() => { connector('java.settings."+id+".version.'+this.value); }, 250); }); document.getElementById('"+uuid+"-summary').addEventListener('input', function() { clearTimeout(this.timeout); this.timeout = setTimeout(() => { connector('java.settings."+id+".summary.'+this.value); }, 250); });";
        frame.executeJavaScript(syncSettingsType+syncSettingsJava+listeners); System.gc();
    }

    private void resolveInstanceSync(String request) {
        if (request.equals("overview")) {
            if (ApplicationStorage.language.equals("de")) {
                frame.executeJavaScript("document.getElementById('mje-add-instance-button').innerText = 'Instanz hinzufügen';");
                frame.executeJavaScript("document.getElementById('mje-menu-title').innerText = 'Instanzen';");
                frame.executeJavaScript("setTitle('Übersicht');");
            }
        }
    }

    public void resolveOpenRequest(String request) {

    }

    public void resolveRunRequest(String request) {

    }

    public void resolveAuthRequest(String request) {
        if (request.equals("login")) {
            if (module.getAuthState() != MinecraftJavaAddon.AuthState.LOGGED_IN) {
                module.getAuthenticator(authKey).login();
            }
        } else if (request.equals("logout")) {
            ApplicationStorage.disableDriveAccess();
            if (module.getAuthState() == MinecraftJavaAddon.AuthState.LOGGED_IN) {
                frame.executeJavaScript("mjeLogout('" + JavaStorage.Strings.notLoggedIn + "','" + JavaStorage.Strings.login + "');");
                JavaStorage.map.delete("auth.username");
                JavaStorage.map.delete("auth.uuid");
                module.setAuthState(MinecraftJavaAddon.AuthState.LOGGED_OUT);
                module.createNewAuthenticator();
            }
        }
    }

    private void resolveSearchFilter(String filterRequest) {
        String request = filterRequest.replaceFirst("java.searchFilter.", "");
        if (request.startsWith("source.")) {
            JavaStorage.setSearchSource(request.replaceFirst("source.", ""));
            frame.getBrowser().loadURL(ApplicationStorage.urlBase + ApplicationStorage.language + "/discover.html?l=search&moduleId=" + module.getId() + "_java");
        }
    }

    private void resolveSearchRequest(String request) {

        frame.executeJavaScript("addFilterGroup('mje-source','Source'); addSelectFilter('source','mje-source','java.searchFilter.source',\"<option value='official'>Zyneon NEX</option><option value='modrinth'>Modrinth</option><option value='curseforge'>CurseForge</option>\",false); document.getElementById('mje-source-source-select').value = \"" + JavaStorage.getSearchSource() + "\"; deactivateMenu('menu',true);");

        String query = "";
        if (request.startsWith(".")) {
            query = request.replaceFirst(".", "");
        }

        switch (JavaStorage.getSearchSource()) {
            case "official" -> ZyndexIntegration.searchModpacks(query, frame);
            case "curseforge" -> CurseForgeIntegration.searchModpacks(query, frame);
            case "modrinth" -> ModrinthIntegration.searchModpacks(query, frame);
        }
    }

    private void resolveSettingsRequest(String request) {
        if (request.startsWith("global.")) {
            request = request.replaceFirst("global.", "");
            if (request.equals("memory")) {
                OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
                long c = 1024L * 1024L;
                long max = os.getTotalMemorySize() / c;
                String url = ApplicationStorage.urlBase + ApplicationStorage.language + "/mje-memory.html?min=0&max=" + max + "&id=global&value=" + JavaStorage.map.getInteger("settings.global.memory");
                url = url.replace("\\", "/");
                frame.executeJavaScript("enableOverlay('" + url + "');");
            } else if (request.startsWith("memory.")) {
                try {
                    int i = Integer.parseInt(request.replace("memory.", ""));
                    JavaStorage.getConfig().set("settings.global.memory", i);
                    JavaStorage.map.setInteger("settings.global.memory", i);
                } catch (Exception ignore) {}
                frame.getBrowser().loadURL(ApplicationStorage.urlBase + ApplicationStorage.language + "/settings.html?t=global");
            } else if (request.startsWith("minimize.")) {
                boolean minimize = Boolean.parseBoolean(request.replace("minimize.", ""));
                JavaStorage.getConfig().set("settings.global.minimizeApp", minimize);
                JavaStorage.map.setBoolean("settings.global.minimizeApp", minimize);
            }
        } else {
            String[] r = request.split("\\.",2);
            String id = r[0]; request = r[1];
            if(JavaStorage.getLocalZyndex().getLocalInstancesById().containsKey(id)) {
                LocalInstance instance = JavaStorage.getLocalZyndex().getLocalInstancesById().get(id);
                if(request.startsWith("type.")) {
                    r = request.split("\\.",3);
                    String type = r[2];
                    String uuid = r[1];

                    ArrayList<String> gameVersions;
                    String gameVersion;
                    try {
                        gameVersions = switch (type) {
                            case "fabric" -> Verget.getFabricGameVersions(true);
                            case "forge" -> Verget.getForgeGameVersions();
                            case "neoforge" -> Verget.getNeoForgeGameVersions();
                            case "quilt" -> Verget.getQuiltGameVersions(true);
                            case "vanilla" -> Verget.getMinecraftVersions(MinecraftVerget.Filter.RELEASES);
                            case "experimental" -> Verget.getMinecraftVersions(MinecraftVerget.Filter.EXPERIMENTAL);
                            default -> Verget.getMinecraftVersions(MinecraftVerget.Filter.BOTH);
                        };
                        gameVersion = gameVersions.getFirst();
                    } catch (Exception e) {
                        NexusApplication.getLogger().err("[Minecraft] (Connector) Couldn't fetch available game versions for "+type+": "+e.getMessage());
                        gameVersions = new ArrayList<>();
                        gameVersion = null;
                    }
                    StringBuilder settingsGameVersions = new StringBuilder();
                    for(String version:gameVersions) {
                        settingsGameVersions.append("<option value='").append(version).append("'>").append(version).append("</option>");
                    }

                    ArrayList<String> loaderVersions;
                    String loaderVersion;
                    try {
                        loaderVersions = switch (type) {
                            case "fabric" -> Verget.getFabricVersions(true,gameVersion);
                            case "forge" -> Verget.getForgeVersions(gameVersion);
                            case "neoforge" -> Verget.getNeoForgeVersions(gameVersion);
                            case "quilt" -> Verget.getQuiltVersions(gameVersion);
                            default -> new ArrayList<>();
                        };
                        if(loaderVersions.isEmpty()) {
                            loaderVersion = null;
                        } else {
                            loaderVersion = loaderVersions.getFirst();
                        }
                    } catch (Exception e) {
                        NexusApplication.getLogger().err("[Minecraft] (Connector) Couldn't fetch available game versions for "+type+": "+e.getMessage());
                        loaderVersions = new ArrayList<>();
                        loaderVersion = null;
                    }
                    StringBuilder settingsLoaderVersions = new StringBuilder();
                    for(String version:loaderVersions) {
                        settingsLoaderVersions.append("<option value='").append(version).append("'>").append(version).append("</option>");
                    }

                    String command = "document.getElementById('"+uuid+"-game-version').innerHTML = \""+settingsGameVersions+"\"; document.getElementById('"+uuid+"-loader-version').innerHTML = \""+settingsLoaderVersions+"\";";
                    frame.executeJavaScript(command);

                    if(gameVersion!=null) {
                        instance.setModloader(type.toLowerCase(), loaderVersion);
                        instance.setGameVersion(gameVersion);
                    }
                } else if(request.startsWith("game-version.")) {
                    System.out.println(request);
                    r = request.split("\\.",3);
                    String gameVersion = r[2];
                    String uuid = r[1];
                    String type = instance.getModloader().toLowerCase();

                    ArrayList<String> loaderVersions;
                    String loaderVersion;
                    try {
                        loaderVersions = switch (type) {
                            case "fabric" -> Verget.getFabricVersions(true,gameVersion);
                            case "forge" -> Verget.getForgeVersions(gameVersion);
                            case "neoforge" -> Verget.getNeoForgeVersions(gameVersion);
                            case "quilt" -> Verget.getQuiltVersions(gameVersion);
                            default -> new ArrayList<>();
                        };
                        if(loaderVersions.isEmpty()) {
                            loaderVersion = null;
                        } else {
                            loaderVersion = loaderVersions.getFirst();
                        }
                    } catch (Exception e) {
                        NexusApplication.getLogger().err("[Minecraft] (Connector) Couldn't fetch available game versions for "+type+": "+e.getMessage());
                        loaderVersions = new ArrayList<>();
                        loaderVersion = null;
                    }
                    StringBuilder settingsLoaderVersions = new StringBuilder();
                    for(String version:loaderVersions) {
                        settingsLoaderVersions.append("<option value='").append(version).append("'>").append(version).append("</option>");
                    }

                    String command = "document.getElementById('"+uuid+"-loader-version').innerHTML = \""+settingsLoaderVersions+"\";";
                    frame.executeJavaScript(command);

                    instance.setModloader(type.toLowerCase(), loaderVersion);
                    instance.setGameVersion(gameVersion);
                } else if(request.startsWith("loader-version.")) {
                    String version = request.split("\\.",3)[2];
                    instance.setModloader(instance.getModloader(),version);
                    String command = "";
                    frame.executeJavaScript(command);
                } else if(request.startsWith("jvm-arguments.add.")) {
                    ArrayList<String> args = (ArrayList<String>)instance.getSettings().get("settings.java.jvm-arguments");
                    String arg = request.replaceFirst("jvm-arguments.add.","");
                    if(!args.contains(arg)) {
                        args.add(arg);
                    }
                    instance.getSettings().set("settings.java.jvm-arguments",args);
                } else if(request.startsWith("jvm-arguments.remove.")) {
                    ArrayList<String> args = (ArrayList<String>)instance.getSettings().get("settings.java.jvm-arguments");
                    args.remove(request.replaceFirst("jvm-arguments.remove.",""));
                    instance.getSettings().set("settings.java.jvm-arguments",args);
                } else if(request.equals("memory")) {
                    id = instance.getId();
                    OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
                    long c = 1024L * 1024L;
                    long max = os.getTotalMemorySize() / c;
                    String url = ApplicationStorage.urlBase + ApplicationStorage.language + "/mje-memory.html?min=0&max=" + max + "&id="+id+"&value=" + instance.getMemory();
                    url = url.replace("\\", "/");
                    frame.executeJavaScript("enableOverlay('" + url + "');");
                } else if(request.startsWith("memory.")) {
                    instance.getSettings().set("settings.java.memory",Integer.parseInt(request.replaceFirst("memory.","")));
                    frame.executeJavaScript("disableOverlay(); document.getElementById('"+id+"-memory').innerText = '"+instance.getMemory()+"MB';");
                } else if(request.startsWith("name.")) {
                    instance.setName(request.replaceFirst("name.",""));
                } else if(request.startsWith("version.")) {
                    instance.setVersion(request.replaceFirst("version.",""));
                } else if(request.startsWith("summary.")) {
                    instance.setSummary(request.replaceFirst("summary.",""));
                }
            }
        }
    }
}