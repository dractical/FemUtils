import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.jvm.tasks.Jar

plugins {
    java
    id("com.gradleup.shadow") version "9.1.0" apply false
}

group = "com.dractical"
version = "1.0"
extra["paperVersion"] = "1.21.8-R0.1-SNAPSHOT"

allprojects {
    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/") {
            name = "papermc"
        }
        maven("https://oss.sonatype.org/content/groups/public/") {
            name = "sonatype"
        }
        maven("https://jitpack.io") {
            name = "jitpack"
        }
    }

    plugins.withId("java") {
        extensions.configure<JavaPluginExtension> {
            toolchain.languageVersion.set(JavaLanguageVersion.of(21))
        }
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    plugins.withId("java") {
        extensions.configure<PublishingExtension> {
            publications {
                create<MavenPublication>("mavenJava") {
                    from(components["java"])
                    artifactId = project.name
                    groupId = project.group.toString()
                    version = project.version.toString()
                }
            }
            repositories {
                mavenLocal()
            }
        }
    }

    plugins.withId("com.gradleup.shadow") {
        tasks.named<Jar>("jar") {
            enabled = false
        }

        tasks.named<ShadowJar>("shadowJar") {
            archiveVersion.set("")
            archiveClassifier.set("")
        }

        tasks.named("build") {
            dependsOn("shadowJar")
        }
    }
}
