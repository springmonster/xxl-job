package com.xxl.job.admin.core.route.strategy;

import com.xxl.job.admin.core.route.ExecutorRouter;
import com.xxl.job.admin.core.scheduler.XxlJobScheduler;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.common.model.Response;
import com.xxl.job.core.biz.ExecutorBiz;
import com.xxl.job.core.biz.model.TriggerParam;
import java.util.List;

/**
 * Created by xuxueli on 17/3/10.
 */
public class ExecutorRouteFailover extends ExecutorRouter {

  @Override
  public Response<String> route(TriggerParam triggerParam, List<String> addressList) {

    StringBuffer beatResultSB = new StringBuffer();
    for (String address : addressList) {
      // beat
      Response<String> beatResult = null;
      try {
        ExecutorBiz executorBiz = XxlJobScheduler.getExecutorBiz(address);
        beatResult = executorBiz.beat();
      } catch (Exception e) {
        logger.error(e.getMessage(), e);
        beatResult = new Response<String>(Response.FAIL_CODE, "" + e);
      }
      beatResultSB.append((beatResultSB.length() > 0) ? "<br><br>" : "")
          .append(I18nUtil.getString("jobconf_beat") + "：")
          .append("<br>address：").append(address)
          .append("<br>code：").append(beatResult.getCode())
          .append("<br>msg：").append(beatResult.getMsg());

      // beat success
      if (beatResult.getCode() == Response.SUCCESS_CODE) {

        beatResult.setMsg(beatResultSB.toString());
        beatResult.setContent(address);
        return beatResult;
      }
    }
    return new Response<String>(Response.FAIL_CODE, beatResultSB.toString());

  }
}
