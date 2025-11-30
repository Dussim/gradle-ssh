import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17

plugins {
    `kotlin-dsl`
    id("com.gradle.plugin-publish").version("1.3.1")
    id("com.github.hierynomus.license").version("0.16.1")
    id("ru.vyarus.github-info").version("2.0.0")
    id("org.jmailen.kotlinter").version("5.3.0")
    id("org.jetbrains.dokka").version("2.1.0")
    id("com.github.ben-manes.versions").version("0.53.0")
}

dependencies {
    implementation("com.hierynomus:sshj:0.40.0")
    implementation("com.jcraft:jzlib:1.1.3")

    testImplementation(gradleTestKit())
    testImplementation(platform("org.junit:junit-bom:6.0.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(kotlin("test"))
}

kotlin.compilerOptions {
    jvmTarget = JVM_17
    freeCompilerArgs.add("-Xjdk-release=17")
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 17
}

group = "xyz.dussim"
version = "0.0.4"

gradlePlugin {
    plugins {
        register("xyz.dussim.gradle-ssh") {
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

    ext.set("year", "2025")
    ext.set("name", "Dussim (Artur Tuzim)")
    ext.set("email", "artur@tuzim.xzy")
}

downloadLicenses {
    includeProjectDependencies = false
    dependencyConfiguration = "runtimeClasspath"
}

kotlinter {
    ktlintVersion = "1.8.0"
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

tasks.test {
    useJUnitPlatform()
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