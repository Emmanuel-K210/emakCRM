package com.emak.crm.exception;

import com.emak.crm.utils.MessageExceptionUtils;

public class EntityNotFound extends Throwable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private EntityNotFound(String message) {
		super(message);
	}
	
	public static EntityNotFound of(String message) {
		return new EntityNotFound(MessageExceptionUtils.entityNotFound(message));
	}
}
