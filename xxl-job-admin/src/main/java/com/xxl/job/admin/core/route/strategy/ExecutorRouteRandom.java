package com.xxl.job.admin.core.route.strategy;

import com.xxl.job.admin.core.route.ExecutorRouter;
import com.xxl.job.common.model.Response;
import com.xxl.job.common.biz.model.TriggerParam;
import java.util.List;
import java.util.Random;

/**
 * Created by xuxueli on 17/3/10.
 */
public class ExecutorRouteRandom extends ExecutorRouter {

  private static Random localRandom = new Random();

  @Override
  public Response<String> route(TriggerParam triggerParam, List<String> addressList) {
    String address = addressList.get(localRandom.nextInt(addressList.size()));
    return new Response<String>(address);
  }

}
