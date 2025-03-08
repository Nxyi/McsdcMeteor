plugins {
    id("fabric-loom") version "1.9-SNAPSHOT"
}

base {
    archivesName = properties["archives_base_name"] as String
    version = properties["mod_version"] as String
    group = properties["maven_group"] as String
}

repositories {
    maven {
        name = "meteor-maven"
        url = uri("https://maven.meteordev.org/releases")
    }
    maven {
        name = "meteor-maven-snapshots"
        url = uri("https://maven.meteordev.org/snapshots")
    }
    maven {
        name = "ViaVersion"
        url = uri("https://repo.viaversion.com")
    }
    maven {
        name = "Lenni0451"
        url = uri("https://maven.lenni0451.net/everything")
    }
    maven {
        name = "TerraformersMC"
        url = uri("https://maven.terraformersmc.com/releases")
    }
    maven {
        name = "OpenCollab Snapshots"
        url = uri("https://repo.opencollab.dev/maven-snapshots/")
    }
    maven {
        name = "JitPack"
        url = uri("https://jitpack.io")
    }
}

dependencies {
    // Fabric
    minecraft("com.mojang:minecraft:${properties["minecraft_version"] as String}")
    mappings("net.fabricmc:yarn:${properties["yarn_mappings"] as String}:v2")
    modImplementation("net.fabricmc:fabric-loader:${properties["loader_version"] as String}")

    // Meteor
    modImplementation("meteordevelopment:meteor-client:${properties["minecraft_version"] as String}-SNAPSHOT")

    // ViaFabric
    modImplementation("com.viaversion:viafabricplus:${properties["viafabricplus_version"]}")
}

tasks {
    processResources {
        val propertyMap = mapOf(
            "version" to project.version,
            "mc_version" to project.property("minecraft_version"),
        )

        filesMatching("fabric.mod.json") {
            expand(propertyMap)
        }
    }

    jar {
        val licenseSuffix = project.base.archivesName.get()
        from("LICENSE") {
            rename { "${it}_${licenseSuffix}" }
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = 21
    }
}

loom {
    accessWidenerPath = file("src/main/resources/mcsdc.accesswidener")
}
