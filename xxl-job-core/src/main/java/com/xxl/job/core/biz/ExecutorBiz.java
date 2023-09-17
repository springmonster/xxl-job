package com.xxl.job.core.biz;

import com.xxl.job.common.model.Response;
import com.xxl.job.core.biz.model.IdleBeatParam;
import com.xxl.job.core.biz.model.KillParam;
import com.xxl.job.core.biz.model.LogParam;
import com.xxl.job.core.biz.model.LogResult;
import com.xxl.job.core.biz.model.TriggerParam;

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
