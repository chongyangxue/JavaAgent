package com.sachiel.agent;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * RSA加密工具
 * </p>
 * @author chongyangxue
 * @since 2014-7-31
 */
public class RsaUtils {
	// 用于加密解密的rsa 模数
	static String n = "138735050616842444897186689641993144988677592205954074746" +
			"4959536638256792639971395858648177006034963869894340198912694363564" +
			"0333732050320111556889764784281672840793788033075092569642769884126" +
			"6474271714951662521345896210978705258600144941674299414254371759490" +
			"912686046700408694360846088519467572750010785973637";
	// 公钥
	static String e = "65537";
	// 加密解密的私钥
	static String d = "473846128600241281501842754618974710076225525113764134630" +
			"1425800404464660642556071364264582465338149635124419947886194155060" +
			"0917078629532230204208010548863173237466287157930320336172927367566" +
			"0128623123902862384276826963299306191838829611136674864931816129457" +
			"21564493403948918802096868435067203986462855817025";

	//签名认证的rsa模数
	static String sign_n = "1218330524967608918819967764499605165386092291250629" +
			"3673731350968201193526171523905932703818625679483264123806949891847" +
			"6153282785676624728845426121132201452929306844542664352702461638945" +
			"9544646207269277901200762324343128928526647195592159609985106663511" +
			"30547946044069695691737723219058842017700771906828155207";
	//签名认证的私钥
	static String sign_d = "5820332088165596173052530744269365110254232501757168" +
			"4488282171515847714742955008921807074455246868019823374928940013100" +
			"5368437788844384643090383259638375133375160752762088027617218063358" +
			"8953187767059325670353570913521035362626776650247068975821267945284" +
			"2244961621807763332079265128502504453683470137631259553";
	
	/**
	 * 根据keyInfo产生公钥和私钥，并且保存到pk.dat和sk.dat文件中
	 * 
	 * @param keyInfo
	 * @throws Exception
	 */
	public static void genKeys(String keyInfo) throws Exception {
		KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
		SecureRandom random = new SecureRandom();
		random.setSeed(keyInfo.getBytes());
		// 初始加密，长度为512，必须是大于512才可以的
		keygen.initialize(512, random);
		// 取得密钥对
		KeyPair kp = keygen.generateKeyPair();
		// 取得公钥
		PublicKey publicKey = kp.getPublic();
		System.out.println(publicKey);
		// 取得私钥
		PrivateKey privateKey = kp.getPrivate();
		System.out.println(privateKey);
	}

	/**
	 * 根据公钥n、e生成公钥
	 * 
	 * @param modulus
	 *            公钥n串
	 * @param publicExponent
	 *            公钥e串
	 * @return 返回公钥PublicKey
	 * @throws Exception
	 */
	public static PublicKey getPublickKey(String modulus, String publicExponent){
		KeySpec publicKeySpec = new RSAPublicKeySpec(new BigInteger(modulus),
				new BigInteger(publicExponent));
		try {
			KeyFactory factory = KeyFactory.getInstance("RSA");
			PublicKey publicKey = factory.generatePublic(publicKeySpec);
			return publicKey;
		}catch(Exception e) {
			return null;
		}
	}
	
	public static PublicKey getPublickKey(){
		return getPublickKey(n, e);
	}
	
	public static PublicKey getSignPublicKey() {
		return getPublickKey(sign_n, e);
	}

	/**
	 * 根据公钥n、e生成私钥
	 * 
	 * @param modulus
	 *            私钥n串
	 * @param privatekeyExponent
	 *            私钥e串
	 * @return 返回私钥Key
	 * @throws Exception
	 */
	public static PrivateKey getPrivateKey(String modulus, String privatekeyExponent) {
		KeySpec privateKeySpec = new RSAPrivateKeySpec(new BigInteger(modulus),
				new BigInteger(privatekeyExponent));
		PrivateKey privateKey = null;
		try {
			KeyFactory factory = KeyFactory.getInstance("RSA");
			privateKey = factory.generatePrivate(privateKeySpec);
		}catch(Exception e) {
			System.err.println("Failed to get private key");
		}
		return privateKey;
	}

