package com.xxl.job.admin.service.impl;

import com.xxl.job.admin.core.thread.JobCompleteHelper;
import com.xxl.job.admin.core.thread.JobRegistryHelper;
import com.xxl.job.common.model.Response;
import com.xxl.job.common.biz.AdminBiz;
import com.xxl.job.common.model.HandleCallbackParam;
import com.xxl.job.common.model.RegistryParam;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @author xuxueli 2017-07-27 21:54:20
 */
@Service
public class AdminBizServerImpl implements AdminBiz {


  @Override
  public Response<String> callback(List<HandleCallbackParam> callbackParamList) {
    return JobCompleteHelper.getInstance().callback(callbackParamList);
  }

  @Override
  public Response<String> registry(RegistryParam registryParam) {
    return JobRegistryHelper.getInstance().registry(registryParam);
  }

  @Override
  public Response<String> registryRemove(RegistryParam registryParam) {
    return JobRegistryHelper.getInstance().registryRemove(registryParam);
  }

}
