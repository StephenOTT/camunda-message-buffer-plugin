package com.github.stephenott.camunda.messagebuffer.plugin

import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin
import org.camunda.bpm.model.bpmn.Bpmn


open class MessageBufferProcessEnginePlugin : ProcessEnginePlugin {

    val interceptor = MessageCommandInterceptor()

    override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {

        if (processEngineConfiguration.customPreCommandInterceptorsTxRequired == null) {
            processEngineConfiguration.customPreCommandInterceptorsTxRequired = mutableListOf()
        }
        processEngineConfiguration.customPreCommandInterceptorsTxRequired.add(interceptor)

        if (processEngineConfiguration.customPreCommandInterceptorsTxRequiresNew == null) {
            processEngineConfiguration.customPreCommandInterceptorsTxRequiresNew = mutableListOf()
        }
        processEngineConfiguration.customPreCommandInterceptorsTxRequiresNew.add(interceptor)

        if (processEngineConfiguration.customJobHandlers == null) {
            processEngineConfiguration.customJobHandlers = mutableListOf()
        }
        processEngineConfiguration.customJobHandlers.add(BufferedMessageJobHandler())

        processEngineConfiguration.setFailedJobCommandFactory { jobId, exception ->
            return@setFailedJobCommandFactory CustomDefaultJobRetryCmd(jobId, exception)
        }
    }

    override fun postInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
    }

    override fun postProcessEngineBuild(processEngine: ProcessEngine) {
    }
}