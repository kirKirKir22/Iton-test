package org.example.server;

import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.example.BoundsMessage;
import org.example.RemoteSequenceServiceGrpc;
import org.example.ValueMessage;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Server {
    private io.grpc.Server server;

    public void start() throws IOException {
        int port = 50051;
        server = ServerBuilder.forPort(port)
                .addService(new RemoteSequenceServiceImpl())
                .build()
                .start();
        System.out.println("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down gRPC server");
            try {
                server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
        }));
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = new Server();
        server.start();
        server.blockUntilShutdown();
    }

    static class RemoteSequenceServiceImpl extends RemoteSequenceServiceGrpc.RemoteSequenceServiceImplBase {
        @Override
        public void getSequence(BoundsMessage request, StreamObserver<ValueMessage> responseObserver) {
            int firstValue = request.getFirstNumber();
            int lastValue = request.getLastNumber();
            int currentValue = firstValue;
            while (currentValue <= lastValue) {
                responseObserver.onNext(ValueMessage.newBuilder().setValue(currentValue).build());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                currentValue++;
            }
            responseObserver.onCompleted();
        }
    }
}

