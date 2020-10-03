package com.github.stephenott.camunda.messagebuffer.plugin

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.camunda.bpm.engine.impl.MessageCorrelationBuilderImpl
import org.camunda.bpm.engine.impl.cmd.AbstractCorrelateMessageCmd
import org.camunda.bpm.engine.impl.cmd.CorrelateMessageCmd
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.impl.VariableMapImpl
import java.io.Serializable
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

data class BufferedMessage(
        val messageCommand: AbstractCorrelateMessageCmd,
        var attempts: Int
) : Serializable {
    init {
        check(attempts >= 0) { "attempts cannot be less than zero." }
    }
}

fun CorrelateMessageCmd.convertForPersistence(): BufferedMessageConfiguration {
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
            builderPayloadProcessInstanceVariablesLocal = builderImpl.payloadProcessInstanceVariablesLocal)
}


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
        val builderPayloadProcessInstanceVariablesLocal: Map<String, Any?>?
)


class VariableMapSerializer: StdSerializer<VariableMap>(VariableMap::class.java){
    override fun serialize(value: VariableMap, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()

        value.forEach { k, v ->
            gen.writeFieldName(k)
                gen.writeStartObject()
                    gen.writeStringField("@class", v::class.java.canonicalName)
                    gen.writeObjectField("value", v)
                gen.writeEndObject()
        }
        gen.writeEndObject()
    }
}

class VariableMapDeserializer: StdDeserializer<VariableMap>(VariableMap::class.java){
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): VariableMap {
        val tree = p.readValueAsTree<JsonNode>()
        val vMap = VariableMapImpl()
        tree.elements().forEachRemaining {
            val inner = p.readValueAsTree<JsonNode>()
            vMap[p.currentName] = it.get("value").traverse(p.codec).readValueAs(Class.forName(it.get("@class").textValue()))
        }
        return vMap
    }
}