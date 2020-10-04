package com.github.stephenott.camunda.messagebuffer.plugin

import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin


open class MessageBufferProcessEnginePlugin : ProcessEnginePlugin {

    val interceptor = MessageCommandInterceptor()
    val failedJobInterceptor = BufferedMessageFailedJobCmdInterceptor()

    override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {

        if (processEngineConfiguration.customPreCommandInterceptorsTxRequired == null) {
            processEngineConfiguration.customPreCommandInterceptorsTxRequired = mutableListOf()
        }
        processEngineConfiguration.customPreCommandInterceptorsTxRequired.add(interceptor)
        processEngineConfiguration.customPreCommandInterceptorsTxRequired.add(failedJobInterceptor)

        if (processEngineConfiguration.customPreCommandInterceptorsTxRequiresNew == null) {
            processEngineConfiguration.customPreCommandInterceptorsTxRequiresNew = mutableListOf()
        }
        processEngineConfiguration.customPreCommandInterceptorsTxRequiresNew.add(interceptor)
        processEngineConfiguration.customPreCommandInterceptorsTxRequiresNew.add(failedJobInterceptor)

        if (processEngineConfiguration.customJobHandlers == null) {
            processEngineConfiguration.customJobHandlers = mutableListOf()
        }
        processEngineConfiguration.customJobHandlers.add(BufferedMessageJobHandler())
    }

    override fun postInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
    }

    override fun postProcessEngineBuild(processEngine: ProcessEngine) {
    }
}