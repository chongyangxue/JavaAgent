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
		JSONObject result = ClientUtils.sendCommand("ls -l /root | grep ^d | wc -l", "http://127.0.0.1");
		//JSONObject result = ClientUtils.sendCommand("ls -l /log/sce3/999962587/20140701|grep ^- |wc -l", "http://10.121.41.119");
		System.out.println(result.toString());
	}
}
