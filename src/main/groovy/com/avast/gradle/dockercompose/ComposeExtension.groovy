package com.avast.gradle.dockercompose

import com.avast.gradle.dockercompose.tasks.ComposeDown
import com.avast.gradle.dockercompose.tasks.ComposeUp
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.process.JavaForkOptions
import org.gradle.process.ProcessForkOptions

import java.time.Duration

class ComposeExtension {
    private final ComposeUp upTask
    private final ComposeDown downTask
    private final Project project

    boolean buildBeforeUp = true
    boolean waitForTcpPorts = true
    Duration waitAfterTcpProbeFailure = Duration.ofSeconds(1)

    boolean stopContainers = true
    boolean removeContainers = true

    ComposeExtension(Project project, ComposeUp upTask, ComposeDown downTask) {
        this.project = project
        this.downTask = downTask
        this.upTask = upTask
    }

    void isRequiredBy(Task task) {
        task.dependsOn upTask
        task.finalizedBy downTask
    }

    Map<String, ServiceInfo> getServicesInfos() {
        upTask.servicesInfos
    }

    void exposeAsEnvironment(ProcessForkOptions task) {
        servicesInfos.values().each { si ->
            task.environment.put("${si.name.toUpperCase()}_HOST".toString(), si.host)
            si.tcpPorts.each {
                task.environment.put("${si.name.toUpperCase()}_TCP_${it.key}".toString(), it.value)
            }
        }
    }

    void exposeAsSystemProperties(JavaForkOptions task) {
        servicesInfos.values().each { si ->
            task.systemProperties.put("${si.name}.host".toString(), si.host)
            si.tcpPorts.each {
                task.systemProperties.put("${si.name}.tcp.${it.key}".toString(), it.value)
            }
        }
    }
}