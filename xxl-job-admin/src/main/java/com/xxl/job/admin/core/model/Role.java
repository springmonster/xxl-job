package com.xxl.job.admin.core.model;

public enum Role {
  NORMAL(0),
  ADMIN(1);

  private final int value;

  Role(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
