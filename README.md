# ResFoldersDetector
An Android Gradle plugin that detects added res folders and adds it to the res source set.

## Why?
I used to separate my resource files by feature after following [this guide](http://antonioleiva.com/android-multiple-resource-folders/),
but the problem with that approach is once the project get bigger, you will have a long array to
maintain, and with each new feature you will need manually to add a new entry to the
```main.res.srcDirs``` array. So I wrote this simple plugin to automate this process.


## Usage
All you have to do is create a folder named `res-root` and add that folder under the project's main
`res` folder, and by adding any folder under the `res-root`, it will be included automatically to
the `res.srcDirs` array.

## Configuration
In your app/build.gradle file
```
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "gradle.plugin.com.github.r4md4c.gradle:res-folders-detector:0.1"
  }
}

apply plugin: 'com.android.application'
apply plugin: "com.github.r4md4c.gradle.res-folders-detector-plugin"

resFolderDetector {
    rootDirName 'res-root' // DEFAULT
}
```


## Tests
To Run the tests:
`./gradlew install test`
