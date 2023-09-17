package com.xxl.job.common.model;

import java.io.Serializable;

/**
 * common return
 * @author xuxueli 2015-12-4 16:32:31
 * @param <T>
 */
public class Response<T> implements Serializable {

  private static final long serialVersionUID = 42L;

  public static final int SUCCESS_CODE = 200;
  public static final int FAIL_CODE = 500;

  public static final Response<String> SUCCESS = new Response<>(null);
  public static final Response<String> FAIL = new Response<>(FAIL_CODE, null);

  private int code;
  private String msg;
  private T content;

  public Response() {
  }

  public Response(int code, String msg) {
    this.code = code;
    this.msg = msg;
  }

  public Response(T content) {
    this.code = SUCCESS_CODE;
    this.content = content;
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public T getContent() {
    return content;
  }

  public void setContent(T content) {
    this.content = content;
  }

  @Override
  public String toString() {
    return "ReturnT [code=" + code + ", msg=" + msg + ", content=" + content + "]";
  }

}
