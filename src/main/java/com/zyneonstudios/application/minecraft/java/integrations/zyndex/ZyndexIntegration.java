package com.zyneonstudios.application.minecraft.java.integrations.zyndex;

import com.zyneonstudios.application.frame.web.ApplicationFrame;
import com.zyneonstudios.nexus.instance.ReadableZynstance;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ZyndexIntegration {


    public static final InstanceSearch search = new InstanceSearch("https://raw.githubusercontent.com/zyneonstudios/nexus-nex/main/zyndex/index.json");;

    public static void searchModpacks(String query, ApplicationFrame frame) {
        frame.executeJavaScript(
                "addFilterGroup('mje-official-categories','Categories');" +
                "addToggleFilter(\"Optimization\",\"mje-official-categories\",\"java.searchFilter.official.category.optimization\",true,true);" +

                "addFilterGroup('mje-official-loaders','Loaders');" +
                "addToggleFilter(\"Forge\",\"mje-official-loaders\",\"java.searchFilter.official.loader.forge\",true,true);" +
                "addToggleFilter(\"Fabric\",\"mje-official-loaders\",\"java.searchFilter.official.loader.fabric\",true,true);" +

                "addFilterGroup('mje-official-minecraft','Minecraft versions');" +
                "addToggleFilter(\"Show all versions\",\"mje-official-minecraft\",\"java.searchFilter.official.minecraft.showAllVersions\",true,true);" +
                "addSelectFilter('minecraftVersions','mje-official-minecraft','java.searchFilter.official.minecraft.versions',\"<option>All</option>\",true);"
        );

        String searchTerm = "";
        if (search.getCachedSearchTerm() != null) {
            searchTerm = search.getCachedSearchTerm();
            if (!searchTerm.isEmpty() && !searchTerm.isBlank()) {
                frame.executeJavaScript("document.getElementById(\"search-bar\").placeholder = \"" + searchTerm + "\";");
            }
        }

        if (search.getCachedResults() == null || !searchTerm.equals(query)) {
            search.search(query);
        }
        ArrayList<ReadableZynstance> results = search.getCachedResults();
        for (ReadableZynstance instance : results) {
            String tags = "Tags: " + instance.getTagString();
            String meta = instance.getId() + " | v" + instance.getVersion() + " | Hidden: " + instance.isHidden() + "<br>" + tags;
            String actions = "<a onclick=\\\"connector('java.init.details.instance." + URLEncoder.encode(instance.getLocation(), StandardCharsets.UTF_8) + "');\\\"><i class='bx bx-spreadsheet'></i> More</a> <a style=\\\"background: #5632a8; color: white;\\\" onclick=\\\"connector('sync.discover.install.module.nexus-minecraft-module');\\\"><i class='bx bx-download'></i> Install</a>";
            String command = "addResult(\"" + instance.getId() + "\",\"" + instance.getThumbnailUrl() + "\",\"" + instance.getName() + "\",\"" + instance.getAuthor() + "\",\"" + instance.getSummary() + "\",\"" + meta + "\",\"" + actions + "\",\"" + instance.getLocation() + "\",\"java.init.details.instance." + instance.getLocation() + "\");";
            frame.executeJavaScript(command);
        }
    }
}
