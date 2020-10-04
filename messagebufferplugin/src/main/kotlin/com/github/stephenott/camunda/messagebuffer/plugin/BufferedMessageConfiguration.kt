package com.github.stephenott.camunda.messagebuffer.plugin

import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.camunda.bpm.engine.impl.bpmn.parser.DefaultFailedJobParseListener
import org.camunda.bpm.engine.impl.bpmn.parser.FailedJobRetryConfiguration
import org.camunda.bpm.engine.impl.context.Context
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
class BufferedMessageConfiguration(
        val startMessageOnly: Boolean,
        val messageName: String,
        val collectVariables: Boolean,
        val deserializeVariableValues: Boolean,
        val builderBusinessKey: String?,
        val builderMessageName: String,
        val builderProcessDefinitionId: String?,
        val builderProcessInstanceId: String?,
        val builderTenantId: String?,
        val builderIsExclusiveCorrelation: Boolean,
        val builderIsTenantIdSet: Boolean,
        val builderCorrelationProcessInstanceVariables: Map<String, Any?>?,
        val builderCorrelationLocalVariables: Map<String, Any?>?,
        val builderPayloadProcessInstanceVariables: Map<String, Any?>?,
        val builderPayloadProcessInstanceVariablesLocal: Map<String, Any?>?,
        val failedJobRetryExpression: String?
)