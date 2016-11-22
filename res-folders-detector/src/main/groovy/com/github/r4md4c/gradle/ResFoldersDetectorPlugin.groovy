package com.github.r4md4c.gradle

import com.android.annotations.VisibleForTesting
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.BasePlugin
import groovy.io.FileType
import groovy.transform.CompileStatic
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

import java.nio.file.NotDirectoryException

/**
 * Created by ahkhalil on 11/19/16.
 */
class ResFoldersDetectorPlugin implements Plugin<Project> {
    private Project project;
    private ResFoldersDetectorExtension resExtension;

    void apply(Project project) {
        this.project = project;

        resExtension = project.extensions.create("resFolderDetector", ResFoldersDetectorExtension)

        BasePlugin basePlugin = getAndroidBasePlugin(project)
        if (!basePlugin) {
            throw new GradleException('You must apply the Android application plugin ' +
                    'plugin before using the res-folders-detector plugin')
        }

        project.android.applicationVariants.all { variant ->
            variant.sourceSets.each { sourceSet ->
                sourceSet.res.srcDirs.each { File f ->
                    File resRootFile = new File(f, resExtension.rootDirName)
                    if (!resRootFile.exists()) return
                    getDirsUnderRoot(resRootFile).each {
                        androidExtension.sourceSets.findByName(sourceSet.name).res.srcDir(it.absolutePath)
                    }
                }
            }
        }

    }

    @CompileStatic
    private static BasePlugin getAndroidBasePlugin(Project project) {
        def plugin = project.plugins.findPlugin('com.android.application')

        return plugin as BasePlugin
    }

    @CompileStatic
    private BaseExtension getAndroidExtension() {
        return project.extensions.getByName('android') as BaseExtension
    }

    @VisibleForTesting
    static List<File> getDirsUnderRoot(File rootDir) {
        if (!rootDir.exists()) {
            throw new FileNotFoundException("${rootDir.path} does not exist.")
        }
        if (!rootDir.directory) {
            throw new NotDirectoryException("${rootDir.path} is not a directory.")
        }

        def list = []
        rootDir.eachFile(FileType.DIRECTORIES, { dir ->
            list << dir
        })

        return list
    }
}
