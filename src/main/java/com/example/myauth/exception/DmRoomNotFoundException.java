package com.example.myauth.exception;

public class DmRoomNotFoundException extends RuntimeException {
  public DmRoomNotFoundException(Long roomId) {
    super("DM 대화방을 찾을 수 없습니다. (ID: " + roomId + ")");
  }
}
