package com.xxl.job.core.biz.client;

import com.xxl.job.common.biz.AdminBiz;
import com.xxl.job.common.model.HandleCallbackParam;
import com.xxl.job.common.model.RegistryParam;
import com.xxl.job.common.model.Response;
import com.xxl.job.core.util.XxlJobRemotingUtil;
import java.util.List;

/**
 * admin api test
 *
 * @author xuxueli 2017-07-28 22:14:52
 */
public class AdminBizClientImpl implements AdminBiz {

  public AdminBizClientImpl() {
  }

  public AdminBizClientImpl(String addressUrl, String accessToken) {
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
  @SuppressWarnings("unchecked")
  public Response<String> callback(List<HandleCallbackParam> callbackParamList) {
    return XxlJobRemotingUtil.postBody(addressUrl + "api/callback", accessToken, timeout,
        callbackParamList, String.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Response<String> registry(RegistryParam registryParam) {
    return XxlJobRemotingUtil.postBody(addressUrl + "api/registry", accessToken, timeout,
        registryParam, String.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Response<String> registryRemove(RegistryParam registryParam) {
    return XxlJobRemotingUtil.postBody(addressUrl + "api/registryRemove", accessToken, timeout,
        registryParam, String.class);
  }

}
