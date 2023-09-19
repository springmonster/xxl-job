package com.xxl.job.core.server;

import com.xxl.job.common.model.Response;
import com.xxl.job.common.biz.ExecutorBiz;
import com.xxl.job.core.biz.impl.ExecutorBizClientImpl;
import com.xxl.job.common.biz.model.IdleBeatParam;
import com.xxl.job.common.biz.model.KillParam;
import com.xxl.job.common.biz.model.LogParam;
import com.xxl.job.common.biz.model.TriggerParam;
import com.xxl.job.core.thread.ExecutorRegistryThread;
import com.xxl.job.core.util.GsonTool;
import com.xxl.job.core.util.ThrowableUtil;
import com.xxl.job.core.util.XxlJobRemotingUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copy from : https://github.com/xuxueli/xxl-rpc
 *
 * @author xuxueli 2020-04-11 21:25
 */
public class EmbedServer {

  private static final Logger logger = LoggerFactory.getLogger(EmbedServer.class);

  private ExecutorBiz executorBiz;
  private Thread thread;

  public void start(final String address, final int port, final String appName,
      final String accessToken) {
    executorBiz = new ExecutorBizClientImpl();
    thread = new Thread(() -> {
      // param
      EventLoopGroup bossGroup = new NioEventLoopGroup();
      EventLoopGroup workerGroup = new NioEventLoopGroup();
      ThreadPoolExecutor bizThreadPool = new ThreadPoolExecutor(
          0,
          200,
          60L,
          TimeUnit.SECONDS,
          new LinkedBlockingQueue<Runnable>(2000),
          new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
              return new Thread(r, "xxl-job, EmbedServer bizThreadPool-" + r.hashCode());
            }
          },
          new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
              throw new RuntimeException("xxl-job, EmbedServer bizThreadPool is EXHAUSTED!");
            }
          });
      try {
        // start server
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() {
              @Override
              public void initChannel(SocketChannel channel) throws Exception {
                channel.pipeline()
                    .addLast(new IdleStateHandler(0, 0, 30 * 3,
                        TimeUnit.SECONDS))  // beat 3N, close if idle
                    .addLast(new HttpServerCodec())
                    .addLast(new HttpObjectAggregator(
                        5 * 1024 * 1024))  // merge request & reponse to FULL
                    .addLast(new EmbedHttpServerHandler(executorBiz, accessToken, bizThreadPool));
              }
            })
            .childOption(ChannelOption.SO_KEEPALIVE, true);

        // bind
        ChannelFuture future = bootstrap.bind(port).sync();

        logger.info(">>>>>>>>>>> xxl-job remoting server start success, nettype = {}, port = {}",
            EmbedServer.class, port);

        // start registry
        startRegistry(appName, address);

        // wait util stop
        future.channel().closeFuture().sync();

      } catch (InterruptedException e) {
        logger.info(">>>>>>>>>>> xxl-job remoting server stop.");
      } catch (Exception e) {
        logger.error(">>>>>>>>>>> xxl-job remoting server error.", e);
      } finally {
        // stop
        try {
          workerGroup.shutdownGracefully();
          bossGroup.shutdownGracefully();
        } catch (Exception e) {
          logger.error(e.getMessage(), e);
        }
      }
    });
    thread.setDaemon(
        true);    // daemon, service jvm, user thread leave >>> daemon leave >>> jvm leave
    thread.start();
  }

  public void stop() throws Exception {
    // destroy server thread
    if (thread != null && thread.isAlive()) {
      thread.interrupt();
    }

    // stop registry
    // kuanghc 发送停止注册请求
    stopRegistry();

    logger.info(">>>>>>>>>>> xxl-job remoting server destroy success.");
  }

  // ---------------------- registry ----------------------

  /**
   * netty_http
   * <p>
   * Copy from : https://github.com/xuxueli/xxl-rpc
   *
   * @author xuxueli 2015-11-24 22:25:15
   */
  public static class EmbedHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(EmbedHttpServerHandler.class);

    private ExecutorBiz executorBiz;
    private String accessToken;
    private ThreadPoolExecutor bizThreadPool;

    public EmbedHttpServerHandler(ExecutorBiz executorBiz, String accessToken,
        ThreadPoolExecutor bizThreadPool) {
      this.executorBiz = executorBiz;
      this.accessToken = accessToken;
      this.bizThreadPool = bizThreadPool;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, FullHttpRequest msg) {
      // request parse
      //final byte[] requestBytes = ByteBufUtil.getBytes(msg.content());    // byteBuf.toString(io.netty.util.CharsetUtil.UTF_8);
      String requestData = msg.content().toString(CharsetUtil.UTF_8);
      String uri = msg.uri();
      HttpMethod httpMethod = msg.method();
      boolean keepAlive = HttpUtil.isKeepAlive(msg);
      String accessTokenReq = msg.headers().get(XxlJobRemotingUtil.XXL_JOB_ACCESS_TOKEN);

      // invoke
      bizThreadPool.execute(() -> {
        // do invoke
        Object responseObj = process(httpMethod, uri, requestData, accessTokenReq);

        // to json
        String responseJson = GsonTool.toJson(responseObj);

        // write response
        writeResponse(ctx, keepAlive, responseJson);
      });
    }

    private Object process(HttpMethod httpMethod, String uri, String requestData,
        String accessTokenReq) {
      // valid
      if (HttpMethod.POST != httpMethod) {
        return new Response<String>(Response.FAIL_CODE, "invalid request, HttpMethod not support.");
      }
      if (uri == null || uri.trim().isEmpty()) {
        return new Response<String>(Response.FAIL_CODE, "invalid request, uri-mapping empty.");
      }
      if (accessToken != null
          && !accessToken.trim().isEmpty()
          && !accessToken.equals(accessTokenReq)) {
        return new Response<String>(Response.FAIL_CODE, "The access token is wrong.");
      }

      // kuanghc 这里应该是admin调用client端的uri
      // services mapping
      try {
        switch (uri) {
          case "/beat":
            return executorBiz.beat();
          case "/idleBeat":
            IdleBeatParam idleBeatParam = GsonTool.fromJson(requestData, IdleBeatParam.class);
            return executorBiz.idleBeat(idleBeatParam);
          case "/run":
            // kuanghc admin发送请求，client执行任务
            TriggerParam triggerParam = GsonTool.fromJson(requestData, TriggerParam.class);
            return executorBiz.run(triggerParam);
          case "/kill":
            KillParam killParam = GsonTool.fromJson(requestData, KillParam.class);
            return executorBiz.kill(killParam);
          case "/log":
            LogParam logParam = GsonTool.fromJson(requestData, LogParam.class);
            return executorBiz.log(logParam);
          default:
            return new Response<String>(Response.FAIL_CODE,
                "invalid request, uri-mapping(" + uri + ") not found.");
        }
      } catch (Exception e) {
        logger.error(e.getMessage(), e);
        return new Response<String>(Response.FAIL_CODE,
            "request error:" + ThrowableUtil.toString(e));
      }
    }

    /**
     * write response
     */
    private void writeResponse(ChannelHandlerContext ctx, boolean keepAlive, String responseJson) {
      // write response
      FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
          HttpResponseStatus.OK, Unpooled.copiedBuffer(responseJson,
          CharsetUtil.UTF_8));   //  Unpooled.wrappedBuffer(responseJson)
      response.headers().set(HttpHeaderNames.CONTENT_TYPE,
          "text/html;charset=UTF-8");       // HttpHeaderValues.TEXT_PLAIN.toString()
      response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
      if (keepAlive) {
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
      }
      ctx.writeAndFlush(response);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
      ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
      logger.error(">>>>>>>>>>> xxl-job provider netty_http server caught exception", cause);
      ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
      if (evt instanceof IdleStateEvent) {
        ctx.channel().close();      // beat 3N, close if idle
        logger.debug(">>>>>>>>>>> xxl-job provider netty_http server close an idle channel.");
      } else {
        super.userEventTriggered(ctx, evt);
      }
    }
  }

  // ---------------------- registry ----------------------

  public void startRegistry(final String appName, final String address) {
    // start registry
    ExecutorRegistryThread.getInstance().start(appName, address);
  }

  public void stopRegistry() {
    // stop registry
    ExecutorRegistryThread.getInstance().toStop();
  }
}
