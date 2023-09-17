package com.xxl.job.adminbiz;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.xxl.job.common.biz.AdminBiz;
import com.xxl.job.common.model.HandleCallbackParam;
import com.xxl.job.common.model.RegistryParam;
import com.xxl.job.common.model.Response;
import com.xxl.job.core.biz.client.AdminBizClientImpl;
import com.xxl.job.core.context.XxlJobContext;
import com.xxl.job.core.enums.RegistryConfig;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * admin api test
 *
 * @author xuxueli 2017-07-28 22:14:52
 */
public class AdminBizTest {

  // admin-client
  private static final String addressUrl = "http://127.0.0.1:10000/xxl-job-admin/";
  private static final String accessToken = "default_token";


  @Test
  public void callback() {
    AdminBiz adminBiz = new AdminBizClientImpl(addressUrl, accessToken);

    HandleCallbackParam param = new HandleCallbackParam();
    param.setLogId(1);
    param.setHandleCode(XxlJobContext.HANDLE_CODE_SUCCESS);

    List<HandleCallbackParam> callbackParamList = Arrays.asList(param);

    Response<String> response = adminBiz.callback(callbackParamList);

    assertEquals(Response.SUCCESS_CODE, response.getCode());
  }

  /**
   * registry executor
   *
   */
  @Test
  public void registry() {
    AdminBiz adminBiz = new AdminBizClientImpl(addressUrl, accessToken);

    RegistryParam registryParam = new RegistryParam(RegistryConfig.RegistType.EXECUTOR.name(),
        "xxl-job-executor-example-test", "127.0.0.1:9999");
    Response<String> response = adminBiz.registry(registryParam);

    assertEquals(Response.SUCCESS_CODE, response.getCode());
  }

  /**
   * registry executor remove
   *
   */
  @Test
  public void registryRemove() {
    AdminBiz adminBiz = new AdminBizClientImpl(addressUrl, accessToken);

    RegistryParam registryParam = new RegistryParam(RegistryConfig.RegistType.EXECUTOR.name(),
        "xxl-job-executor-example-test", "127.0.0.1:9999");
    Response<String> response = adminBiz.registryRemove(registryParam);

    assertEquals(Response.SUCCESS_CODE, response.getCode());
  }
}
