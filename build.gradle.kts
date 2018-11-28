plugins {
    java
    idea
    kotlin("jvm") version "1.2.60"
    id("cn.bestwu.publish") version "0.0.30"
    id("com.jfrog.artifactory") version "4.7.5"
}

group = "cn.bestwu"
version = "1.1.5-SNAPSHOT"

repositories {
    jcenter()
}

dependencies {
    compile(kotlin("stdlib"))
    compile("com.fasterxml.jackson.core:jackson-databind:2.9.6")
    compileOnly("org.apache.lucene:lucene-analyzers-common:7.4.0")
    compileOnly("org.springframework.boot:spring-boot-starter-web:2.0.4.RELEASE")
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
