function initHover() {
    const discover = document.getElementById("mje-discover-modpacks");

    discover.addEventListener("mouseenter", () => {
        if(!document.getElementById("mje-discover-modpacks-i").classList.contains("bx-spin")) {
            document.getElementById("mje-discover-modpacks-i").classList.add("bx-spin");
        }
        connector('java.sync.discoverHover.on');
    });

    discover.addEventListener("mouseleave", () => {
        if(document.getElementById("mje-discover-modpacks-i").classList.contains("bx-spin")) {
            document.getElementById("mje-discover-modpacks-i").classList.remove("bx-spin");
        }
        connector('java.sync.discoverHover.off');
    });
}