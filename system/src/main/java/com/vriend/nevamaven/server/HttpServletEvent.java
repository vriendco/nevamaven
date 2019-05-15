package com.vriend.nevamaven.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@FunctionalInterface
public interface HttpServletEvent {
	public void exec(HttpServletRequest request, HttpServletResponse response);
}
