plugins {
	id("opencreative.java-conventions")
	id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
	id("com.gradleup.shadow") version "9.6.1"
}

dependencies {
	implementation(project(":api"))
	paperweight {
		paperDevBundle("26.2.build.+")
	}

	implementation("commons-io:commons-io:2.22.0")

	// Fawe
	implementation(platform("com.intellectualsites.bom:bom-newest:1.56")) // Ref: https://github.com/IntellectualSites/bom
	compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core")
	compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit") { isTransitive = false }

	// Plugins
	compileOnly("me.clip:placeholderapi:2.12.3")
	compileOnly("net.dmulloy2:ProtocolLib:5.4.0")
	compileOnly("com.github.retrooper:packetevents-spigot:2.13.0")
	compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
	compileOnly("me.libraryaddict.disguises:libsdisguises:11.0.18")
	compileOnly("com.infernalsuite.asp:api:4.0.0-SNAPSHOT")
}

tasks {
	build {
		dependsOn(shadowJar)
	}
	shadowJar {
		archiveBaseName.set("OpenCreative")
		destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
		archiveClassifier.set("")
	}
}