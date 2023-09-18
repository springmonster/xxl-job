package com.xxl.job.core.biz.client;

import com.xxl.job.common.model.Response;
import com.xxl.job.core.biz.ExecutorBiz;
import com.xxl.job.core.biz.model.IdleBeatParam;
import com.xxl.job.core.biz.model.KillParam;
import com.xxl.job.core.biz.model.LogParam;
import com.xxl.job.core.biz.model.LogResult;
import com.xxl.job.core.biz.model.TriggerParam;
import com.xxl.job.core.util.XxlJobRemotingUtil;

/**
 * admin api test
 *
 * @author xuxueli 2017-07-28 22:14:52
 */
public class ExecutorBizClient implements ExecutorBiz {

  public ExecutorBizClient() {
  }

  public ExecutorBizClient(String addressUrl, String accessToken) {
    this.addressUrl = addressUrl;
    this.accessToken = accessToken;

    // valid
    if (!this.addressUrl.endsWith("/")) {
      this.addressUrl = this.addressUrl + "/";
    }
  }

  private String addressUrl;
  private String accessToken;
  private int timeout = 3;


  @Override
  public Response<String> beat() {
    return XxlJobRemotingUtil.postBody(addressUrl + "beat", accessToken, timeout, "", String.class);
  }

  @Override
  public Response<String> idleBeat(IdleBeatParam idleBeatParam) {
    return XxlJobRemotingUtil.postBody(addressUrl + "idleBeat", accessToken, timeout, idleBeatParam,
        String.class);
  }

  @Override
  public Response<String> run(TriggerParam triggerParam) {
    // kuanghc admin发送请求，client执行任务
    return XxlJobRemotingUtil.postBody(addressUrl + "run", accessToken, timeout, triggerParam,
        String.class);
  }

  @Override
  public Response<String> kill(KillParam killParam) {
    return XxlJobRemotingUtil.postBody(addressUrl + "kill", accessToken, timeout, killParam,
        String.class);
  }

  @Override
  public Response<LogResult> log(LogParam logParam) {
    return XxlJobRemotingUtil.postBody(addressUrl + "log", accessToken, timeout, logParam,
        LogResult.class);
  }

}
