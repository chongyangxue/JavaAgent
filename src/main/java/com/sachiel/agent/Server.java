package com.sachiel.agent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent工具
 * </p>
 * @author chongyangxue
 * @since 2014-7-31
 */
public class Server {
	
	private final static Logger log = LoggerFactory.getLogger(Server.class);
	
	public static void main(String[] args){
		Server server = new Server();
		server.run();
	}
	
	public void run() {
		ServerBootstrap bootstrap = new ServerBootstrap(new 
				NioServerSocketChannelFactory(Executors.newCachedThreadPool(), 
				Executors.newCachedThreadPool())); 

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() { 
            @Override 
            public ChannelPipeline getPipeline() { 
            	ChannelPipeline pipeline = Channels.pipeline();
             	pipeline.addLast("decoder", new MessageDecoder()); 
                pipeline.addLast("encoder", new MessageEncoder()); 

        		pipeline.addLast("handler", new testCommandHandler());
        		return pipeline;
            } 
        });
        bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);
		bootstrap.setOption("child.reuseAddress", true);
		InetSocketAddress address = new InetSocketAddress(Integer.parseInt("6698"));
		bootstrap.bind(address);
		System.out.println("Agent is listening at " + address.getHostName() + ":" + address.getPort() + "......");
		
		try {
            Thread.sleep(3600L*60);
        } catch (Exception e) {
        }
	}
	
	public class testCommandHandler extends SimpleChannelUpstreamHandler {
		@Override
		public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e)
				throws Exception {
			if (e instanceof ChannelStateEvent) {
				System.err.println(e.toString());
	        }
			super.handleUpstream(ctx, e);
		}
		
		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) throws Exception {
			//System.out.println(e.getMessage());
			String request = event.getMessage().toString();
			JSONObject result = new JSONObject();
			StringBuffer response = new StringBuffer();
			try {
				//签名认证并解密
				boolean signSuccess = false;
				JSONObject json = JSONObject.fromObject(request);
				byte[] cmd = Base64.decodeBase64(json.getString("cmd"));
				byte[] signature = Base64.decodeBase64(json.getString("signature"));
				
				signSuccess = RsaUtils.verify(RsaUtils.getSignPublicKey(), cmd, signature);
				if(signSuccess) {
					byte[] decriptCmd = RsaUtils.decript(RsaUtils.getPrivateKey(), cmd);
					String command = new String(decriptCmd);
					System.out.println("[" + event.getRemoteAddress() + "] CommandRequest: " + command);
					Process process = Runtime.getRuntime().exec(command);
					BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
					String line = null;
					while((line = reader.readLine()) != null){
						System.out.println(line);
						response.append(line + "  ");
					}
					result.put("result", "success");
					result.put("msg", response.toString());
				}
			} catch (Exception ex) {
				log.error("Exec cmd failed", ex);
				result.put("result", "failed");
				result.put("msg", ex.toString());
			}
			Channel channel = event.getChannel();
			ChannelFuture future = channel.write(result.toString());
			System.out.println("[" + event.getRemoteAddress() + "] CommandResponse: " + response.toString());
			
			future.addListener(ChannelFutureListener.CLOSE);
		}
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
				throws Exception {
			System.err.println(e.toString());
			System.err.println("Error message:  " + e.getCause().getMessage());
			e.getChannel().close();
		}
	}
	
	public class MessageDecoder extends FrameDecoder { 

	    @Override 
	    protected Object decode( 
	            ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception { 
	    	buffer.readBytes(buffer.readableBytes());
	    	return new String(buffer.array()); 
	    } 
	}
	
	public class MessageEncoder extends OneToOneEncoder { 
	    @Override 
	    protected Object encode( 
	            ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception { 
	    	byte[] data = null;
	    	if (msg instanceof String) { 
	    		String res = (String)msg;
	    		data = res.getBytes(); 
	        } else if(msg instanceof byte[]) {
	        	data = (byte[])msg;
	        }
	        ChannelBuffer buf = ChannelBuffers.dynamicBuffer(); 
	        buf.writeBytes(data); 
	        return buf;
	    } 
	}
}
