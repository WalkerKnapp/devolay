# Devolay
A Java library for sending and receiving video over the network using the Newtek NDI(tm) SDK. For more information about NDI(tm), see:

http://NDI.NewTek.com/

## Download / Installation
Currently, Devolay is only actively tested on Windows, but builds include Windows, Linux, and MacOS binaries.
If you have any issues unique to the Linux or MacOS builds, please [open an issue](https://github.com/WalkerKnapp/devolay/issues) so they can be resolved in the next version.

### Gradle
```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'me.walkerknapp:devolay:2.0.0'
}
```

### Maven
```xml
<dependency>
  <groupId>me.walkerknapp</groupId>
  <artifactId>devolay</artifactId>
  <version>2.0.0</version>
</dependency>
```

## Usage
Devolay aims to be close to the original NDI SDK while still following Java standards and conventions. The vast majority of applications can be simply translated from NDI SDK calls to Devolay calls.

Examples can be found in [examples/src/main/java/me/walkerknapp/devolayexamples](https://github.com/WalkerKnapp/devolay/tree/master/examples/src/main/java/me/walkerknapp/devolayexamples).

## Compiling

### Linux or WSL (Recommended)

#### Requirements

##### Targeting any platform
- [The NewTek NDI SDK (v4.0 or higher)](https://www.ndi.tv/sdk/)
- git
- openjdk-11-jdk

##### Targeting Linux
- gcc

##### Targeting Windows
- mingw-w64

##### Targeting MacOS
- [osxcross](https://github.com/tpoechtrager/osxcross)

##### Targeting Android
- libc6-dev-i386
- [Android NDK](https://developer.android.com/ndk/downloads)

##### Targeting IOS/tvOS
Targeting tvOS or iOS is theoretically possible, but not configured, as I don't have access to either platform to test. If you need support for these platforms and would be willing to help, please open an issue.

In theory, these dependencies would be needed:
- [An XCode DMG](https://developer.apple.com/xcode/download) (Note: This requires a free Apple ID)
- libfuse-dev
- libicu-dev
- openssl
- zlib1g
- libbz2-dev
- cctools-port (Instructions for setup can be found [here](https://web.archive.org/web/20200902224950/https://github.com/tpoechtrager/osxcross/issues/45#issuecomment-138351002). If you have xcode as a *.xip file instead of a *.dmg, use [these instructions](https://web.archive.org/web/20200902224812/https://github.com/bitcoin/bitcoin/issues/8748#issuecomment-247745279) to unzip it.)

#### Building

Clone the repository, or download the zip archive and extract it:
```
> git clone --recurse-submodules -j8 https://github.com/WalkerKnapp/devolay.git 
> cd devolay
```

Run the automatic assembly:
```
> ./gradlew assemble
> ./gradlew install
```

The library output will now be in `devolay-java/build/libs` and installed in the local maven repository.
The jar can either be used directly, or with gradle:
```groovy
repositories {
    mavenLocal()
}

dependencies {
    implementation 'com.walker:devolay:VERSION'
}
```
