package com.whensunset.invoker

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

public class InvokerPlugin implements Plugin<Project> {

    void apply(Project project) {
        def isApp = project.plugins.hasPlugin(AppPlugin)
        // 仅处理application合包
        if (isApp) {
            def android = project.extensions.findByType(AppExtension)
            android.registerTransform(new InvokerTransform(project))
        }
    }
}