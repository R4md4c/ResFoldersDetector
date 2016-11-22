package com.github.r4md4c.gradle

import com.android.build.gradle.AppPlugin
import com.github.r4md4c.gradle.internal.FileHelper
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Created by r4md4c on 11/20/16.
 */
class ResFoldersDetectorPluginTest implements FileHelper {
    @Rule
    public TemporaryFolder dir = new TemporaryFolder();

    Project project;

    @Before
    public void beforeEachTest() {
        project = ProjectBuilder.builder().withProjectDir(dir.root).build();
    }

    @Test(expected = GradleException.class)
    public void testThat_PluginDoesntWork_WhenWithoutAppPlugin() {
        project.pluginManager.apply(ResFoldersDetectorPlugin)
    }

    @Test
    public void testThat_PluginWorks_WhenUsedWithAppPlugin() {
        project.pluginManager.apply(AppPlugin)
        project.pluginManager.apply(ResFoldersDetectorPlugin)

        assert project.plugins.hasPlugin(ResFoldersDetectorPlugin)
    }

    @Test
    public void testThat_PluginIsWorking_AndResRootDirIsIncludedInSourceSet() {
        project.pluginManager.apply(AppPlugin)
        project.pluginManager.apply(ResFoldersDetectorPlugin)

        def extension = project.extensions.getByName('resFolderDetector')

        def layoutCode = """
                    <?xml version="1.0" encoding="utf-8"?>
                    <TextView
                        xmlns:android="http://schemas.android.com/apk/res/android"
                        android:text="Hello World!"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content" />
                        """.trim()

        file('src/main/AndroidManifest.xml') << """
               <?xml version="1.0" encoding="utf-8"?>
                <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                package="com.r4md4c.github" />
                """.trim()

        file('src/main/java/com/r4md4c/github/OnBoardingActivity.java') << """
              package com.r4md4c.github;

              import android.app.Activity;
              import android.os.Bundle;

              class OnBoardingActivity extends Activity {
                @Override
                protected void onCreate(Bundle savedInstanceState) {
                  super.onCreate(savedInstanceState);
                  setContentView(R.layout.activity_onboarding);
                }
              }
        """

        file('src/main/res/layout/activity_main.xml') << layoutCode
        file("src/main/res/${extension.rootDirName}/res-onboarding/layout/activity_onboarding.xml") << layoutCode

        setupGradleTestScript()

        // For debugging
        copyTestDir()

        BuildResult buildResult = GradleRunner.create()
                .withProjectDir(dir.getRoot())
                .withArguments("assemble")
                .build();

        assert buildResult.task(":assemble").outcome == TaskOutcome.SUCCESS

        // Assert that the activity_onboarding.xml that was in a different res dir is included in the build.
        assert file('build/intermediates/res/merged/debug/layout/activity_onboarding.xml').exists()
    }

    @Test
    public void testGetDirsUnderRoot() {
        File resRoot = new File("src/test/resources/res-root");

        def dirsList = ResFoldersDetectorPlugin.getDirsUnderRoot(resRoot)
        def expected = [new File(resRoot, "dir1"), new File(resRoot, "dir2")]

        Collections.sort(dirsList)
        Collections.sort(expected)

        assert dirsList.size() == 2
        assert expected == dirsList
    }

    @Override
    TemporaryFolder getDir() {
        return dir
    }

    private void setupGradleTestScript() {
        file("settings.gradle") << "rootProject.name = 'test-app'"

        buildFile << """
          buildscript {
            repositories {
               maven { url "${System.getProperty('localRepoUrl').toURI()}" }
              jcenter()
            }
            dependencies {
              classpath 'com.android.tools.build:gradle:2.2.2'
              classpath 'com.github.r4md4c.gradle:res-folders-detector:${
            System.getProperty("pluginVersion")
        }'
            }
          }
          apply plugin: 'com.android.application'
          apply plugin: 'res-folders-detector'
          repositories {
            jcenter()
          }
          android {
            compileSdkVersion 25
            buildToolsVersion "25.0.0"
            defaultConfig {
              minSdkVersion 16
              targetSdkVersion 25
              versionCode 1
              versionName '1.0.0'
              testInstrumentationRunner 'android.support.test.runner.AndroidJUnitRunner'
            }
            compileOptions {
              sourceCompatibility '1.7'
              targetCompatibility '1.7'
            }
          }"""
    }
}
