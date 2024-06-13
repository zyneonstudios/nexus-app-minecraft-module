package com.zyneonstudios.application.minecraft.java;

import com.zyneonstudios.application.MinecraftJavaAddon;
import com.zyneonstudios.application.frame.web.ApplicationFrame;
import com.zyneonstudios.application.main.ApplicationConfig;
import com.zyneonstudios.application.main.NexusApplication;
import com.zyneonstudios.application.modules.ModuleConnector;
import com.zyneonstudios.nexus.instance.Instance;
import com.zyneonstudios.nexus.instance.ReadableZynstance;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
        } else if(request.startsWith("java.sync.")) {
            resolveSyncRequest(request.replaceFirst("java.sync.", ""));
        } else if(request.startsWith("java.open.")) {
            resolveOpenRequest(request.replaceFirst("java.open.", ""));
        } else if(request.startsWith("java.run.")) {
            resolveRunRequest(request.replaceFirst("java.run.", ""));
        } else if(request.equals("init.settings.modules")) {
            resolveInitRequest("settings.modules");
        } else if(request.equals("init.library")) {
            frame.executeJavaScript("addModuleToList('Minecraft: Java Edition','" + module.getId()+"_java" + "');");
        } else if(request.startsWith("sync.language.")) {
            ApplicationConfig.language = request.replaceFirst("sync.language.","");
            JavaStorage.init(module.getId());
        }
    }

    public void resolveInitRequest(String request) {
        if(request.equals("library")) {
            JavaStorage.reloadLocalZyndex();
            frame.executeJavaScript("addAction('"+JavaStorage.Strings.addInstance+"','bx bx-plus','connector(\\'java.init.instances.creator\\');','mje-add-instance'); addAction('"+JavaStorage.Strings.refreshInstances+"','bx bx-refresh','location.reload();','mje-refresh-instances'); addGroup('"+JavaStorage.Strings.instances+"','mje-instances');");

            List<ReadableZynstance> instances = JavaStorage.getLocalZyndex().getInstances();
            instances.sort(Comparator.comparing(Instance::getName));

            for (ReadableZynstance instance : instances) {
                try {
                    String title = instance.getName().replace("\"", "''");
                    String id = instance.getId().replace("\"", "");
                    String image = "";
                    if(instance.getIconUrl()!=null) {
                        image = instance.getIconUrl().replace("\"", "'");
                    } else {
                        String path = JavaStorage.getLocalZyndex().getPath(instance);
                        if(!path.endsWith("/")) {
                            path = path + "/meta/icon.png";
                        } else {
                            path = path + "meta/icon.png";
                        }
                        if(new File(path).exists()) {
                            image = "file://"+path;
                        }
                    }
                    frame.executeJavaScript("addGroupEntry(\"mje-instances\",\"" + title + "\",\"" + id + "\",\"" + image + "\");");
                } catch (Exception e) {
                    NexusApplication.getLogger().error("[Minecraft] Couldn't index instance: "+e.getMessage());
                }
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
            } else if(request.equals("auth")) {
                if(module.getAuthenticator().isLoggedIn()) {
                    frame.executeJavaScript("mjeLogin('"+module.getAuthenticator().getAuthInfos().getUsername()+"','"+module.getAuthenticator().getAuthInfos().getUuid()+"','"+JavaStorage.Strings.logout+"');");
                } else {
                    frame.executeJavaScript("mjeLogout('"+JavaStorage.Strings.notLoggedIn+"','"+JavaStorage.Strings.login+"');");
                }
            } else {
                frame.getBrowser().loadURL(ApplicationConfig.urlBase + ApplicationConfig.language + "/library.html?moduleId=" + request);
            }
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
