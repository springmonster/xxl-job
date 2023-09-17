package com.xxl.job.common.biz;

import com.xxl.job.common.model.HandleCallbackParam;
import com.xxl.job.common.model.RegistryParam;
import com.xxl.job.common.model.Response;
import java.util.List;

/**
 * @author xuxueli 2017-07-27 21:52:49
 */
public interface AdminBiz {

  // ---------------------- callback ----------------------

  /**
   * callback
   *
   * @param callbackParamList
   * @return
   */
  Response<String> callback(List<HandleCallbackParam> callbackParamList);

  // ---------------------- registry ----------------------

  /**
   * registry
   *
   * @param registryParam
   * @return
   */
  Response<String> registry(RegistryParam registryParam);

  /**
   * registry remove
   *
   * @param registryParam
   * @return
   */
  Response<String> registryRemove(RegistryParam registryParam);

  // ---------------------- biz (custome) ----------------------
  // group、job ... manage

}
