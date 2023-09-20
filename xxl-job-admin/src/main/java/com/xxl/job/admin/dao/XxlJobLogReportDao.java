package com.xxl.job.admin.dao;

import com.xxl.job.admin.core.model.XxlJobLogReport;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * job log
 * @author xuxueli 2019-11-22
 */
@Mapper
public interface XxlJobLogReportDao {

  int save(XxlJobLogReport xxlJobLogReport);

  int update(XxlJobLogReport xxlJobLogReport);

  List<XxlJobLogReport> queryLogReport(@Param("triggerDayFrom") Date triggerDayFrom,
      @Param("triggerDayTo") Date triggerDayTo);

  XxlJobLogReport queryLogReportTotal();

}
