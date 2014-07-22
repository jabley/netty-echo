import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.Executors;

public class EchoClient {

	public static void main(String[] args) {
		final AtomicReference<Long> numMessages = new AtomicReference<Long>(
				100000L);

		if (args.length == 1) {
			numMessages.set(Long.parseLong(args[0]));
		} else {
			System.err.println("usage: EchoClient <num-msgs>");
			System.exit(1);
		}

		ChannelFactory factory = new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool());

		ClientBootstrap bootstrap = new ClientBootstrap(factory);
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				return Channels.pipeline(new EchoClientHandler(numMessages
						.get()));
			}
		});
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);

		final ChannelFuture f = bootstrap.connect(new InetSocketAddress(
				"127.0.0.1", 8080));

		f.getChannel().getCloseFuture().awaitUninterruptibly();
		factory.releaseExternalResources();
	}

}
