package com.zyneonstudios.application.minecraft.java;

import com.zyneonstudios.application.MinecraftJavaAddon;
import com.zyneonstudios.application.frame.web.ApplicationFrame;
import com.zyneonstudios.application.main.ApplicationConfig;
import com.zyneonstudios.application.main.NexusApplication;
import com.zyneonstudios.application.minecraft.java.integrations.zyndex.LocalInstance;
import com.zyneonstudios.application.minecraft.java.launchers.*;
import com.zyneonstudios.application.modules.ModuleConnector;
import com.zyneonstudios.nexus.instance.Instance;

import java.util.Comparator;
import java.util.List;

public class JavaConnector extends ModuleConnector {

    private final MinecraftJavaAddon module;
    private final ApplicationFrame frame;

    public JavaConnector(MinecraftJavaAddon module) {
        super(module);
        this.module = module;
        this.frame = (ApplicationFrame)this.module.getApplication().getFrame();
    }

    @Override @SuppressWarnings("deprecation")
    public void resolveRequest(String request) {
        if(request.startsWith("java.auth.")) {
            resolveAuthRequest(request.replaceFirst("java.auth.", ""));
        } else if(request.equals("sync.library.module.nexus-minecraft-module_java")) {
            resolveInitRequest("library");
        } else if(request.startsWith("java.overlay")) {
            frame.executeJavaScript("enableOverlay('https://www.zyneonstudios.com');");
        } else if(request.startsWith("java.init.")) {
            resolveInitRequest(request.replaceFirst("java.init.", ""));
        } else if(request.startsWith("java.button.")) {
            resolveButtonRequest(request.replaceFirst("java.button.", ""));
        } else if(request.startsWith("java.sync.")) {
            resolveSyncRequest(request.replaceFirst("java.sync.", ""));
        } else if(request.startsWith("java.open.")) {
            resolveOpenRequest(request.replaceFirst("java.open.", ""));
        } else if(request.startsWith("java.run.")) {
            resolveRunRequest(request.replaceFirst("java.run.", ""));
        } else if(request.equals("init.settings.modules")) {
            resolveInitRequest("settings.modules");
        } else if(request.startsWith("sync.library.module.nexus-minecraft-module_java.")) {
            resolveSyncRequest(request.replaceFirst("sync.library.module.nexus-minecraft-module_java.",""));
        } else if(request.startsWith("sync.button.library.menu.group.mje-instances.")) {
            request = request.replaceFirst("sync.button.library.menu.group.mje-instances.","");
            resolveButtonRequest("view."+request);
        } else if(request.equals("init.library")) {
            frame.executeJavaScript("addModuleToList('Minecraft: Java Edition','" + module.getId()+"_java" + "');");
        } else if(request.startsWith("sync.language.")) {
            ApplicationConfig.language = request.replaceFirst("sync.language.","");
            JavaStorage.init(module.getId());
        }
    }

    public void resolveButtonRequest(String request) {
        if(request.startsWith("launch.")) {
            request = request.replaceFirst("launch.", "");
            LocalInstance instance = JavaStorage.getLocalZyndex().getLocalZynstances().get(request);
            if(instance.getModloader().equals("Forge")) {
                new ForgeLauncher(module).launch(instance);
            } else if(instance.getModloader().equals("Fabric")) {
                new FabricLauncher(module).launch(instance);
            } else if(instance.getModloader().equals("Quilt")) {
                new QuiltLauncher(module).launch(instance);
            } else if(instance.getModloader().equals("NeoForge")) {
                new NeoForgeLauncher(module).launch(instance);
            } else {
                new VanillaLauncher(module).launch(instance);
            }
        } else if(request.startsWith("view.")) {
            request = request.replaceFirst("view.", "");
            frame.executeJavaScript("showView(\""+request+"\");");
        }
    }

