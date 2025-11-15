plugins {
    java
    id("com.gradleup.shadow")
}

val paperVersion: String by rootProject.extra

dependencies {
    compileOnly("io.papermc.paper:paper-api:$paperVersion")
    implementation("com.github.Carleslc.Simple-YAML:Simple-Yaml:1.8.4")
    implementation(project(":femutils-core"))
}
