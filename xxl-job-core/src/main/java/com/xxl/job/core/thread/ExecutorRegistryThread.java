package com.xxl.job.core.thread;

import com.xxl.job.common.biz.AdminBiz;
import com.xxl.job.common.model.RegistryParam;
import com.xxl.job.common.model.Response;
import com.xxl.job.core.enums.RegistryConfig;
import com.xxl.job.core.executor.XxlJobExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xuxueli on 17/3/2.
 */
public class ExecutorRegistryThread {

  private static final Logger logger = LoggerFactory.getLogger(ExecutorRegistryThread.class);

  private static ExecutorRegistryThread instance = new ExecutorRegistryThread();

  public static ExecutorRegistryThread getInstance() {
    return instance;
  }

  private Thread registryThread;
  private volatile boolean toStop = false;

  public void start(final String appname, final String address) {

    // valid
    if (appname == null || appname.trim().length() == 0) {
      logger.warn(">>>>>>>>>>> xxl-job, executor registry config fail, appname is null.");
      return;
    }
    if (XxlJobExecutor.getAdminBizList() == null) {
      logger.warn(">>>>>>>>>>> xxl-job, executor registry config fail, adminAddresses is null.");
      return;
    }

    registryThread = new Thread(new Runnable() {
      @Override
      public void run() {

        // registry
        while (!toStop) {
          try {
            RegistryParam registryParam = new RegistryParam(
                RegistryConfig.RegistType.EXECUTOR.name(), appname, address);
            for (AdminBiz adminBiz : XxlJobExecutor.getAdminBizList()) {
              try {
                Response<String> registryResult = adminBiz.registry(registryParam);
                if (registryResult != null && Response.SUCCESS_CODE == registryResult.getCode()) {
                  registryResult = Response.SUCCESS;
                  logger.debug(
                      ">>>>>>>>>>> xxl-job registry success, registryParam:{}, registryResult:{}",
                      registryParam, registryResult);
                  break;
                } else {
                  logger.info(
                      ">>>>>>>>>>> xxl-job registry fail, registryParam:{}, registryResult:{}",
                      registryParam, registryResult);
                }
              } catch (Exception e) {
                logger.info(">>>>>>>>>>> xxl-job registry error, registryParam:{}", registryParam,
                    e);
              }

            }
          } catch (Exception e) {
            if (!toStop) {
              logger.error(e.getMessage(), e);
            }

          }

          try {
            if (!toStop) {
              TimeUnit.SECONDS.sleep(RegistryConfig.BEAT_TIMEOUT);
            }
          } catch (InterruptedException e) {
            if (!toStop) {
              logger.warn(">>>>>>>>>>> xxl-job, executor registry thread interrupted, error msg:{}",
                  e.getMessage());
            }
          }
        }

        // registry remove
        try {
          RegistryParam registryParam = new RegistryParam(RegistryConfig.RegistType.EXECUTOR.name(),
              appname, address);
          for (AdminBiz adminBiz : XxlJobExecutor.getAdminBizList()) {
            try {
              Response<String> registryResult = adminBiz.registryRemove(registryParam);
              if (registryResult != null && Response.SUCCESS_CODE == registryResult.getCode()) {
                registryResult = Response.SUCCESS;
                logger.info(
                    ">>>>>>>>>>> xxl-job registry-remove success, registryParam:{}, registryResult:{}",
                    registryParam, registryResult);
                break;
              } else {
                logger.info(
                    ">>>>>>>>>>> xxl-job registry-remove fail, registryParam:{}, registryResult:{}",
                    registryParam, registryResult);
              }
            } catch (Exception e) {
              if (!toStop) {
                logger.info(">>>>>>>>>>> xxl-job registry-remove error, registryParam:{}",
                    registryParam, e);
              }

            }

          }
        } catch (Exception e) {
          if (!toStop) {
            logger.error(e.getMessage(), e);
          }
        }
        logger.info(">>>>>>>>>>> xxl-job, executor registry thread destroy.");

      }
    });
    registryThread.setDaemon(true);
    registryThread.setName("xxl-job, executor ExecutorRegistryThread");
    registryThread.start();
  }

  public void toStop() {
    toStop = true;

    // interrupt and wait
    if (registryThread != null) {
      registryThread.interrupt();
      try {
        registryThread.join();
      } catch (InterruptedException e) {
        logger.error(e.getMessage(), e);
      }
    }
  }
}
