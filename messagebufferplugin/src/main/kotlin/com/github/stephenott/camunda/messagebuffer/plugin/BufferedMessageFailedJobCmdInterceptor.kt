package com.github.stephenott.camunda.messagebuffer.plugin

import org.camunda.bpm.engine.impl.cmd.JobRetryCmd
import org.camunda.bpm.engine.impl.interceptor.Command
import org.camunda.bpm.engine.impl.interceptor.CommandInterceptor
import org.camunda.bpm.engine.impl.jobexecutor.FailedJobListener

class BufferedMessageFailedJobCmdInterceptor : CommandInterceptor() {
    override fun <T : Any?> execute(command: Command<T>?): T {
        if (command is JobRetryCmd){
            println("dog")
        }
        if (command is FailedJobListener){
            println("cat")
        }
        return next.execute(command)
    }
}