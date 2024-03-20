package org.example.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.example.BoundsMessage;
import org.example.RemoteSequenceServiceGrpc;

import java.util.concurrent.TimeUnit;

public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private final ManagedChannel channel;
    private final RemoteSequenceServiceGrpc.RemoteSequenceServiceBlockingStub blockingStub;

    public Client(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        blockingStub = RemoteSequenceServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void requestSequence(int firstValue, int lastValue) {
        BoundsMessage request = BoundsMessage.newBuilder()
                .setFirstNumber(firstValue)
                .setLastNumber(lastValue)
                .build();
        blockingStub.getSequence(request).forEachRemaining(valueMessage -> {
            int currentValue = 0;
            logger.info("Received value: {}", valueMessage.getValue());
            System.out.println("currentValue:" + (currentValue += valueMessage.getValue() + 1));
        });
    }

    public static void main(String[] args) throws InterruptedException {
        Client client = new Client("localhost", 50051);
        client.requestSequence(0, 30);
        client.shutdown();
    }
}