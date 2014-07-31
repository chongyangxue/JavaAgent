package com.sachiel.agent.test;

import net.sf.json.JSONObject;

import org.junit.Test;

import com.sachiel.agent.ClientUtils;

/**
 * Agent工具
 * </p>
 * @author chongyangxue
 * @since 2014-7-31
 */
public class testClient {
	@Test
	public void testAgentUtils() {
		JSONObject result = ClientUtils.sendCommand("df -h", "http://10.11.6.59");
		//JSONObject result = ClientUtils.sendCommand("ls /opt", "http://127.0.0.1");
		System.out.println(result.toString());
	}
}
