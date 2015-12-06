package rpn.server.model.primary;

import io.netty.channel.ChannelPipeline;
import rpn.server.model.client.Client;
import rpn.server.net.decoders.PrimaryRequestDecoder;
import rpn.server.net.handlers.PrimaryChannelHandler;

public class Primary extends Client {

    public Primary(String host, int port) {
        super(host, port);
    }

    @Override
    protected void initialisePipeline(ChannelPipeline pipeline) {
        pipeline.addLast("decoder", new PrimaryRequestDecoder())
                .addLast("handler", new PrimaryChannelHandler());
    }
}