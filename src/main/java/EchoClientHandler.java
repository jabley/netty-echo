import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class EchoClientHandler extends ChannelInboundHandlerAdapter {

	private int msgsRcvd;
	private long totalTimeSpent;
	private float meanTimeSpent;

	private final long numMessages;

	private final ByteBuf time = Unpooled.buffer(8);

	public EchoClientHandler(long numMessages) {
		this.numMessages = numMessages;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		time.writerIndex(0);
		time.writeLong(System.currentTimeMillis());
		ctx.channel().writeAndFlush(time.retain());
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		ByteBuf buf = (ByteBuf) msg;

		if (buf.readableBytes() < 8) {
			return;
		}

		msgsRcvd++;

		long recvTime = System.currentTimeMillis();
		long sentTime = buf.readLong();

		totalTimeSpent += recvTime - sentTime;
		meanTimeSpent = (float) totalTimeSpent / msgsRcvd;

		if (msgsRcvd >= numMessages) {
			System.out.printf("Mean: %.4fms\n", meanTimeSpent);
			ctx.channel().close();
		} else {
			time.resetWriterIndex();
			time.writeLong(System.currentTimeMillis());
			ctx.channel().writeAndFlush(time.retain());
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		cause.printStackTrace();
		ctx.channel().close();
	}

}
