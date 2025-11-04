package com.vibeport.exception.handler;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 시스템 오류가 아닌 업무로직에서 발생한 예외를 처리
 */
@Getter
@Setter
@ToString
public class BusinessException extends RuntimeException {

	private final String code; 		// 코드

	private final String message; 	// 메세지

	public BusinessException(String message) {
		super(message);
		this.code = "";
		this.message = message;
	}

	public BusinessException(String code, String message) {
		super(message);
		this.code = code;
		this.message = message;
	}
}
