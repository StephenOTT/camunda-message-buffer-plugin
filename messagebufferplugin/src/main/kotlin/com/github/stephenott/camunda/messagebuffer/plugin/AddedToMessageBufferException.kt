package com.github.stephenott.camunda.messagebuffer.plugin

import org.camunda.bpm.engine.ProcessEngineException

/**
 * Used to manage response from API usage.  Main usage is when Messages are sent through the rest-api
 */
class AddedToMessageBufferException: ProcessEngineException("Message was added to message buffer") {
}