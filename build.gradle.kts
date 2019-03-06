plugins {
    java
    idea
    kotlin("jvm") version "1.3.10"
    id("cn.bestwu.publish") version "0.0.30"
    id("com.jfrog.artifactory") version "4.7.5"
}

group = "cn.bestwu"
version = "1.1.7-SNAPSHOT"

repositories {
    jcenter()
}

dependencies {
    compile(kotlin("stdlib"))
    compile("com.fasterxml.jackson.core:jackson-databind:2.9.7")
    compileOnly("org.jsoup:jsoup:1.11.3")
    compileOnly("org.springframework.boot:spring-boot-starter-web:2.1.1.RELEASE")

    testCompile("org.jsoup:jsoup:1.11.3")
    testCompile("junit:junit:4.12")
}

idea {
    module {
        inheritOutputDirs = false
        isDownloadJavadoc = false
        isDownloadSources = true
        outputDir = the<SourceSetContainer>()["main"].java.outputDir
        testOutputDir = the<SourceSetContainer>()["test"].java.outputDir
    }
}
