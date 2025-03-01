import java.io.File;
import java.io.FileOutputStream;
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

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int bytesRead = socketChannel.read(buffer);

            buffer.flip();
            byte[] a = new byte[bytesRead];
            buffer.get(a);
            String clientMessage = new String(a);
            System.out.println("Client command: "+ clientMessage);
            switch(clientMessage){
                case "E":
                    String str6 = "CLOSING CONNECTION";
                    ByteBuffer replyBuffer7 = ByteBuffer.wrap(str6.getBytes());
                    socketChannel.write(replyBuffer7);
                    socketChannel.close();
                    break;
                case "U":
                    String uploadPrompt = "Enter file name:";
                    ByteBuffer uploadPromptBuffer = ByteBuffer.wrap(uploadPrompt.getBytes());
                    socketChannel.write(uploadPromptBuffer);

                    // Read file name
                    ByteBuffer fileNameBufferUpload = ByteBuffer.allocate(1024);
                    int fileNameBytesUpload = socketChannel.read(fileNameBufferUpload);
                    fileNameBufferUpload.flip();
                    byte[] fileNameBytesArr = new byte[fileNameBytesUpload];
                    fileNameBufferUpload.get(fileNameBytesArr);
                    String fileNameUpload = new String(fileNameBytesArr);
                    fileNameBufferUpload.clear();

                    String filePathUpload = filepath + fileNameUpload;

                    // Ask for file size
                    String fileSizePrompt = "Enter file size:";
                    ByteBuffer fileSizePromptBuffer = ByteBuffer.wrap(fileSizePrompt.getBytes());
                    socketChannel.write(fileSizePromptBuffer);

                    ByteBuffer filesizeBuffer = ByteBuffer.allocate(8);
                    socketChannel.read(filesizeBuffer);
                    filesizeBuffer.flip();
                    long filesize = filesizeBuffer.getLong();

                    FileOutputStream fileOutputStream = new FileOutputStream(filePathUpload);
                    FileChannel fileChannelUpload = fileOutputStream.getChannel();

                    ByteBuffer fileBuffer = ByteBuffer.allocate(1024);
                    long bytesReceived = 0;

                    while (bytesReceived < filesize) {
                        fileBuffer.clear();
                        int bytesReadUpload = socketChannel.read(fileBuffer);
                        if (bytesReadUpload == -1) break;
                        bytesReceived += bytesReadUpload;

                        fileBuffer.flip();
                        fileChannelUpload.write(fileBuffer);
                    }

                    fileChannelUpload.close();
                    fileOutputStream.close();

                    String uploadSuccess = "File uploaded successfully!";
                    ByteBuffer uploadSuccessBuffer = ByteBuffer.wrap(uploadSuccess.getBytes());
                    socketChannel.write(uploadSuccessBuffer);
                    break;
                case "D":
                    String str4 = "File name for download?";
                    ByteBuffer sendBuffer = ByteBuffer.wrap(str4.getBytes());
                    socketChannel.write(sendBuffer);
                    ByteBuffer replyBuffer5 = ByteBuffer.allocate(1024);
                    int download_reply_bytes = socketChannel.read(replyBuffer5);
                    replyBuffer5.flip();

                    byte[] download_reply = new byte[download_reply_bytes];
                    replyBuffer5.get(download_reply);
                    System.out.println("Client wants: " + new String(download_reply));
                    replyBuffer5.clear();

                    String download_file = new String(download_reply);
                    File download_file_file = new File(filepath + download_file);
                    if (!download_file_file.exists()){
                        System.out.println("User entered wrong filename");
                        ByteBuffer downloadError = ByteBuffer.wrap(ByteBuffer.allocate(8).putLong(-1).array());
                        socketChannel.write(downloadError);
                        break;
                    }else{
                        long fileSize = download_file_file.length();
                        ByteBuffer fileSizeBuffer = ByteBuffer.wrap(ByteBuffer.allocate(8).putLong(fileSize).array());
                        socketChannel.write(fileSizeBuffer);

                        FileInputStream fileInputStream = new FileInputStream(download_file_file);
                        FileChannel fileChannel = fileInputStream.getChannel();
                        ByteBuffer download_file_buffer = ByteBuffer.allocate(1024);
                        int byteread;

                        do{
                            byteread = fileChannel.read(download_file_buffer);
                            if (byteread > 0){
                                download_file_buffer.flip();
                                while (download_file_buffer.hasRemaining()){
                                    socketChannel.write(download_file_buffer);
                                }
                                download_file_buffer.clear();
                            }
                        }while (byteread >= 0);
                        fileChannel.close();
                        fileInputStream.close();
                        break;
                    }
                case "A": // Append / Change File Name
                    // Ask user what file to rename
                    String file_request = "File name?";
                    ByteBuffer replyBuffer4 = ByteBuffer.wrap(file_request.getBytes());
                    socketChannel.write(replyBuffer4);
                    // Read the file name from the client
                    ByteBuffer fileNameBuffer = ByteBuffer.allocate(1024);
                    int fileNameBytesRead = socketChannel.read(fileNameBuffer);
                    fileNameBuffer.flip();
                    byte[] fileNameBytes = new byte[fileNameBytesRead];
                    fileNameBuffer.get(fileNameBytes);
                    String fileName = new String(fileNameBytes);
                    fileNameBuffer.clear();
                    // ask user the new name
                    String contentPrompt = "Insert New Name:";
                    ByteBuffer contentPromptBuffer = ByteBuffer.wrap(contentPrompt.getBytes());
                    socketChannel.write(contentPromptBuffer);
                    // Reads user response to rename
                    ByteBuffer contentBuffer = ByteBuffer.allocate(1024);
                    int contentBytesRead = socketChannel.read(contentBuffer);
                    contentBuffer.flip();
                    byte[] contentBytes = new byte[contentBytesRead];
                    contentBuffer.get(contentBytes);
                    String appendContent = new String(contentBytes);
                    contentBuffer.clear();
                    // Rename and move file within "Server Files"
                    try {
                        Path oldFilePath = Paths.get(filepath, fileName);
                        Path newFilePath = Paths.get(filepath, appendContent);
                        if (Files.exists(oldFilePath)) {
                            Files.move(oldFilePath, newFilePath, StandardCopyOption.REPLACE_EXISTING);
                            String successMessage = "File renamed successfully and placed in Server Files.";
                            ByteBuffer successBuffer = ByteBuffer.wrap(successMessage.getBytes());
                            socketChannel.write(successBuffer);
                        } else {
                            String errorMessage = "File not found.";
                            ByteBuffer errorBuffer = ByteBuffer.wrap(errorMessage.getBytes());
                            socketChannel.write(errorBuffer);
                        }
                    } catch (IOException e) {
                        String errorMessage = "Error renaming file: " + e.getMessage();
                        ByteBuffer errorBuffer = ByteBuffer.wrap(errorMessage.getBytes());
                        socketChannel.write(errorBuffer);
                    }
                    break;
                case "R":
                    int bytesRead1 = socketChannel.read(buffer);
                    buffer.flip();
                    byte[] a2 = new byte[bytesRead1];
                    buffer.get(a2);
                    String clientMessage2 = new String(a2);
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
                    String clientMessage3 = new String(a3);
                    System.out.println("File deleting: "+ clientMessage3);
                    File filetodelete = new File(filepath + clientMessage3);
                    if (!filetodelete.exists()) {
                        String error = "File not found, sending error";
                        System.out.println(error);
                        ByteBuffer reply4 = ByteBuffer.wrap(error.getBytes());
                        socketChannel.write(reply4);

                    }else{
                        filetodelete.delete();
                        ByteBuffer reply5 = ByteBuffer.wrap("File deleted successfully".getBytes());
                        socketChannel.write(reply5);
                    }
                    break;
                case "L":
                    try (Stream<Path> paths = Files.walk(Paths.get(filepath))){
                        StringBuilder file_list = new StringBuilder(paths
                                .filter(Files::isRegularFile)
                                .map(path -> path.getFileName().toString())
                                .collect(Collectors.joining(",")));
                        if (file_list.isEmpty()){
                            ByteBuffer replyBuffer2 = ByteBuffer.wrap("Filepath is empty".getBytes());
                            socketChannel.write(replyBuffer2);
                            break;
                        }
                        ByteBuffer replyBuffer2 = ByteBuffer.wrap(file_list.toString().getBytes());
                        socketChannel.write(replyBuffer2);
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    break;
                default:
                    ByteBuffer default_response = ByteBuffer.wrap("Incorrect command".getBytes());
                    socketChannel.write(default_response);
                    break;
            }
        }
    }
}
