plugins {
    java
    idea
    id("cn.bestwu.publish") version "0.0.18"
}

group = "cn.bestwu"
version = "1.1.0"

repositories {
    jcenter()
}

dependencies {
    compile("org.slf4j:slf4j-api:1.7.25")
    compile("com.fasterxml.jackson.core:jackson-databind:2.9.4")
    compileOnly("org.apache.lucene:lucene-analyzers-common:7.2.1")
    compileOnly("org.springframework.boot:spring-boot-starter-web:2.0.0.RELEASE")
    testCompile("junit:junit:4.12")
}

idea {
    module {
        inheritOutputDirs = false
        isDownloadJavadoc = false
        isDownloadSources = true
        outputDir = java.sourceSets["main"].java.outputDir
        testOutputDir = java.sourceSets["test"].java.outputDir
    }
}