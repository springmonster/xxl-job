package com.xxl.job.admin.service;


import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.common.model.Response;
import java.util.Date;
import java.util.Map;

/**
 * core job action for xxl-job
 *
 * @author xuxueli 2016-5-28 15:30:33
 */
public interface XxlJobService {

  /**
   * page list
   *
   * @param start
   * @param length
   * @param jobGroup
   * @param jobDesc
   * @param executorHandler
   * @param author
   * @return
   */
  Map<String, Object> pageList(int start, int length, int jobGroup, int triggerStatus,
      String jobDesc, String executorHandler, String author);

  /**
   * add job
   *
   * @param jobInfo
   * @return
   */
  Response<String> add(XxlJobInfo jobInfo);

  /**
   * update job
   *
   * @param jobInfo
   * @return
   */
  Response<String> update(XxlJobInfo jobInfo);

  /**
   * remove job
   * 	 *
   * @param id
   * @return
   */
  Response<String> remove(int id);

  /**
   * start job
   *
   * @param id
   * @return
   */
  Response<String> start(int id);

  /**
   * stop job
   *
   * @param id
   * @return
   */
  Response<String> stop(int id);

  /**
   * dashboard info
   *
   * @return
   */
  Map<String, Object> dashboardInfo();

  /**
   * chart info
   *
   * @param startDate
   * @param endDate
   * @return
   */
  Response<Map<String, Object>> chartInfo(Date startDate, Date endDate);

}
