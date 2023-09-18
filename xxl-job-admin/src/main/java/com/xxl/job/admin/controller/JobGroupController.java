package com.xxl.job.admin.controller;

import com.xxl.job.admin.controller.annotation.PermissionLimit;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobRegistry;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.admin.dao.XxlJobInfoDao;
import com.xxl.job.admin.dao.XxlJobRegistryDao;
import com.xxl.job.common.model.Response;
import com.xxl.job.core.enums.RegistryConfig;
import com.xxl.job.core.enums.RegistryConfig.RegisterType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * job group controller
 * @author xuxueli 2016-10-02 20:52:56
 */
@Controller
@RequestMapping("/jobgroup")
public class JobGroupController {

  @Resource
  public XxlJobInfoDao xxlJobInfoDao;
  @Resource
  public XxlJobGroupDao xxlJobGroupDao;
  @Resource
  private XxlJobRegistryDao xxlJobRegistryDao;

  @RequestMapping
  @PermissionLimit(adminUser = true)
  public String index(Model model) {
    return "jobgroup/jobgroup.index";
  }

  @RequestMapping("/pageList")
  @ResponseBody
  @PermissionLimit(adminUser = true)
  public Map<String, Object> pageList(HttpServletRequest request,
      @RequestParam(required = false, defaultValue = "0") int start,
      @RequestParam(required = false, defaultValue = "10") int length,
      String appname, String title) {

    // page query
    List<XxlJobGroup> list = xxlJobGroupDao.pageList(start, length, appname, title);
    int list_count = xxlJobGroupDao.pageListCount(start, length, appname, title);

    // package result
    Map<String, Object> maps = new HashMap<String, Object>();
    maps.put("recordsTotal", list_count);    // 总记录数
    maps.put("recordsFiltered", list_count);  // 过滤后的总记录数
    maps.put("data", list);            // 分页列表
    return maps;
  }

  @RequestMapping("/save")
  @ResponseBody
  @PermissionLimit(adminUser = true)
  public Response<String> save(XxlJobGroup xxlJobGroup) {

    // valid
    if (xxlJobGroup.getAppName() == null || xxlJobGroup.getAppName().trim().length() == 0) {
      return new Response<String>(500, (I18nUtil.getString("system_please_input") + "AppName"));
    }
    if (xxlJobGroup.getAppName().length() < 4 || xxlJobGroup.getAppName().length() > 64) {
      return new Response<String>(500, I18nUtil.getString("jobgroup_field_appname_length"));
    }
    if (xxlJobGroup.getAppName().contains(">") || xxlJobGroup.getAppName().contains("<")) {
      return new Response<String>(500, "AppName" + I18nUtil.getString("system_unvalid"));
    }
    if (xxlJobGroup.getTitle() == null || xxlJobGroup.getTitle().trim().length() == 0) {
      return new Response<String>(500,
          (I18nUtil.getString("system_please_input") + I18nUtil.getString("jobgroup_field_title")));
    }
    if (xxlJobGroup.getTitle().contains(">") || xxlJobGroup.getTitle().contains("<")) {
      return new Response<String>(500,
          I18nUtil.getString("jobgroup_field_title") + I18nUtil.getString("system_unvalid"));
    }
    if (xxlJobGroup.getAddressType() != 0) {
      if (xxlJobGroup.getAddressList() == null
          || xxlJobGroup.getAddressList().trim().length() == 0) {
        return new Response<String>(500, I18nUtil.getString("jobgroup_field_addressType_limit"));
      }
      if (xxlJobGroup.getAddressList().contains(">") || xxlJobGroup.getAddressList()
          .contains("<")) {
        return new Response<String>(500,
            I18nUtil.getString("jobgroup_field_registryList") + I18nUtil.getString(
                "system_unvalid"));
      }

      String[] addresss = xxlJobGroup.getAddressList().split(",");
      for (String item : addresss) {
        if (item == null || item.trim().length() == 0) {
          return new Response<String>(500,
              I18nUtil.getString("jobgroup_field_registryList_unvalid"));
        }
      }
    }

    // process
    xxlJobGroup.setUpdateTime(new Date());

    int ret = xxlJobGroupDao.save(xxlJobGroup);
    return (ret > 0) ? Response.SUCCESS : Response.FAIL;
  }

