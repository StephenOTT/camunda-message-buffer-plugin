package com.github.stephenott.camunda.messagebuffer.plugin

import org.camunda.bpm.engine.impl.MessageCorrelationBuilderImpl
import org.camunda.bpm.engine.impl.cmd.CorrelateMessageCmd
import org.camunda.bpm.engine.impl.context.Context
import org.camunda.bpm.engine.impl.interceptor.CommandContext
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler
import org.camunda.bpm.engine.impl.jobexecutor.JobHandlerConfiguration
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity
import org.camunda.bpm.engine.variable.impl.VariableMapImpl
import org.camunda.spin.Spin
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class BufferedMessageJobHandler: JobHandler<BufferedMessageJobHandler.BufferedMessageJobHandlerConfiguration> {

    class BufferedMessageJobHandlerConfiguration(
            val cfg: BufferedMessageConfiguration
    ): JobHandlerConfiguration {
        override fun toCanonicalString(): String {
            return Spin.JSON(cfg).toString()
        }
    }

    override fun getType(): String {
        return "buffered-message"
    }

    override fun execute(configuration: BufferedMessageJobHandlerConfiguration, execution: ExecutionEntity?, commandContext: CommandContext, tenantId: String?) {
        val d = configuration.cfg

        //@TODO Review how the TenantId argument for this method should aling or does not matter? for the builder.tenantId ?

        check(d.messageName == d.builderMessageName){"Message Name and Builder Message Name must match."}

        val builder = MessageCorrelationBuilderImpl(commandContext, d.builderMessageName)

        (builder::class.memberFunctions.find { it.name == "ensureCorrelationProcessInstanceVariablesInitialized" } as KFunction<*>)
                .apply { isAccessible = true }.call(builder)

        (builder::class.memberFunctions.find { it.name == "ensureCorrelationLocalVariablesInitialized" } as KFunction<*>)
                .apply { isAccessible = true }.call(builder)

        (builder::class.memberFunctions.find { it.name == "ensurePayloadProcessInstanceVariablesInitialized" } as KFunction<*>)
                .apply { isAccessible = true }.call(builder)

        (builder::class.memberFunctions.find { it.name == "ensurePayloadProcessInstanceVariablesLocalInitialized" } as KFunction<*>)
                .apply { isAccessible = true }.call(builder)

        d.builderBusinessKey?.let {
            builder.processInstanceBusinessKey(d.builderBusinessKey)
        }
        d.builderProcessDefinitionId?.let {
            builder.processDefinitionId(d.builderProcessDefinitionId)
        }
        d.builderProcessInstanceId?.let {
            builder.processInstanceId(d.builderProcessInstanceId)
        }
        d.builderTenantId?.let {
            builder.tenantId(d.builderTenantId)
        }
        d.builderCorrelationProcessInstanceVariables?.let {
            builder.correlationProcessInstanceVariables.putAll(it)
        }
        d.builderCorrelationLocalVariables?.let {
            builder.correlationLocalVariables.putAll(d.builderCorrelationLocalVariables)
        }
        d.builderPayloadProcessInstanceVariables?.let {
            builder.payloadProcessInstanceVariables.putAll(d.builderPayloadProcessInstanceVariables)
        }
        d.builderPayloadProcessInstanceVariablesLocal?.let {
            builder.payloadProcessInstanceVariablesLocal.putAll(d.builderPayloadProcessInstanceVariablesLocal)
        }

        d.builderIsExclusiveCorrelation.let{ value ->
            (builder::class.memberProperties.find { it.name == "isExclusiveCorrelation" } as KMutableProperty1<Any, Boolean>)
                    .apply { isAccessible = true }.setter.call(builder, value)
        }

        d.builderIsTenantIdSet.let { value ->
            (builder::class.memberProperties.find { it.name == "isTenantIdSet" } as KMutableProperty1<Any, Boolean>)
                    .apply { isAccessible = true }.setter.call(builder, value)
        }

        CorrelateMessageCmd(builder, d.collectVariables, d.deserializeVariableValues, d.startMessageOnly).execute(builder.commandContext)

    }

    override fun newConfiguration(canonicalString: String): BufferedMessageJobHandlerConfiguration {
        val myClass = Spin.JSON(canonicalString).mapTo(BufferedMessageConfiguration::class.java)
        return BufferedMessageJobHandlerConfiguration(myClass)
    }

    override fun onDelete(configuration: BufferedMessageJobHandlerConfiguration?, jobEntity: JobEntity?) {
    }
}

