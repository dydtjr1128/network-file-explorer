package file.receiver;

import config.Config;
import file.FileAction;
import org.xerial.snappy.Snappy;
import protocol.core.NFEProtocol;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FileReceiver {
    private final String ip;
    private final int port;

    public FileReceiver(final String ip, final int port) {
        this.ip = ip;
        this.port = port;
    }


    public void receive(final String storePath) throws IOException {
        final ByteBuffer dataBuffer = ByteBuffer.allocate(NFEProtocol.NETWORK_BYTE);
        AsynchronousSocketChannel channel = AsynchronousSocketChannel.open();
        channel.connect(new InetSocketAddress(ip, port), channel, new CompletionHandler<Void, AsynchronousSocketChannel>() {
            @Override
            public void completed(Void result, AsynchronousSocketChannel channel) {
                dataBuffer.clear();
                dataBuffer.put(FileAction.FILE_SEND_TO_CLIENT);
                dataBuffer.flip();
                Future<Integer> future = channel.write(dataBuffer);
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                dataBuffer.clear();
                writeToFile(channel, dataBuffer, storePath);
            }

            @Override
            public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
                System.out.println("err!");
            }
        });

    }

    public void writeToFile(AsynchronousSocketChannel channel, ByteBuffer dataBuffer, String storePath) {
        channel.read(dataBuffer, new Attachment(), new CompletionHandler<Integer, Attachment>() {

            @Override
            public void completed(final Integer result, final Attachment readData) {
                if (result > 0) {
                    dataBuffer.flip();
                    long messageLen = dataBuffer.getLong();
                    byte[] bytes = new byte[(int) messageLen];
                    dataBuffer.get(bytes);
                    String string = null;
                    try {
                        string = Snappy.uncompressString(bytes);
                        if (string.contains(Config.END_MESSAGE_MARKER)) {
                            readData.calcFileData(string);
                            /*Path path = Paths.get(Config.FILE_STORE_PATH + readData.getFileName());
                            if (Files.notExists(Paths.get(Config.FILE_STORE_PATH))) {
                                try {
                                    Files.createDirectories(Paths.get(Config.FILE_STORE_PATH));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (Files.notExists(path) && !Files.isDirectory(path)) {

                                System.out.println(path);
                                readData.openFileChannel(path);

                                dataBuffer.clear();
                                writeToFile(channel, readData, dataBuffer);
                            } else {
                                close(channel, readData.getFileChannel());
                                System.out.println("Download err!");
                                return;
                            }*/
                        } else {
                            writeToFile(readData, dataBuffer, result);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                dataBuffer.clear();
                channel.read(dataBuffer, readData, this);
            }

            @Override
            public void failed(final Throwable exc, final Attachment attachment) {
                close(channel, null);
                System.out.println("file client exit");
            }
        });
    }

    public void writeToFile(Attachment attachment, ByteBuffer buffer, int result) {
        Future<Integer> operation = attachment.getFileChannel().write(buffer, attachment.getReadPosition());
        while (!operation.isDone()) ;
        attachment.addPosition(result);
    }

    public void close(AsynchronousSocketChannel channel, AsynchronousFileChannel fileChannel) {
        try {
            if (fileChannel != null && fileChannel.isOpen())
                fileChannel.close();
            if (!channel.isOpen())
                channel.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("FileServer close err!");
        }
    }

}
