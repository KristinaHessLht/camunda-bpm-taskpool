package io.holunda.camunda.taskpool.example.process.rest

import io.holunda.camunda.taskpool.example.process.process.ProcessApproveRequestBean
import io.holunda.camunda.taskpool.example.process.rest.api.AmendRequestApi
import io.holunda.camunda.taskpool.example.process.rest.model.TaskAmendRequestFormDataDto
import io.holunda.camunda.taskpool.example.process.rest.model.TaskAmendRequestSubmitDataDto
import io.holunda.camunda.taskpool.view.auth.UserService
import io.swagger.annotations.ApiParam
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import javax.validation.Valid

@Controller
@RequestMapping(path = [Rest.REST_PREFIX])
class AmendRequestTaskController(
  private val processApproveRequestBean: ProcessApproveRequestBean,
  private val userService: UserService
) : AmendRequestApi {

  companion object : KLogging()

  override fun loadTaskAmendRequestFormData(
    @ApiParam(value = "Task id.", required = true) @PathVariable("id") id: String,
    @ApiParam(value = "Specifies the id of current user.", required = true) @RequestHeader(value = "X-Current-User-ID", required = true) xCurrentUserID: String
    ): ResponseEntity<TaskAmendRequestFormDataDto> {
    logger.debug { "Loading data for task $id" }
    val (task, approvalRequest) = processApproveRequestBean.loadAmendTaskFormData(id)
    return ResponseEntity.ok(TaskAmendRequestFormDataDto().approvalRequest(approvalRequestDto(approvalRequest)).task(taskDto(task)))
  }

  override fun submitTaskAmendRequestSubmitData(
    @ApiParam(value = "Task id.", required = true) @PathVariable("id") id: String,
    @ApiParam(value = "Specifies the id of current user.", required = true) @RequestHeader(value = "X-Current-User-ID", required = true) xCurrentUserID: String,
    @ApiParam(value = "Payload to be added to the process instance on task completion.") @Valid @RequestBody payload: TaskAmendRequestSubmitDataDto
  ): ResponseEntity<Void> {
    logger.debug { "Submitting data for task $id, $payload" }

    val username = userService.getUser(xCurrentUserID).username

    processApproveRequestBean.amendTask(taskId = id, action = payload.action, request = request(payload.approvalRequest), username = username, comment = payload.comment)
    return ResponseEntity.noContent().build()
  }
}
