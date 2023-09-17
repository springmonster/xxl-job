package com.xxl.job.admin.controller;

import com.xxl.job.admin.controller.annotation.PermissionLimit;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobUser;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.admin.dao.XxlJobUserDao;
import com.xxl.job.admin.service.LoginService;
import com.xxl.job.common.model.ReturnT;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author xuxueli 2019-05-04 16:39:50
 */
@Controller
@RequestMapping("/user")
public class UserController {

  @Resource
  private XxlJobUserDao xxlJobUserDao;
  @Resource
  private XxlJobGroupDao xxlJobGroupDao;

  @RequestMapping
  @PermissionLimit(adminUser = true)
  public String index(Model model) {

    // 执行器列表
    List<XxlJobGroup> groupList = xxlJobGroupDao.findAll();
    model.addAttribute("groupList", groupList);

    return "user/user.index";
  }

  @RequestMapping("/pageList")
  @ResponseBody
  @PermissionLimit(adminUser = true)
  public Map<String, Object> pageList(@RequestParam(required = false, defaultValue = "0") int start,
      @RequestParam(required = false, defaultValue = "10") int length,
      String username, int role) {

    // page list
    List<XxlJobUser> list = xxlJobUserDao.pageList(start, length, username, role);
    int list_count = xxlJobUserDao.pageListCount(start, length, username, role);

    // filter
    if (list != null && !list.isEmpty()) {
      for (XxlJobUser item : list) {
        item.setPassword(null);
      }
    }

    // package result
    Map<String, Object> maps = new HashMap<>();
    maps.put("recordsTotal", list_count);    // 总记录数
    maps.put("recordsFiltered", list_count);  // 过滤后的总记录数
    maps.put("data", list);            // 分页列表
    return maps;
  }

  @RequestMapping("/add")
  @ResponseBody
  @PermissionLimit(adminUser = true)
  public ReturnT<String> add(XxlJobUser xxlJobUser) {

    // valid username
    // 检查用户名是否为空
    if (!StringUtils.hasText(xxlJobUser.getUsername())) {
      return new ReturnT<>(ReturnT.FAIL_CODE,
          I18nUtil.getString("system_please_input") + I18nUtil.getString("user_username"));
    }

    xxlJobUser.setUsername(xxlJobUser.getUsername().trim());

    // 检查用户名长度是否符合要求
    if (!(xxlJobUser.getUsername().length() >= 4 && xxlJobUser.getUsername().length() <= 20)) {
      return new ReturnT<>(ReturnT.FAIL_CODE,
          I18nUtil.getString("system_length_limit") + "[4-20]");
    }

    // valid password
    if (!StringUtils.hasText(xxlJobUser.getPassword())) {
      return new ReturnT<>(ReturnT.FAIL_CODE,
          I18nUtil.getString("system_please_input") + I18nUtil.getString("user_password"));
    }

    xxlJobUser.setPassword(xxlJobUser.getPassword().trim());

    // 检查密码长度是否符合要求
    if (!(xxlJobUser.getPassword().length() >= 4 && xxlJobUser.getPassword().length() <= 20)) {
      return new ReturnT<>(ReturnT.FAIL_CODE,
          I18nUtil.getString("system_length_limit") + "[4-20]");
    }

    // md5 password
    // TODO: 2023/9/17 这里需要改进，只使用md5加密是不安全的，需要加盐，或者使用其他加密方式
    xxlJobUser.setPassword(DigestUtils.md5DigestAsHex(xxlJobUser.getPassword().getBytes()));

    // check repeat
    // 检查用户名是否已存在 select * from xxl_job_user where username = #{username}
    XxlJobUser existUser = xxlJobUserDao.loadByUserName(xxlJobUser.getUsername());

    if (existUser != null) {
      return new ReturnT<>(ReturnT.FAIL_CODE, I18nUtil.getString("user_username_repeat"));
    }

    // write
    xxlJobUserDao.save(xxlJobUser);

    return ReturnT.SUCCESS;
  }

  @RequestMapping("/update")
  @ResponseBody
  @PermissionLimit(adminUser = true)
  public ReturnT<String> update(HttpServletRequest request, XxlJobUser xxlJobUser) {

    // avoid opt login seft
    XxlJobUser loginUser = (XxlJobUser) request.getAttribute(LoginService.LOGIN_IDENTITY_KEY);
    if (loginUser.getUsername().equals(xxlJobUser.getUsername())) {
      return new ReturnT<>(ReturnT.FAIL.getCode(),
          I18nUtil.getString("user_update_login_user_limit"));
    }

    // valid password
    if (StringUtils.hasText(xxlJobUser.getPassword())) {
      xxlJobUser.setPassword(xxlJobUser.getPassword().trim());
      if (!(xxlJobUser.getPassword().length() >= 4 && xxlJobUser.getPassword().length() <= 20)) {
        return new ReturnT<>(ReturnT.FAIL_CODE,
            I18nUtil.getString("system_length_limit") + "[4-20]");
      }
      // md5 password
      xxlJobUser.setPassword(DigestUtils.md5DigestAsHex(xxlJobUser.getPassword().getBytes()));
    } else {
      xxlJobUser.setPassword(null);
    }

    // write
    xxlJobUserDao.update(xxlJobUser);
    return ReturnT.SUCCESS;
  }

  @RequestMapping("/remove")
  @ResponseBody
  @PermissionLimit(adminUser = true)
  public ReturnT<String> remove(HttpServletRequest request, int id) {

    // avoid opt login seft
    XxlJobUser loginUser = (XxlJobUser) request.getAttribute(LoginService.LOGIN_IDENTITY_KEY);
    if (loginUser.getId() == id) {
      return new ReturnT<>(ReturnT.FAIL.getCode(),
          I18nUtil.getString("user_update_login_user_limit"));
    }

    xxlJobUserDao.delete(id);
    return ReturnT.SUCCESS;
  }

  @RequestMapping("/updatePwd")
  @ResponseBody
  public ReturnT<String> updatePwd(HttpServletRequest request, String password) {

    // valid password
    if (password == null || password.trim().isEmpty()) {
      return new ReturnT<>(ReturnT.FAIL.getCode(), "密码不可为空");
    }

    password = password.trim();

    if (!(password.length() >= 4 && password.length() <= 20)) {
      return new ReturnT<>(ReturnT.FAIL_CODE,
          I18nUtil.getString("system_length_limit") + "[4-20]");
    }

    // md5 password
    String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());

    // update pwd
    XxlJobUser loginUser = (XxlJobUser) request.getAttribute(LoginService.LOGIN_IDENTITY_KEY);

    // do write
    XxlJobUser existUser = xxlJobUserDao.loadByUserName(loginUser.getUsername());
    existUser.setPassword(md5Password);
    xxlJobUserDao.update(existUser);

    return ReturnT.SUCCESS;
  }

}
