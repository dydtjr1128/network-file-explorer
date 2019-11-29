package com.dydtjr1128.nfe.server.fileserver;

import com.dydtjr1128.nfe.server.config.Config;
import lombok.Getter;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Getter
class Attachment {
    private long readPosition;
    private AsynchronousFileChannel fileChannel;
    private String fileName;
    private long fileSize;

    Attachment() {
        readPosition = 0;
    }
    public void calcFileData(String string){
        String temp[] = string.replace(Config.END_MESSAGE_MARKER, "").split(Config.MESSAGE_DELIMITTER);
        fileName = temp[0];
        fileSize = Long.parseLong(temp[1]);
    }

    public void addPosition(int position) {
        readPosition += position;
    }

    public void openFileChannel(Path path) throws IOException {
        System.out.println("open!");
        fileChannel = AsynchronousFileChannel.open(
                path,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE
        );
    }
}