	public static PrivateKey getPrivateKey() {
		return getPrivateKey(n, d);
	}
	
	public static PrivateKey getSignPrivateKey() {
		return getPrivateKey(sign_n, sign_d);
	}

	/**
	 * 用私钥证书进行签名
	 * 
	 * @param message
	 *            签名之前的原文
	 * @param privateKey
	 *            私钥
	 * @return byte[] 返回签名
	 * @throws Exception
	 */
	public static byte[] sign(PrivateKey privateKey, byte[] message) {
		try {
			//Signature sign = Signature.getInstance("RSA/ECB/PKCS1Padding");
			Signature sign = Signature.getInstance("MD5withRSA");
			sign.initSign(privateKey);
			sign.update(message);
			byte[] signed = sign.sign();
			return signed;
		}catch(Exception e) {
			System.err.println("Failed to get signature");
			e.printStackTrace();
			return null;
		}
	}

	public static byte[] sign(PrivateKey privateKey, String message) {
		try {
			Signature sign = Signature.getInstance("MD5withRSA");
			sign.initSign(privateKey);
			sign.update(message.getBytes("utf-8"));
			byte[] signed = sign.sign();
			return signed;
		}catch(Exception e) {
			System.err.println("Failed to get signature");
			return null;
		}
	}

	/**
	 * 用公钥证书进行验签
	 * 
	 * @param src
	 *            签名之前的原文
	 * @param cipherText
	 *            签名
	 * @param publicKey
	 *            公钥
	 * @return boolean 验签成功为true,失败为false
	 * @throws Exception
	 */
	public static boolean verify(PublicKey publicKey, byte[] src,
			byte[] cipherText) throws SignatureException,
			NoSuchAlgorithmException, InvalidKeyException {
		Signature sign = Signature.getInstance("MD5withRSA");
		sign.initVerify(publicKey);
		sign.update(src);
		if (sign.verify(cipherText)) {
			return true;
		}
		return false;
	}

	public static boolean verify(PublicKey publicKey, String src,
			byte[] cipherText) throws SignatureException,
			NoSuchAlgorithmException, InvalidKeyException,
			UnsupportedEncodingException {
		Signature sign = Signature.getInstance("MD5withRSA");
		sign.initVerify(publicKey);
		sign.update(src.getBytes("utf-8"));
		if (sign.verify(cipherText)) {
			return true;
		}
		return false;
	}

	/**
	 * 用公钥进行加密
	 * 
	 * @param message
	 *            签名之前的原文
	 * @param publicKey
	 *            公钥
	 * @return byte[] 密文
	 * @throws Exception
	 */
	public static byte[] encript(PublicKey publicKey, byte[] message){
		try{
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			return cipher.doFinal(message);
		}catch (Exception e){
			return null;
		}
	}

	public static byte[] encript(PublicKey publicKey, String message){
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			return cipher.doFinal(message.getBytes("utf-8"));
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 用私钥解密
	 * 
	 * @param privateKey		私钥
	 * @param cipherText		密文
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static byte[] decript(PrivateKey privateKey, byte[] cipherText)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		return cipher.doFinal(cipherText);
	}
	

	public static String decript(PrivateKey privateKey, String cipherText)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		byte[] message =  cipher.doFinal(cipherText.getBytes());
		return new String(message);
	}

	public static void main(String[] args) throws InvalidKeyException,
			SignatureException, NoSuchAlgorithmException, Exception {
		String src = "hello Sachiel";
		PublicKey publicKey = getPublickKey(n, e);
		PrivateKey privateKey = getPrivateKey(n, d);
		
		System.out.println(publicKey + "---------" + privateKey);
		/**
		 * 公钥加密，私钥解密
		 */
		System.out.println(encript(publicKey, src.getBytes()));
		System.out.println(new String(decript(privateKey, encript(publicKey, src.getBytes()))));
		
		/**
		 * 私钥签名，公钥认证
		 */
		byte[] signed = sign(privateKey, src);
		System.out.println(verify(publicKey, src, signed));
		
		//genKeys("123456");
	}
}
