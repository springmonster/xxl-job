package com.xxl.job.core.biz.impl;

import com.xxl.job.common.model.Response;
import com.xxl.job.common.biz.ExecutorBiz;
import com.xxl.job.common.biz.model.IdleBeatParam;
import com.xxl.job.common.biz.model.KillParam;
import com.xxl.job.common.biz.model.LogParam;
import com.xxl.job.common.biz.model.LogResult;
import com.xxl.job.common.biz.model.TriggerParam;
import com.xxl.job.core.enums.ExecutorBlockStrategyEnum;
import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.glue.GlueFactory;
import com.xxl.job.core.glue.GlueTypeEnum;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.impl.GlueJobHandler;
import com.xxl.job.core.handler.impl.ScriptJobHandler;
import com.xxl.job.core.log.XxlJobFileAppender;
import com.xxl.job.core.thread.JobThread;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xuxueli on 17/3/1.
 */
public class ExecutorBizClientImpl implements ExecutorBiz {

  private static final Logger logger = LoggerFactory.getLogger(ExecutorBizClientImpl.class);

  @Override
  public Response<String> beat() {
    return Response.SUCCESS;
  }

  @Override
  public Response<String> idleBeat(IdleBeatParam idleBeatParam) {

    // isRunningOrHasQueue
    boolean isRunningOrHasQueue = false;
    JobThread jobThread = XxlJobExecutor.loadJobThread(idleBeatParam.getJobId());
    if (jobThread != null && jobThread.isRunningOrHasQueue()) {
      isRunningOrHasQueue = true;
    }

    if (isRunningOrHasQueue) {
      return new Response<String>(Response.FAIL_CODE,
          "job thread is running or has trigger queue.");
    }
    return Response.SUCCESS;
  }

  @Override
  public Response<String> run(TriggerParam triggerParam) {
    // load old：jobHandler + jobThread
    JobThread jobThread = XxlJobExecutor.loadJobThread(triggerParam.getJobId());
    IJobHandler jobHandler = jobThread != null ? jobThread.getHandler() : null;
    String removeOldReason = null;

    // valid：jobHandler + jobThread
    GlueTypeEnum glueTypeEnum = GlueTypeEnum.match(triggerParam.getGlueType());
    if (GlueTypeEnum.BEAN == glueTypeEnum) {

      // new jobhandler
      IJobHandler newJobHandler = XxlJobExecutor.loadJobHandler(triggerParam.getExecutorHandler());

      // valid old jobThread
      if (jobThread != null && jobHandler != newJobHandler) {
        // change handler, need kill old thread
        removeOldReason = "change jobhandler or glue type, and terminate the old job thread.";

        jobThread = null;
        jobHandler = null;
      }

      // valid handler
      if (jobHandler == null) {
        jobHandler = newJobHandler;
        if (jobHandler == null) {
          return new Response<>(Response.FAIL_CODE,
              "job handler [" + triggerParam.getExecutorHandler() + "] not found.");
        }
      }
    } else if (GlueTypeEnum.GLUE_GROOVY == glueTypeEnum) {

      // valid old jobThread
      if (jobThread != null &&
          !(jobThread.getHandler() instanceof GlueJobHandler
              && ((GlueJobHandler) jobThread.getHandler()).getGlueUpdatetime()
              == triggerParam.getGlueUpdatetime())) {
        // change handler or gluesource updated, need kill old thread
        removeOldReason = "change job source or glue type, and terminate the old job thread.";

        jobThread = null;
        jobHandler = null;
      }

      // valid handler
      if (jobHandler == null) {
        try {
          IJobHandler originJobHandler = GlueFactory.getInstance()
              .loadNewInstance(triggerParam.getGlueSource());
          jobHandler = new GlueJobHandler(originJobHandler, triggerParam.getGlueUpdatetime());
        } catch (Exception e) {
          logger.error(e.getMessage(), e);
          return new Response<>(Response.FAIL_CODE, e.getMessage());
        }
      }
    } else if (glueTypeEnum != null && glueTypeEnum.isScript()) {

      // valid old jobThread
      if (jobThread != null &&
          !(jobThread.getHandler() instanceof ScriptJobHandler
              && ((ScriptJobHandler) jobThread.getHandler()).getGlueUpdatetime()
              == triggerParam.getGlueUpdatetime())) {
        // change script or gluesource updated, need kill old thread
        removeOldReason = "change job source or glue type, and terminate the old job thread.";

        jobThread = null;
        jobHandler = null;
      }

      // valid handler
      if (jobHandler == null) {
        jobHandler = new ScriptJobHandler(triggerParam.getJobId(), triggerParam.getGlueUpdatetime(),
            triggerParam.getGlueSource(), GlueTypeEnum.match(triggerParam.getGlueType()));
      }
    } else {
      return new Response<>(Response.FAIL_CODE,
          "glueType[" + triggerParam.getGlueType() + "] is not valid.");
    }

    // executor block strategy
    if (jobThread != null) {
      ExecutorBlockStrategyEnum blockStrategy = ExecutorBlockStrategyEnum.match(
          triggerParam.getExecutorBlockStrategy(), null);
      if (ExecutorBlockStrategyEnum.DISCARD_LATER == blockStrategy) {
        // discard when running
        if (jobThread.isRunningOrHasQueue()) {
          return new Response<>(Response.FAIL_CODE,
              "block strategy effect：" + ExecutorBlockStrategyEnum.DISCARD_LATER.getTitle());
        }
      } else if (ExecutorBlockStrategyEnum.COVER_EARLY == blockStrategy) {
        // kill running jobThread
        if (jobThread.isRunningOrHasQueue()) {
          removeOldReason =
              "block strategy effect：" + ExecutorBlockStrategyEnum.COVER_EARLY.getTitle();

          jobThread = null;
        }
      } else {
        // just queue trigger
      }
    }

    // replace thread (new or exists invalid)
    if (jobThread == null) {
      jobThread = XxlJobExecutor.registJobThread(triggerParam.getJobId(), jobHandler,
          removeOldReason);
    }

    // push data to queue
    return jobThread.pushTriggerQueue(triggerParam);
  }

  @Override
  public Response<String> kill(KillParam killParam) {
    // kill handlerThread, and create new one
    JobThread jobThread = XxlJobExecutor.loadJobThread(killParam.getJobId());
    if (jobThread != null) {
      XxlJobExecutor.removeJobThread(killParam.getJobId(), "scheduling center kill job.");
      return Response.SUCCESS;
    }

    return new Response<>(Response.SUCCESS_CODE, "job thread already killed.");
  }

  @Override
  public Response<LogResult> log(LogParam logParam) {
    // log filename: logPath/yyyy-MM-dd/9999.log
    String logFileName = XxlJobFileAppender.makeLogFileName(new Date(logParam.getLogDateTim()),
        logParam.getLogId());

    LogResult logResult = XxlJobFileAppender.readLog(logFileName, logParam.getFromLineNum());
    return new Response<>(logResult);
  }

}
