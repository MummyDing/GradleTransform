package com.github.mummyding.plugin

import com.android.build.gradle.AppExtension
import com.github.mummyding.transform.GradleTransform
import org.gradle.api.Plugin
import org.gradle.api.Project
class EntryPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def android = project.extensions.getByType(AppExtension)
        android.registerTransform(new GradleTransform())
    }
}
