package com.github.stephenott.camunda.messagebuffer.plugin

import org.camunda.bpm.engine.impl.MessageCorrelationBuilderImpl
import org.camunda.bpm.engine.impl.cmd.CorrelateMessageCmd
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * Creates a configuration class for a job
 */
fun CorrelateMessageCmd.convertForPersistence(failedJobRetryExpression: String? = null): BufferedMessageConfiguration {
    val builderImpl = ((this::class.memberProperties.find { it.name == "builder" } as KProperty1<Any, *>).also { it.isAccessible = true }).get(this) as MessageCorrelationBuilderImpl
    val startMessageOnly = ((this::class.memberProperties.find { it.name == "startMessageOnly" } as KProperty1<Any, *>).also { it.isAccessible = true }).get(this) as Boolean
    val messageName = ((this::class.memberProperties.find { it.name == "messageName" } as KProperty1<Any, *>).also { it.isAccessible = true }).get(this) as String
    val collectVariables = ((this::class.memberProperties.find { it.name == "variablesEnabled" } as KProperty1<Any, *>).also { it.isAccessible = true }).get(this) as Boolean
    val deserializeVariableValues = ((this::class.memberProperties.find { it.name == "deserializeVariableValues" } as KProperty1<Any, *>).also { it.isAccessible = true }).get(this) as Boolean

    return BufferedMessageConfiguration(startMessageOnly = startMessageOnly,
            messageName = messageName,
            collectVariables = collectVariables,
            deserializeVariableValues = deserializeVariableValues,
            builderBusinessKey = builderImpl.businessKey,
            builderMessageName = builderImpl.messageName,
            builderProcessDefinitionId = builderImpl.processDefinitionId,
            builderProcessInstanceId = builderImpl.processInstanceId,
            builderTenantId = builderImpl.tenantId,
            builderIsExclusiveCorrelation = builderImpl.isExclusiveCorrelation,
            builderIsTenantIdSet = builderImpl.isTenantIdSet,
            builderCorrelationProcessInstanceVariables = builderImpl.correlationProcessInstanceVariables,
            builderCorrelationLocalVariables = builderImpl.correlationLocalVariables,
            builderPayloadProcessInstanceVariables = builderImpl.payloadProcessInstanceVariables,
            builderPayloadProcessInstanceVariablesLocal = builderImpl.payloadProcessInstanceVariablesLocal,
            failedJobRetryExpression = failedJobRetryExpression
    )
}