package com.sachiel.agent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Agent工具
 * </p>
 * @author chongyangxue
 * @since 2014-7-31
 */
public class ClientUtils {
	private final static Logger log = LoggerFactory.getLogger(ClientUtils.class);

	public static JSONObject sendCommand(String cmd, String targetSrc, String port) {
		JSONObject resultJson = new JSONObject();
		Pattern pattern = Pattern.compile("\\d+[.]\\d+[.]\\d+[.]\\d+");
		Matcher matcher = pattern.matcher(targetSrc);
		String ipAddress = null;
		if(matcher.find()){
			ipAddress = matcher.group(0);
		}else{
			log.error("Can not get IP address from targetSrc!");
			resultJson.put("result", "failed");
			resultJson.put("msg", "Can not get IP address from targetSrc!");
			return resultJson;
		}
		byte[] encriptCmd = RsaUtils.encript(RsaUtils.getPublickKey(), cmd);
		byte[] signatureBytes = RsaUtils.sign(RsaUtils.getSignPrivateKey(), encriptCmd);
		JSONObject jsonMsg = new JSONObject();
		jsonMsg.put("cmd", Base64.encodeBase64String(encriptCmd));
		jsonMsg.put("signature", Base64.encodeBase64String(signatureBytes));
		
		byte[] request = jsonMsg.toString().getBytes();

		Client client = new Client();
		try {
			//System.out.println("Send msg: " + jsonMsg.toString());
			String result = client.run(ipAddress, port, request);
			resultJson = JSONObject.fromObject(result);
			
		} catch (Exception e) {
			log.error("" + e.getMessage());
			resultJson.put("result", "failed");
			resultJson.put("msg", e.getMessage());
		}
		return resultJson;
	}
}
