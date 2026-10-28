# Family TV Home

A personal Hebrew/Russian category launcher based on Gathin23's CleanYourTV. [Hebrew guide](README.he.md).

Build from this directory with JDK 17+ and Android SDK 34:

```sh
./gradlew --no-daemon assembleRelease
```

The output is `app/build/outputs/apk/release/app-release.apk`. The application ID remains `com.gathin.tvhome`; updates require the same signing key. Local ADB and signing keys are deliberately excluded from this repository. This variant embeds user-provided banner artwork and is larger than the minimal upstream launcher.

See the root MIT license and the included font license. Upstream source and safety tools remain in their original directories.
