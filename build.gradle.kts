plugins {
    id("org.jenkins-ci.jpi") version "0.53.0"
}

group = "org.jenkins-ci.plugins"
version = "1.32-SNAPSHOT"

jenkinsPlugin {
    jenkinsVersion = "2.479.1"
    shortName = "text-finder"
    displayName = "Text Finder"
    url = "https://github.com/jenkinsci/text-finder-plugin"
    gitHub.set(uri("https://github.com/jenkinsci/text-finder-plugin"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(platform("io.jenkins.tools.bom:bom-2.479.x:4136.vca_c3202a_7fd1"))

    testImplementation("io.jenkins:configuration-as-code")
    testImplementation("org.jenkins-ci.plugins:config-file-provider")
    testImplementation("org.jenkins-ci.plugins:job-dsl")
    testImplementation("org.jenkins-ci.plugins.workflow:workflow-api")
    testImplementation("org.jenkins-ci.plugins.workflow:workflow-basic-steps")
    testImplementation("org.jenkins-ci.plugins.workflow:workflow-cps")
    testImplementation("org.jenkins-ci.plugins.workflow:workflow-durable-task-step")
    testImplementation("org.jenkins-ci.plugins.workflow:workflow-job")
    testImplementation("org.jenkins-ci.plugins.workflow:workflow-support")
}
