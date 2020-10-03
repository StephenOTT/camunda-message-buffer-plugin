package com.github.stephenott.camunda.messagebuffer.plugin

import org.camunda.bpm.engine.MismatchingMessageCorrelationException
import org.camunda.bpm.engine.impl.cmd.AbstractCorrelateMessageCmd
import org.camunda.bpm.engine.impl.cmd.CorrelateAllMessageCmd
import org.camunda.bpm.engine.impl.cmd.CorrelateMessageCmd
import org.camunda.bpm.engine.impl.interceptor.Command
import org.camunda.bpm.engine.impl.interceptor.CommandInterceptor
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration
import org.camunda.bpm.engine.impl.jobexecutor.TimerDeclarationImpl
import org.camunda.bpm.engine.impl.jobexecutor.TimerDeclarationType
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionEntity
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity
import org.camunda.bpm.engine.impl.util.ParseUtil

class MessageCommandInterceptor(
        val maxAttempts: Int
) : CommandInterceptor() {

    override fun <T> execute(command: Command<T>): T {
        // If it is not a CorrelateMessageCmd that just go to next:
        if (command !is AbstractCorrelateMessageCmd) {
            return next.execute(command)
        }

        return if (command is CorrelateMessageCmd || command is CorrelateAllMessageCmd) {
            kotlin.runCatching {

                // Run the comment
                next.execute(command)
            }.getOrElse {
                //If the command returns a specific exception that occurs when correlation was not possible:
                if (it is MismatchingMessageCorrelationException) {
                    next.execute { cc ->
                        if (cc.currentJob == null) {
                            val myClass = (command as CorrelateMessageCmd).convertForPersistence()

                            val job = MessageEntity()
                            val config = BufferedMessageJobHandler.BufferedMessageJobHandlerConfiguration(myClass)
                            job.jobHandlerType = "buffered-message"
                            job.jobHandlerConfigurationRaw = config.toCanonicalString()

                            cc.jobManager.send(job)
                            null //@TODO review this line
                        } else {
                            //otherwise throw error on job
                            throw AddedToMessageBufferException()
                        }
                    }
                } else {
                    // If another type of exception was returned then throw the error
                    throw it
                }
            }

        } else {
            throw IllegalStateException("Unexpected command (${command::class.qualifiedName}) was received in MessageCommandInterceptor")
        }
    }
}