import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

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

		Bootstrap bootstrap = new Bootstrap()
				.channel(NioSocketChannel.class)
				.group(new NioEventLoopGroup())
				.handler(new ChannelInitializer<Channel>() {

					@Override
					protected void initChannel(Channel ch) throws Exception {
						ch.pipeline().addLast(
								new EchoClientHandler(numMessages.get()));

					}

				}).option(ChannelOption.TCP_NODELAY, true)
				.option(ChannelOption.SO_KEEPALIVE, true);

		final ChannelFuture f = bootstrap.connect(new InetSocketAddress(
				"127.0.0.1", 8080));

		f.channel().closeFuture().awaitUninterruptibly();
		bootstrap.group().shutdownGracefully().awaitUninterruptibly();
	}

}
