import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;

public class EchoClientHandler extends SimpleChannelHandler {

	private int msgsRcvd;
	private long totalTimeSpent;
	private float meanTimeSpent;

	private final long numMessages;

	private final ChannelBuffer time = ChannelBuffers.buffer(8);

	public EchoClientHandler(long numMessages) {
		this.numMessages = numMessages;
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		time.writerIndex(0);
		time.writeLong(System.currentTimeMillis());
		e.getChannel().write(time);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		ChannelBuffer buf = (ChannelBuffer) e.getMessage();

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
			e.getChannel().close();
		} else {
			time.resetWriterIndex();
			time.writeLong(System.currentTimeMillis());
			e.getChannel().write(time);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		e.getCause().printStackTrace();
		e.getChannel().close();
	}

}
