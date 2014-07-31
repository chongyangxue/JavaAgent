package com.sachiel.agent;

import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;

import org.jboss.netty.bootstrap.ClientBootstrap;
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
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
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
public class Client {
	private final static Logger log = LoggerFactory.getLogger(Client.class);
	
    private final ClientBootstrap bootstrap; 
    
    public Client() {
    	log.info("ClientBootstrap is starting..."); 
    	bootstrap = new ClientBootstrap( 
                new NioClientSocketChannelFactory( 
                Executors.newCachedThreadPool(), 
                Executors.newCachedThreadPool())); 
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() { 
            @Override 
            public ChannelPipeline getPipeline() { 
            	ChannelPipeline pipeline = Channels.pipeline();
            	pipeline.addLast("decoder", new MessageDecoder()); 
            	pipeline.addLast("encoder", new MessageEncoder()); 
        		pipeline.addLast("handler", new CommandHandler());

                return pipeline; 
            } 
        }); 
        bootstrap.setOption("connectTimeoutMillis", 20000);
        bootstrap.setOption("tcpNoDelay", true); 
        //bootstrap.setOption("keepAlive", true); 
        bootstrap.setOption("reuseAddress", true);
    }

	public String run(String ipAddress, String port, byte[] request) {
		Throwable throwable = null;
		String response = null;
		try {
			// Start the connection attempt.
			ChannelFuture future = bootstrap.connect(new InetSocketAddress(ipAddress, Integer.parseInt(port)));

			// Wait until the connection attempt succeeds or fails.
			Channel channel = future.awaitUninterruptibly().getChannel();
			if (!future.isSuccess()) {
				throwable = future.getCause();
				log.error(throwable.getMessage(), throwable);
			}
			//ChannelFuture writeFuture = channel.write(request);
			ChannelFuture writeFuture = channel.write(request);
			
			// Wait until all messages are flushed before closing the channel.
			if (writeFuture != null) {
				writeFuture.awaitUninterruptibly();
			}
			if(!writeFuture.isSuccess()) {
				throwable = future.getCause();
				log.error(throwable.getMessage(), throwable);
			}
			CommandHandler handler = (CommandHandler) channel.getPipeline().getLast();
			response = handler.getResponseMessage();
		} catch (Exception e) {
			throwable = e;
			log.error(throwable.getMessage(), throwable);
		}
        return response;
	}
	
	public void close() { 
        log.info("ClientBootstrap releases the external resources..."); 
        bootstrap.releaseExternalResources(); 
    } 
	
	public class CommandHandler extends SimpleChannelUpstreamHandler {
		
		final BlockingQueue<String> answer = new SynchronousQueue<String>();
		
		public String getResponseMessage() {
			boolean interrupted = false;
	          for (;;) {
	              try {
	            	  String response = answer.take();
	                  if (interrupted) {
	                      Thread.currentThread().interrupt();
	                  }
	                  return response;
	              } catch (InterruptedException e) {
	                  interrupted = true;
	              }
	          }
		}
		
		@Override
		public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e)
				throws Exception {
			if (e instanceof ChannelStateEvent) {
				log.debug(e.toString());
	        }
			super.handleUpstream(ctx, e);
		}
		
		@Override
		public void messageReceived(ChannelHandlerContext ctx, final MessageEvent e){
			e.getChannel().close().addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) {
                	String response = (String) e.getMessage();
                    boolean offered = answer.offer(response);
                    assert offered;
                }
            });
		}
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, final ExceptionEvent e){
			log.error(e.getCause().getMessage(), e);
			e.getChannel().close().addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) {
                	// 无法获取操作类型
                	String response = e.getCause().toString();
                    boolean offered = answer.offer(response);
                    assert offered;
                }
            });
		}
	}
	
	public class MessageDecoder extends FrameDecoder { 
	    @Override 
	    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer){ 
	    	buffer.readBytes(buffer.readableBytes());
	    	return new String(buffer.array());
	    } 
	}
	
	public class MessageEncoder extends OneToOneEncoder { 
	    @Override 
	    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg){ 
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