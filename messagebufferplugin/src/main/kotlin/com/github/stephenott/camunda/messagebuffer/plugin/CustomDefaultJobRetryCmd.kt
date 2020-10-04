package com.github.stephenott.camunda.messagebuffer.plugin

import org.camunda.bpm.engine.impl.bpmn.parser.DefaultFailedJobParseListener
import org.camunda.bpm.engine.impl.bpmn.parser.FailedJobRetryConfiguration
import org.camunda.bpm.engine.impl.cmd.DefaultJobRetryCmd
import org.camunda.bpm.engine.impl.context.Context
import org.camunda.bpm.engine.impl.interceptor.CommandContext
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl

/**
 * Provides extra handling for buffered jobs
 */
class CustomDefaultJobRetryCmd(jobId: String, exception: Throwable) : DefaultJobRetryCmd(jobId, exception) {

    override fun getCurrentActivity(commandContext: CommandContext, job: JobEntity): ActivityImpl {
        val type = job.jobHandlerType
        var activity: ActivityImpl? = null

        if (SUPPORTED_TYPES.contains(type)) {
            val deploymentCache = Context.getProcessEngineConfiguration().deploymentCache
            val processDefinitionEntity = deploymentCache.findDeployedProcessDefinitionById(job.processDefinitionId)
            activity = processDefinitionEntity.findActivity(job.activityId)

        } else if (type == BufferedMessageJobHandler.TYPE) {
            val expressionText: String? = (job.jobHandlerConfiguration as BufferedMessageJobHandler.BufferedMessageJobHandlerConfiguration).cfg.failedJobRetryExpression

            if (expressionText != null) {
                val cfg = FailedJobRetryConfiguration(commandContext.processEngineConfiguration.expressionManager.createExpression(expressionText))

                activity = ActivityImpl("__custom-job-handler__", null).also { actImpl ->
                    actImpl.properties.set(DefaultFailedJobParseListener.FAILED_JOB_CONFIGURATION, cfg)
                }
            }
        } else {
            // noop, because activity type is not supported
        }

        return activity!!
    }
}