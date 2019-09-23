# Devolay  [ ![Download](https://api.bintray.com/packages/walkerknapp/devolay/devolay/images/download.svg) ](https://bintray.com/walkerknapp/devolay/devolay/_latestVersion)
Java bindings for the Newtek NDI(tm) SDK. For more information about NDI(tm), see:

http://NDI.NewTek.com/

## Download / Installation
Currently, devolay is distrubuted using Bintray and only has windows builds. If a build is needed for Linux or MacOS, see Compiling. If anyone needs a public build of this library for those platforms, [open an issue](https://github.com/WalkerKnapp/devolay/issues) and I can try to release builds in the next version.

### Gradle
```groovy
repositories {
    maven {
        url  "https://dl.bintray.com/walkerknapp/devolay"
    }
}

dependencies {
    implementation 'com.walker:devolay:0.0.1'
}
```

### Maven
In settings.xml:
```xml
<repositories>
    <repository>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
        <id>bintray-walkerknapp-devolay</id>
        <name>bintray</name>
        <url>https://dl.bintray.com/walkerknapp/devolay</url>
    </repository>
</repositories>
```
In pom.xml:
```xml
<dependency>
  <groupId>com.walker</groupId>
  <artifactId>devolay</artifactId>
  <version>0.0.1</version>
  <type>pom</type>
</dependency>
```

## Usage
Devolay aims to be close to the original NDI SDK while still following Java standards and conventions. The vast majority of applications can be simply translated from NDI SDK calls to Devolay calls.

Example can be found in [examples/src/main/java/com/walker/devolayexamples](https://github.com/WalkerKnapp/devolay/tree/master/examples/src/main/java/com/walker/devolayexamples).
