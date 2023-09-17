package com.xxl.job.core.biz;

import com.xxl.job.common.model.ReturnT;
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
  ReturnT<String> beat();

  /**
   * idle beat
   *
   * @param idleBeatParam
   * @return
   */
  ReturnT<String> idleBeat(IdleBeatParam idleBeatParam);

  /**
   * run
   * @param triggerParam
   * @return
   */
  ReturnT<String> run(TriggerParam triggerParam);

  /**
   * kill
   * @param killParam
   * @return
   */
  ReturnT<String> kill(KillParam killParam);

  /**
   * log
   * @param logParam
   * @return
   */
  ReturnT<LogResult> log(LogParam logParam);

}
