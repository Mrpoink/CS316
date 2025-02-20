import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.io.FileInputStream;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TCPProjectServer {

    public static void main(String[] args) throws Exception {
        final String filepath = "Server Files/";

        ServerSocketChannel listenChannel = ServerSocketChannel.open();
        listenChannel.bind(new InetSocketAddress(1068));
        SocketChannel socketChannel = listenChannel.accept();

        while (true) {
            System.out.println("Line 21");

            System.out.println("Line 23 complete");
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int bytesRead = socketChannel.read(buffer);
            System.out.println("Line 26 complete");
            buffer.flip();
            byte[] a = new byte[bytesRead];
            buffer.get(a);
            String clientMessage = new String(a);
            System.out.println("Line 31: "+ clientMessage);
            switch(clientMessage){
                case "E":
                    String str6 = "CLOSING CONNECTION";
                    ByteBuffer replyBuffer7 = ByteBuffer.wrap(str6.getBytes());
                    socketChannel.write(replyBuffer7);
                    socketChannel.close();
                    break;
                case "U":
                    String str5 = "UPLOAD";
                    ByteBuffer replyBuffer6 = ByteBuffer.wrap(str5.getBytes());
                    socketChannel.write(replyBuffer6);
                    break;
                case "D":
                    String str4 = "DOWNLOAD";
                    ByteBuffer replyBuffer5 = ByteBuffer.wrap(str4.getBytes());
                    socketChannel.write(replyBuffer5);
                    break;
                case "A":
                    String str3 = "APPEND";
                    ByteBuffer replyBuffer4 = ByteBuffer.wrap(str3.getBytes());
                    socketChannel.write(replyBuffer4);
                    break;
                case "R":
                    int bytesRead1 = socketChannel.read(buffer);
                    buffer.flip();
                    byte[] a2 = new byte[bytesRead1];
                    buffer.get(a2);
                    String clientMessage2 = new String(a);
                    System.out.println("Line 64: "+ clientMessage2);
                    String str2 = "File name?";
                    ByteBuffer replyBuffer3 = ByteBuffer.wrap(str2.getBytes());
                    socketChannel.write(replyBuffer3);
                    ByteBuffer remove_buffer = ByteBuffer.allocate(1024);
                    int remove_bytes = socketChannel.read(remove_buffer);
                    System.out.println("File name read");
                    remove_buffer.flip();
                    byte[] a3 = new byte[remove_bytes];
                    remove_buffer.get(a3);
                    System.out.println(new String(a3));
                    String clientMessage3 = new String(a3);
                    System.out.println("File deleting: "+ clientMessage3);
                    File filetodelete = new File(filepath + clientMessage3);
                    if (filetodelete.exists()) {
                        filetodelete.delete();
                    }else{
                        String error = "File not found, sending error";
                        System.out.println(error);
                        ByteBuffer reply4 = ByteBuffer.wrap(error.getBytes());
                        socketChannel.write(reply4);
                    }
                    break;
                case "L":
                    StringBuilder file_list = new StringBuilder();
                    try (Stream<Path> paths = Files.walk(Paths.get(filepath))){
                        file_list = new StringBuilder(paths
                                .filter(Files::isRegularFile)
                                .map(path -> path.getFileName().toString())
                                .collect(Collectors.joining(",")));
                        ByteBuffer replyBuffer2 = ByteBuffer.wrap(file_list.toString().getBytes());
                        socketChannel.write(replyBuffer2);
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }
}