  @RequestMapping("/update")
  @ResponseBody
  @PermissionLimit(adminUser = true)
  public Response<String> update(XxlJobGroup xxlJobGroup) {
    // valid
    if (xxlJobGroup.getAppName() == null || xxlJobGroup.getAppName().trim().length() == 0) {
      return new Response<String>(500, (I18nUtil.getString("system_please_input") + "AppName"));
    }
    if (xxlJobGroup.getAppName().length() < 4 || xxlJobGroup.getAppName().length() > 64) {
      return new Response<String>(500, I18nUtil.getString("jobgroup_field_appname_length"));
    }
    if (xxlJobGroup.getTitle() == null || xxlJobGroup.getTitle().trim().length() == 0) {
      return new Response<String>(500,
          (I18nUtil.getString("system_please_input") + I18nUtil.getString("jobgroup_field_title")));
    }
    if (xxlJobGroup.getAddressType() == 0) {
      // 0=自动注册
      List<String> registryList = findRegistryByAppName(xxlJobGroup.getAppName());
      String addressListStr = null;
      if (registryList != null && !registryList.isEmpty()) {
        Collections.sort(registryList);
        addressListStr = "";
        for (String item : registryList) {
          addressListStr += item + ",";
        }
        addressListStr = addressListStr.substring(0, addressListStr.length() - 1);
      }
      xxlJobGroup.setAddressList(addressListStr);
    } else {
      // 1=手动录入
      if (xxlJobGroup.getAddressList() == null
          || xxlJobGroup.getAddressList().trim().length() == 0) {
        return new Response<String>(500, I18nUtil.getString("jobgroup_field_addressType_limit"));
      }
      String[] addresss = xxlJobGroup.getAddressList().split(",");
      for (String item : addresss) {
        if (item == null || item.trim().length() == 0) {
          return new Response<String>(500,
              I18nUtil.getString("jobgroup_field_registryList_unvalid"));
        }
      }
    }

    // process
    xxlJobGroup.setUpdateTime(new Date());

    int ret = xxlJobGroupDao.update(xxlJobGroup);
    return (ret > 0) ? Response.SUCCESS : Response.FAIL;
  }

  private List<String> findRegistryByAppName(String appnameParam) {
    HashMap<String, List<String>> appAddressMap = new HashMap<String, List<String>>();
    List<XxlJobRegistry> list = xxlJobRegistryDao.findAll(RegistryConfig.DEAD_TIMEOUT, new Date());
    if (list != null) {
      for (XxlJobRegistry item : list) {
        if (RegisterType.EXECUTOR.name().equals(item.getRegistryGroup())) {
          String appname = item.getRegistryKey();
          List<String> registryList = appAddressMap.get(appname);
          if (registryList == null) {
            registryList = new ArrayList<String>();
          }

          if (!registryList.contains(item.getRegistryValue())) {
            registryList.add(item.getRegistryValue());
          }
          appAddressMap.put(appname, registryList);
        }
      }
    }
    return appAddressMap.get(appnameParam);
  }

  @RequestMapping("/remove")
  @ResponseBody
  @PermissionLimit(adminUser = true)
  public Response<String> remove(int id) {

    // valid
    int count = xxlJobInfoDao.pageListCount(0, 10, id, -1, null, null, null);
    if (count > 0) {
      return new Response<String>(500, I18nUtil.getString("jobgroup_del_limit_0"));
    }

    List<XxlJobGroup> allList = xxlJobGroupDao.findAll();
    if (allList.size() == 1) {
      return new Response<String>(500, I18nUtil.getString("jobgroup_del_limit_1"));
    }

    int ret = xxlJobGroupDao.remove(id);
    return (ret > 0) ? Response.SUCCESS : Response.FAIL;
  }

  @RequestMapping("/loadById")
  @ResponseBody
  @PermissionLimit(adminUser = true)
  public Response<XxlJobGroup> loadById(int id) {
    XxlJobGroup jobGroup = xxlJobGroupDao.load(id);
    return jobGroup != null ? new Response<XxlJobGroup>(jobGroup)
        : new Response<XxlJobGroup>(Response.FAIL_CODE, null);
  }

}
