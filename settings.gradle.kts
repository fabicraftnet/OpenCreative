/*
 * OpenCreative+, Minecraft plugin.
 * (C) 2022-2026, McChicken Studio, mcchickenstudio@gmail.com
 *
 * OpenCreative+ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenCreative+ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

rootProject.name = "opencreative"

include("api")
include("plugin")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/") // Paper
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
        maven("https://repo.dmulloy2.net/repository/public/") // ProtocolLib
        maven("https://repo.codemc.io/repository/maven-releases/") // PacketEvents
        maven("https://jitpack.io") // Vault
        maven("https://mvn.lib.co.nz/public") // Lib's Disguises
        maven("https://maven.enginehub.org/repo/") // FAWE
        maven("https://repo.infernalsuite.com/repository/maven-releases/") // ASP
        maven("https://repo.infernalsuite.com/repository/maven-snapshots/") // ASP
    }
}