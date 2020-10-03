package com.github.stephenott.camunda.messagebuffer.plugin

import org.camunda.bpm.engine.ProcessEngineException

class AddedToMessageBufferException: ProcessEngineException("Message was added to message buffer") {
}