    public void resolveInitRequest(String request) {
        if(request.equals("library")) {
            if(!module.getAuthenticator().isLoggedIn()) {
                frame.openCustomPage("Minecraft: Java Edition Authentication...","mje-authentication",JavaStorage.getUrlBase()+"mje-login.html");
                return;
            }
            String name = module.getAuthenticator().getAuthInfos().getUsername();
            frame.executeJavaScript("setMenuPanel(\"https://cravatar.eu/helmhead/"+name+"/64.png\",\""+name+"\",\"Profile options\",true);");

            frame.executeJavaScript("addAction('"+JavaStorage.Strings.addInstance+"','bx bx-plus','connector(\\'java.init.instances.creator\\');','mje-add-instance'); addAction('"+JavaStorage.Strings.refreshInstances+"','bx bx-refresh','location.reload();','mje-refresh-instances'); addGroup('"+JavaStorage.Strings.instances+"','mje-instances');");
            frame.executeJavaScript("document.getElementById(\"select-game-module\").value = 'nexus-minecraft-module_java';");


            JavaStorage.reloadLocalZyndex();
            List<LocalInstance> instances = JavaStorage.getLocalZyndex().getLocalInstances();
            instances.sort(Comparator.comparing(Instance::getName));

            for (LocalInstance instance : instances) {
                try {
                    String title = instance.getName().replace("\"", "''");
                    String id = instance.getId().replace("\"", "");
                    String image = "";
                    if(instance.getIconUrl()!=null) {
                        image = instance.getIconUrl().replace("\"", "'");
                    }
                    frame.executeJavaScript("addGroupEntry(\"mje-instances\",\"" + title + "\",\"" + id + "\",\"" + image + "\");");
                } catch (Exception e) {
                    NexusApplication.getLogger().error("[Minecraft] Couldn't index instance: "+e.getMessage());
                }
            }

            if(JavaStorage.getLastInstance()!=null) {
                frame.executeJavaScript("showView(\""+JavaStorage.getLastInstance()+"\");");
            }
        } else if(request.startsWith("auth")) {
            frame.executeJavaScript("document.getElementById('library-button').classList.add('highlighted');");
            if(module.getAuthState().equals(MinecraftJavaAddon.AuthState.LOGGED_OUT)) {
                frame.openCustomPage("Minecraft: Java Edition Login","mje-authentication",JavaStorage.getUrlBase()+"mje-login.html?enable=true");
            } else if(module.getAuthState().equals(MinecraftJavaAddon.AuthState.LOGGED_IN)) {
                resolveSyncRequest("library");
            }
        } else if(request.equals("zyndex")) {
            JavaStorage.reloadLocalZyndex();
        } else if(request.equals("mje-settings")) {
            String settings = "file://"+JavaStorage.getUrlBase().replace("\\","/")+"mje-settings.html";
            frame.executeJavaScript("setContent('settings-custom','minecraft.java-edition','"+settings+"');");
        } else if(request.equals("settings.modules")) {
            frame.executeJavaScript("addModuleSetting('bx bx-cube','Minecraft: Java Edition','java.init.mje-settings','minecraft.java-edition',false);");
        } else if(request.startsWith("instances.")) {
            request = request.replaceFirst("instances.","");
            if(request.equals("creator")) {
                frame.openCustomPage(JavaStorage.Strings.library+" - Minecraft: Java Edition","mje-instance-creator","file://"+JavaStorage.getUrlBase()+"mje-creator.html");
            }
        }
    }

    public void resolveSyncRequest(String request) {
        if(request.startsWith("library.")) {
            request = request.replaceFirst("library.", "");
            if (request.equals("add")) {
                frame.getBrowser().loadURL(ApplicationConfig.urlBase + ApplicationConfig.language + "/library.html?moduleId=-1");
            } else {
                frame.getBrowser().loadURL(ApplicationConfig.urlBase + ApplicationConfig.language + "/library.html?moduleId=" + request);
            }
        } else if(request.startsWith("view.")) {
            request = request.replaceFirst("view.","");
            JavaStorage.getConfig().set("settings.values.last.instance",request);
            LocalInstance instance = JavaStorage.getLocalZyndex().getLocalZynstances().get(request);
            String img = "";

            if(instance.getIconUrl()!=null) {
                img = instance.getIconUrl();
            }
            if(instance.getBackgroundUrl()!=null) {
                frame.executeJavaScript("document.querySelector(\".cnt\").style.backgroundImage = \"url(\\\""+instance.getBackgroundUrl().replace("\"","'")+"\\\")\";");
            }
            if(instance.getLogoUrl()!=null) {
                frame.executeJavaScript("setViewImage(\""+instance.getLogoUrl()+"\");");
            }

            String description = "No description";
            if(instance.getDescription()!=null) {
                description = instance.getDescription();
            }
            frame.executeJavaScript("setTitle(\""+img+"\",\""+instance.getName()+"\"); setViewDescription(\""+description+"\");");
        } else if(request.equals("zyndex")) {
            JavaStorage.asyncReloadLocalZyndex();
        } else if(request.equals("mje-settings")) {

        } else if(request.equals("library")) {
            frame.getBrowser().loadURL(ApplicationConfig.urlBase + ApplicationConfig.language + "/library.html");
        } else if(request.startsWith("discoverHover.")) {
            request = request.replaceFirst("discoverHover.", "");
            if(request.equals("on")) {
                frame.executeJavaScript("if(!document.getElementById('discover-button').classList.contains('teased')) { document.getElementById('discover-button').classList.add('teased'); }");
            } else {
                frame.executeJavaScript("if(document.getElementById('discover-button').classList.contains('teased')) { document.getElementById('discover-button').classList.remove('teased'); }");
            }
        } else if(request.equals("creator")) {
            frame.executeJavaScript("document.getElementById('library-button').classList.add('highlighted'); document.getElementById('library-button').onclick = '';");
        } else if(request.equals("home")) {
            frame.getBrowser().loadURL(ApplicationConfig.urlBase+ApplicationConfig.language+"/start.html");
        } else if(request.equals("discover")) {
            frame.getBrowser().loadURL(ApplicationConfig.urlBase+ApplicationConfig.language+"/discover.html");
        } else if(request.equals("settings")) {
            frame.getBrowser().loadURL(ApplicationConfig.urlBase + ApplicationConfig.language + "/settings.html");
        } else if(request.startsWith("instances.")) {
            resolveInstanceSync(request.replaceFirst("instances.",""));
        } else if(request.equals("instances")) {

        }
    }

    private void resolveInstanceSync(String request) {
        if(request.equals("overview")) {
            if(ApplicationConfig.language.equals("de")) {
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
        if(request.equals("login")) {
            if(!module.getAuthenticator().isLoggedIn()) {
                module.getAuthenticator().login();
            }
        } else if(request.equals("logout")) {
            if(module.getAuthenticator().isLoggedIn()) {
                frame.executeJavaScript("mjeLogout('"+JavaStorage.Strings.notLoggedIn+"','"+JavaStorage.Strings.login+"');");
                module.setAuthState(MinecraftJavaAddon.AuthState.LOGGED_OUT);
                module.createNewAuthenticator();
            }
        }
    }
}
