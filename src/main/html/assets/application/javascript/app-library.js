function initLibrary() {
    const urlParams = new URLSearchParams(window.location.search);
    if(urlParams.get("moduleId")!==null||localStorage.getItem("settings.lastLibraryModule")!==null) {
        let moduleId;
        if(urlParams.get("moduleId")!==null) {
            moduleId = urlParams.get('moduleId');
        } else {
            moduleId = localStorage.getItem("settings.lastLibraryModule");
        }
        document.getElementById("select-game-module").value = moduleId;
    }
    if(urlParams.get("instanceId")) {

    } else {
        clear();
    }
}

function addModuleToList(title,moduleId) {
    const template = document.getElementById('add-module-option');
    const entry = template.cloneNode(true);
    entry.value = moduleId;
    entry.innerText = title;
    template.parentNode.insertBefore(entry,template);
}

function onModuleChange() {
    const module = document.getElementById('select-game-module').value;
    if(module) {
        if(module!=="-1") {
            localStorage.setItem("settings.lastLibraryModule",module);
            connector("java.sync.library."+module);
        } else {
            connector("java.sync.library.add");
        }
    }
}

function mjePreAuth(title,subtitle) {
    document.getElementById("mjeAuth-username").innerText = title;
    document.getElementById("mjeAuth-action").innerHTML = subtitle;
    document.getElementById("mjeAuth-img").src = "https://cravatar.eu/helmhead/MHF_Question/128.png";
}

function mjeLogin(username,uuid,logout) {
    document.getElementById("mjeAuth-username").innerText = username;
    document.getElementById("mjeAuth-action").innerHTML = "<a onclick=\"connector('java.auth.logout');\">"+logout+"</a>";
    document.getElementById("mjeAuth-img").src = "https://cravatar.eu/helmhead/"+username+"/128.png";
}

function mjeLogout(placeholder,login) {
    document.getElementById("mjeAuth-username").innerText = placeholder;
    document.getElementById("mjeAuth-action").innerHTML = "<a onclick=\"connector('java.auth.login');\">"+login+"</a>";
    document.getElementById("mjeAuth-img").src = "https://cravatar.eu/helmhead/MHF_Question/128.png";
}

function togglePanel() {
    if(showPanel) {
        disablePanel();
    } else {
        enablePanel();
    }
}

function clearPanel() {
    document.getElementById("mje-panel-title").innerText = "";
    document.getElementById("mje-panel-content").innerHTML = "";
}

function setPanel(title,content) {
    clearPanel();
    if(title) {
        document.getElementById("mje-panel-title").innerText = title;
    }
    if(content) {
        document.getElementById("mje-panel-content").innerHTML = content;
    }
}

let showPanel = false;
function enablePanel() {
    const panel = document.getElementById("mje-panel");
    if(!panel.classList.contains("active")) {
        panel.classList.add("active");
        const menu = document.getElementById("mje-menu");
        if(!menu.classList.contains("active")) {
            menu.classList.add("active");
        }
    }
    showPanel = true;
}

function disablePanel() {
    const panel = document.getElementById("mje-panel");
    if(panel.classList.contains("active")) {
        panel.classList.remove("active");
        const menu = document.getElementById("mje-menu");
        if(menu.classList.contains("active")) {
            menu.classList.remove("active");
        }
    }
    showPanel = false;
}

function clearMenu() {
    document.getElementById("mje-menu-title").innerText = "";
    document.getElementById("mje-menu-content").innerHTML = "";
}

function setMenu(title,content) {
    clearMenu();
    if(title) {
        document.getElementById("mje-menu-title").innerText = title;
    }
    if(content) {
        document.getElementById("mje-menu-content").innerHTML = content;
    }
}

function clearTitle() {
    document.getElementById("mje-page-title").innerText = "";
}

function setTitle(title) {
    clearTitle();
    if(title) {
        document.getElementById("mje-page-title").innerText = title;
    }
}

function clear() {
    clearTitle();
    clearPanel();
    disablePanel();
    clearMenu();
    setTitle("Overview");
    setMenu("Instances","<div id='mje-list'><ul><li onclick=\"connector('java.init.instances.creator');\"><i class='bx bx-plus'></i><h5 id='mje-add-instance-button'>Add Instance</h5></li><li id='mje-menu-template'><img alt='mje-instance-icon' src='../assets/application/images/nero.png'><h5>mje-instance-title</h5></li></ul></div>");
    connector("java.sync.instances.overview");
}

function addInstanceToList(title,instanceId,image) {
    const template = document.getElementById('mje-menu-template');
    if(template) {
        const entry = template.cloneNode(true);
        if(instanceId) {
            entry.id = instanceId;
        }
        if(image) {
            entry.querySelector("img").src = image;
        }
        if(title) {
            entry.querySelector("h5").innerText = title;
        }
        template.parentNode.insertBefore(entry,template);
    }
}

function highlightInstance(id) {
    document.getElementById(id).classList.toggle("active");
}