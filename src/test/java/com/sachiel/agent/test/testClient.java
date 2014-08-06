package com.sachiel.agent.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
		while(true){
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("please input command:");
			try{
				String pass = in.readLine();
				JSONObject result = ClientUtils.sendCommand(pass, "101.227.175.36", "20724");
				//JSONObject result = ClientUtils.sendCommand("ls /opt", "http://127.0.0.1");
				System.out.println(result.toString());
			}catch (IOException e){
				e.printStackTrace();
			}
		}
		
	}
}
