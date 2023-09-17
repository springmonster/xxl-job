package com.xxl.job.admin.core.route.strategy;

import com.xxl.job.admin.core.route.ExecutorRouter;
import com.xxl.job.common.model.Response;
import com.xxl.job.core.biz.model.TriggerParam;
import java.util.List;

/**
 * Created by xuxueli on 17/3/10.
 */
public class ExecutorRouteFirst extends ExecutorRouter {

  @Override
  public Response<String> route(TriggerParam triggerParam, List<String> addressList) {
    return new Response<String>(addressList.get(0));
  }

}
