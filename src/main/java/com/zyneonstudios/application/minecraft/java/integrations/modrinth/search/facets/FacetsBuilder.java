package com.zyneonstudios.application.minecraft.java.integrations.modrinth.search.facets;

import com.zyneonstudios.application.minecraft.java.integrations.modrinth.search.facets.categories.Category;

import java.util.ArrayList;

public class FacetsBuilder {

    private String facets = "[";
    private ArrayList<String> categories = new ArrayList<>();
    private ArrayList<String> gameVersions = new ArrayList<>();

    public FacetsBuilder withProjectType(String projectType) {
        facets = facets + "[%22project_type:" + projectType + "%22]";
        return this;
    }

    public FacetsBuilder withProjectType(ProjectType projectType) {
        return withProjectType(projectType.toString());
    }

    public FacetsBuilder withCategory(String category) {
        category = category.replace("feature_", "")
                .replace("loader_", "")
                .replace("performance_", "")
                .replace("proxy_", "")
                .replace("resolution_", "")
                .replace("_and_lower", "-")
                .replace("_and_higher", "+")
                .replace("_", "-");
        if(!categories.contains(category)) {
            categories.add(category);
        }
        return this;
    }

    public FacetsBuilder withCategory(Category category) {
        return withCategory(category.toString());
    }

    public FacetsBuilder withCategories(String... categories) {
        for (String category : categories) {
            withCategory(category);
        }
        return this;
    }

    public FacetsBuilder withCategories(Category... categories) {
        for (Category category : categories) {
            withCategory(category);
        }
        return this;
    }

    public FacetsBuilder withGameVersion(String gameVersion) {
        if(!gameVersions.contains(gameVersion)) {
            gameVersions.add(gameVersion);
        }
        return this;
    }

    public FacetsBuilder withGameVersions(String... gameVersions) {
        for(String gameVersion : gameVersions) {
            withGameVersion(gameVersion);
        }
        return this;
    }

    public FacetsBuilder withClientEnvironment(EnvironmentType type) {
        facets = facets + "[%22client_side:" + type + "%22]";
        return this;
    }

    public FacetsBuilder withServerEnvironment(EnvironmentType type) {
        facets = facets + "[%22server_side:" + type + "%22]";
        return this;
    }

    public FacetsBuilder withEnvironments(EnvironmentType clientSide, EnvironmentType serverSide) {
        facets = facets + "[%22client_side:" + clientSide + "%22][%22server_side:" + serverSide + "%22]";
        return this;
    }

    public FacetsBuilder withOpenSourceOnly(boolean openSource) {
        facets = facets + "[%22open_source:" + openSource + "%22]";
        return this;
    }

    public FacetsBuilder withTitle(String title) {
        facets = facets + "[%22title:" + title + "%22]";
        return this;
    }

    public FacetsBuilder withAuthor(String author) {
        facets = facets + "[%22author:" + author + "%22]";
        return this;
    }

    public FacetsBuilder withFollows(int follows) {
        facets = facets + "[%22follows:" + follows + "%22]";
        return this;
    }

    public FacetsBuilder withProjectId(String id) {
        facets = facets + "[%22project_id:" + id + "%22]";
        return this;
    }

    public FacetsBuilder withLicense(String license) {
        facets = facets + "[%22license:" + license + "%22]";
        return this;
    }

    public FacetsBuilder withDownloads(int downloads) {
        facets = facets + "[%22downloads:" + downloads + "%22]";
        return this;
    }

    public FacetsBuilder withColor(String color) {
        facets = facets + "[%22color:" + color + "%22]";
        return this;
    }

    public FacetsBuilder withCreatedTimestamp(String timestamp) {
        facets = facets + "[%22created_timestamp:" + timestamp + "%22]";
        return this;
    }

    public FacetsBuilder withModifiedTimestamp(String timestamp) {
        facets = facets + "[%22modified_timestamp:" + timestamp + "%22]";
        return this;
    }

    public String build() {
        if(!categories.isEmpty()) {
            StringBuilder categories = new StringBuilder();
            for(String category : this.categories) {
                if((categories.isEmpty()) || categories.toString().isBlank()) {
                    categories = new StringBuilder(category);
                } else {
                    categories.append(",").append(category);
                }
            }
            facets = facets + "[%22categories:" + categories + "%22]";
        }

        if(!gameVersions.isEmpty()) {
            StringBuilder gameVersions = new StringBuilder();
            for(String gameVersion : this.gameVersions) {
                if(gameVersions.isEmpty() || gameVersions.toString().isBlank()) {
                    gameVersions = new StringBuilder(gameVersion);
                } else {
                    gameVersions.append(",").append(gameVersion);
                }
            }
            facets = facets + "[%22versions:" + gameVersions + "%22]";
        }

        facets = ((facets + "]").replace("][", "],["));
        return facets;
    }
}