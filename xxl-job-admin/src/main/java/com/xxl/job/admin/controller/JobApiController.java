package com.xxl.job.admin.controller;

import com.xxl.job.admin.controller.annotation.PermissionLimit;
import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.common.biz.AdminBiz;
import com.xxl.job.common.model.HandleCallbackParam;
import com.xxl.job.common.model.RegistryParam;
import com.xxl.job.common.model.Response;
import com.xxl.job.core.util.GsonTool;
import com.xxl.job.core.util.XxlJobRemotingUtil;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by xuxueli on 17/5/10.
 */
@Controller
@RequestMapping("/api")
public class JobApiController {

  @Resource
  private AdminBiz adminBiz;

  /**
   * api
   *
   * @param uri
   * @param data
   * @return
   */
  @RequestMapping("/{uri}")
  @ResponseBody
  @PermissionLimit(limit = false)
  public Response<String> api(HttpServletRequest request, @PathVariable("uri") String uri,
      @RequestBody(required = false) String data) {

    // valid
    if (!"POST".equalsIgnoreCase(request.getMethod())) {
      return new Response<String>(Response.FAIL_CODE, "invalid request, HttpMethod not support.");
    }
    if (uri == null || uri.trim().length() == 0) {
      return new Response<String>(Response.FAIL_CODE, "invalid request, uri-mapping empty.");
    }
    if (XxlJobAdminConfig.getAdminConfig().getAccessToken() != null
        && XxlJobAdminConfig.getAdminConfig().getAccessToken().trim().length() > 0
        && !XxlJobAdminConfig.getAdminConfig().getAccessToken()
        .equals(request.getHeader(XxlJobRemotingUtil.XXL_JOB_ACCESS_TOKEN))) {
      return new Response<String>(Response.FAIL_CODE, "The access token is wrong.");
    }

    // services mapping
    switch (uri) {
      case "callback":
        List<HandleCallbackParam> callbackParamList = GsonTool.fromJson(data, List.class,
            HandleCallbackParam.class);
        return adminBiz.callback(callbackParamList);
      case "registry": {
        // kuanghc client register
        RegistryParam registryParam = GsonTool.fromJson(data, RegistryParam.class);
        return adminBiz.registry(registryParam);
      }
      case "registryRemove": {
        // kuanghc client remove
        RegistryParam registryParam = GsonTool.fromJson(data, RegistryParam.class);
        return adminBiz.registryRemove(registryParam);
      }
      default:
        return new Response<>(Response.FAIL_CODE,
            "invalid request, uri-mapping(" + uri + ") not found.");
    }
  }
}
