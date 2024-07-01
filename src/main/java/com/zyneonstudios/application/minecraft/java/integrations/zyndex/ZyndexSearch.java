package com.zyneonstudios.application.minecraft.java.integrations.zyndex;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.zyneonstudios.application.main.NexusApplication;
import com.zyneonstudios.nexus.instance.ReadableZynstance;
import live.nerotv.shademebaby.utils.GsonUtil;

import java.util.ArrayList;

public class ZyndexSearch {

    private final JsonArray array;
    private ArrayList<ReadableZynstance> cachedResults = null;
    private String cachedSearchTerm = null;
    private final boolean officialSource;
    boolean isSearching = false;

    public ZyndexSearch(String zyndexUrl) {
        array = GsonUtil.getObject(zyndexUrl).getAsJsonArray("instances");
        officialSource = zyndexUrl.equals("https://raw.githubusercontent.com/zyneonstudios/nexus-nex/main/zyndex/index.json");
    }

    @SuppressWarnings("all")
    public ArrayList<ReadableZynstance> search(String searchTerm) {
        if(!isSearching) {
            isSearching = true;
            if (!searchTerm.replace(" ", "").isEmpty()) {
                cachedSearchTerm = searchTerm;
            }

            ArrayList<ReadableZynstance> results = new ArrayList<>();
            String[] searchTerms = searchTerm.toLowerCase().replace(" ", ",").replace(",,", ",").split(",");

            if (!array.isEmpty()) {
                for (JsonElement element : array) {
                    try {
                        ReadableZynstance result = new ReadableZynstance(element.getAsString());
                        if (!result.isHidden()) {
                            results.add(result);
                        } else {
                            if (result.isHidden()) {
                                if (searchTerm.equals(result.getId())) {
                                    results.add(result);
                                }
                            }
                        }
                    } catch (Exception e) {
                        NexusApplication.getLogger().error("[MINECRAFT] Couldn't process search result: " + e.getMessage());
                    }
                }
            }

            cachedResults = results;
            isSearching = false;
            return cachedResults;
        }
        isSearching = false;
        return null;
    }

    public ArrayList<ReadableZynstance> getCachedResults() {
        return cachedResults;
    }

    public String getCachedSearchTerm() {
        return cachedSearchTerm;
    }
}