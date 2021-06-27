# Devolay
A Java library for sending and receiving video over the network using the Newtek NDI® SDK. For more information about NDI®, see:

http://NDI.NewTek.com/

## Download / Installation

### Separated Builds
Separated builds **do not** contain NDI SDK binaries,
so end users will need to install the Newtek NDI SDK.

#### Gradle
```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'me.walkerknapp:devolay:2.0.1'
}
```

#### Maven
```xml
<dependency>
  <groupId>me.walkerknapp</groupId>
  <artifactId>devolay</artifactId>
  <version>2.0.1</version>
</dependency>
```

### Integrated Builds

Integrated builds contain NDI SDK binaries bundled into the Jar file,
so end users do not need to install the NDI SDK.
However, there are some licensing restrictions that must be followed
if you use integrated builds in your product. Please see the [Licensing Considerations Section](#licensing-considerations).


#### Gradle
```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'me.walkerknapp:devolay:2.0.1:integrated'
}
```

#### Maven
```xml
<dependency>
  <groupId>me.walkerknapp</groupId>
  <artifactId>devolay</artifactId>
  <version>2.0.1</version>
  <classifier>integrated</classifier>  
</dependency>
```


## ⚠️Licensing Considerations⚠️

`Separated` Builds do not contain any assets restricted under the NDI SDK License Agreement.
However, `Integrated` Builds contain binary files from each platform's NDI SDK.
Therefore, any products distributed to end users that use `Integrated` Builds **must** follow the guidelines in section 5.2 of the NDI SDK Documentation.

For full details, please install the NDI SDK and read the NDI SDK Documentation and NDI SDK License Agreement.
To summarize to the best of my knowledge (not legal advice, please get legal consultation any serious application),
applications must:
- Provide a link to [http://ndi.tv/](http://ndi.tv/) where NDI is used in the application, on its website, and in its documentation.
- Refer to NDI, the product, with "NDI®", and contain the phrase "NDI® is a registered trademark of NewTek, Inc." on the same page where it is used (this only applies to the first use of "NDI" in a document).
- Include the phrase "NDI® is a registered trademark of NewTek, Inc." in any About Box and other locations where trademark attribution is provided.

## Usage
Devolay aims to be close to the original NDI SDK while still following Java standards and conventions. The vast majority of applications can be simply translated from NDI SDK calls to Devolay calls.

Examples can be found in [examples/src/main/java/me/walkerknapp/devolayexamples](https://github.com/WalkerKnapp/devolay/tree/master/examples/src/main/java/me/walkerknapp/devolayexamples).

### Android Users

On Android, NDI needs access to the Network Service Discovery Manager, so an instance of the "NsdManager" needs to be
exist whenever a sender, finder, or receiver is instantiated. This can be done by adding this to the beginning of long-running activities:
```java
private NsdManager nsdManager;
```
At some point before creating a sender, finder, or receiver, instantiate the NsdManager:
```java
nsdManager = (NsdManager)getSystemService(Context.NSD_SERVICE);
```
This requires the `INTERNET` permission, which can be granted by adding this to your manifest:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

Additionally, emulators in Android Studio with an ABI of `x86_64` do not work with Devolay.
Physical devices appear to function normally, but NDI seems to encounter an emulation bug here.
To mitigate this, try to use `x86` emulators, ARM emulators, or physical devices for testing.

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
