package org.example.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.example.BoundsMessage;
import org.example.RemoteSequenceServiceGrpc;

import java.util.concurrent.TimeUnit;

/**
 * Client for interacting with a remote gRPC service to retrieve a sequence of numbers.
 */
public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private final ManagedChannel channel;
    private final RemoteSequenceServiceGrpc.RemoteSequenceServiceBlockingStub blockingStub;

    /**
     * Constructor for the client.
     *
     * @param host the host of the server.
     * @param port the port of the server.
     */
    public Client(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        blockingStub = RemoteSequenceServiceGrpc.newBlockingStub(channel);
    }

    /**
     * Shutdown the communication channel with the server.
     *
     * @throws InterruptedException if an error occurs while waiting for the channel shutdown to complete.
     */
    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * Send a request to retrieve a sequence of numbers.
     *
     * @param firstValue the first number of the sequence.
     * @param lastValue  the last number of the sequence.
     */
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

    /**
     * Entry point for the application.
     *
     * @param args the command-line arguments (not used).
     * @throws InterruptedException if an error occurs while waiting for the client to finish its work.
     */
    public static void main(String[] args) throws InterruptedException {
        Client client = new Client("localhost", 50051);
        client.requestSequence(0, 30);
        client.shutdown();
    }
}