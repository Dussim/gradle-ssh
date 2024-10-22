import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8

plugins {
    `kotlin-dsl`
    id("com.gradle.plugin-publish").version("1.3.0")
    id("com.github.hierynomus.license").version("0.16.1")
    id("ru.vyarus.github-info").version("2.0.0")
    id("org.jmailen.kotlinter").version("4.4.1")
    id("org.jetbrains.dokka").version("1.9.20")
    id("com.github.ben-manes.versions").version("0.51.0")
}

dependencies {
    implementation("com.hierynomus:sshj:0.39.0")
    implementation("com.jcraft:jzlib:1.1.3")
}

kotlin {
    target.compilations.configureEach {
        compilerOptions.options.jvmTarget.set(JVM_1_8)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
}

group = "xyz.dussim"
version = "0.0.3"

gradlePlugin {
    plugins {
        create("xyz.dussim.gradle-ssh") {
            id = "xyz.dussim.gradle-ssh"
            implementationClass = "xyz.dussim.gradlessh.SshPlugin"

            displayName = "Execute simple ssh commands as Gradle tasks with gradle-ssh"
            tags = listOf("ssh", "remote execution")
            description = "Plugin to execute simple ssh commands as Gradle tasks"
        }
    }
}

publishing {
    repositories {
        maven {
            name = "localPluginRepository"
            url = uri(".localMaven")
        }
    }
}

github {
    user = "Dussim"
    license = "Apache"
}

license {
    header = file("LICENSE_HEADER")
    strictCheck = true

    ext.set("year", "2024")
    ext.set("name", "Dussim (Artur Tuzim)")
    ext.set("email", "artur@tuzim.xzy")
}

downloadLicenses {
    includeProjectDependencies = false
    dependencyConfiguration = "runtimeClasspath"
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

val formatAll = tasks.register<Task>("formatAll") {
    group = "formatting"
    description = "Format all kotlin source files and attach license header to them."
    dependsOn("licenseFormat", "formatKotlin")
}

tasks.publish {
    dependsOn(formatAll)
}

//region dependencies
fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.dependencyUpdates {
    checkForGradleUpdate = true
    checkBuildEnvironmentConstraints = true
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}
//endregion