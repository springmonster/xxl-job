package com.xxl.job.common.biz;

import com.xxl.job.common.biz.model.IdleBeatParam;
import com.xxl.job.common.biz.model.KillParam;
import com.xxl.job.common.biz.model.LogParam;
import com.xxl.job.common.biz.model.LogResult;
import com.xxl.job.common.biz.model.TriggerParam;
import com.xxl.job.common.model.Response;

/**
 * Created by xuxueli on 17/3/1.
 */
public interface ExecutorBiz {

  /**
   * beat
   * @return
   */
  Response<String> beat();

  /**
   * idle beat
   *
   * @param idleBeatParam
   * @return
   */
  Response<String> idleBeat(IdleBeatParam idleBeatParam);

  /**
   * run
   * @param triggerParam
   * @return
   */
  Response<String> run(TriggerParam triggerParam);

  /**
   * kill
   * @param killParam
   * @return
   */
  Response<String> kill(KillParam killParam);

  /**
   * log
   * @param logParam
   * @return
   */
  Response<LogResult> log(LogParam logParam);

}
