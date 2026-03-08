package com.example.myauth.exception;

public class DmAccessDeniedException extends RuntimeException {
  public DmAccessDeniedException() {
    super("해당 DM 대화방에 접근할 권한이 없습니다.");
  }
}
