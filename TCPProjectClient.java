import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.util.Scanner;

public class TCPProjectClient {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Please provide <serverIP> and <serverPort>");
            return;
        }
        final String filepath = "Client Files/";
        int serverPort = Integer.parseInt(args[1]);
        SocketChannel channel = SocketChannel.open();
        channel.connect(
                new InetSocketAddress(args[0], serverPort)
        );
        while(true) {
            System.out.println("______________________");
            System.out.print("Enter Command from options: \nList('L')\nRemove('R')\nAppend('A')\nDownload('D')\nUpload('U')\nEnd Program('E')\nEnter Here: ");
            Scanner keyboard = new Scanner(System.in);
            String message = keyboard.nextLine();
            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
            int bytesWritten = channel.write(buffer);
            if (bytesWritten == 0){
                System.err.println("Could not write to server");
                break;
            }
            buffer.clear();
            //request to close the channel in the client-to-server
            // direction only
            //receive the server reply
            ByteBuffer replyBuffer =
                    ByteBuffer.allocate(1024);
            int bytesRead = channel.read(replyBuffer);
            if (bytesRead == -1){
                System.err.println("Connection closed by server");
                break;
            }else if(bytesRead == 0){
                System.err.println("No response");
                continue;
            }else {
                replyBuffer.flip();
                byte[] a = new byte[bytesRead];
                replyBuffer.get(a);
                String str = new String(a);
                System.out.println(str);
                switch (str){
                    case "CLOSING CONNECTION":
                        System.exit(0);
                        break;
                    case "File name for download?":
                        System.out.print("Enter here: ");
                        String filename2 = keyboard.nextLine();
                        ByteBuffer buffer3 = ByteBuffer.wrap(filename2.getBytes());
                        channel.write(buffer3);

                        ByteBuffer download_file = ByteBuffer.allocate(8);
                        channel.read(download_file);
                        download_file.flip();
                        long fileStatus = download_file.getLong();

                        if (fileStatus == -1){
                            System.out.println("No file found");
                            break;
                        }

                        FileOutputStream fileOutStream = new FileOutputStream(filepath + filename2);
                        FileChannel fileChannel = fileOutStream.getChannel();

                        System.out.println("File being downloaded: " +filename2);

                        ByteBuffer download_buffer = ByteBuffer.allocate(1024);
                        int bytesread = 0;
                        while (bytesread < fileStatus){
                            int currentbytes = channel.read(download_buffer);
                            if (currentbytes == -1){
                                break;
                            }
                            download_buffer.flip();
                            bytesread += currentbytes;
                            fileChannel.write(download_buffer);
                            download_buffer.flip();
                        }
                        fileChannel.close();
                        fileOutStream.close();
                        break;
                    case "File name?":
                        System.out.print("Enter here: ");
                        String filename = keyboard.nextLine();
                        ByteBuffer buffer2 = ByteBuffer.wrap(filename.getBytes());
                        channel.write(buffer2);

                        replyBuffer.clear();
                        int bytesRead2 = channel.read(replyBuffer);
                        replyBuffer.flip();
                        byte[] b = new byte[bytesRead2];
                        replyBuffer.get(b);
                        System.out.println("Server said: " + new String(b));
                        break;
                    case "Insert New Name:":
                        // Send the content to append
                        String content = keyboard.nextLine();
                        ByteBuffer contentBuffer = ByteBuffer.wrap(content.getBytes());
                        channel.write(contentBuffer);

                        // Wait for the server's success/error message
                        replyBuffer.clear();
                        bytesRead = channel.read(replyBuffer);
                        if (bytesRead > 0) {
                            replyBuffer.flip();
                            byte[] resultBytes = new byte[bytesRead];
                            replyBuffer.get(resultBytes);
                            String result = new String(resultBytes);
                            System.out.println(result);
                        }
                    case "Enter file name:": // Upload case
                        System.out.print("Enter filename to upload: ");
                        String uploadFileName = keyboard.nextLine();
                        File uploadFile = new File(filepath + uploadFileName);

                        if (!uploadFile.exists()) {
                            System.out.println("File not found.");
                            break;
                        }

                        ByteBuffer uploadFileNameBuffer = ByteBuffer.wrap(uploadFileName.getBytes());
                        channel.write(uploadFileNameBuffer);

                        // Receive file size request
                        replyBuffer.clear();
                        bytesRead = channel.read(replyBuffer);
                        replyBuffer.flip();
                        String fileSizePrompt = new String(replyBuffer.array(), 0, bytesRead);
                        System.out.println(fileSizePrompt);

                        long uploadFileSize = uploadFile.length();
                        ByteBuffer fileSizeBuffer = ByteBuffer.allocate(8);
                        fileSizeBuffer.putLong(uploadFileSize);
                        fileSizeBuffer.flip();
                        channel.write(fileSizeBuffer);

                        FileInputStream fileInputStream = new FileInputStream(uploadFile);
                        FileChannel fileUploadChannel = fileInputStream.getChannel();
                        ByteBuffer uploadBuffer = ByteBuffer.allocate(1024);
                        long bytesSent = 0;

                        System.out.println("Uploading file: " + uploadFileName);

                        while (bytesSent < uploadFileSize) {
                            uploadBuffer.clear();
                            int bytesReadUpload = fileUploadChannel.read(uploadBuffer);
                            if (bytesReadUpload == -1) break;
                            bytesSent += bytesReadUpload;
                            uploadBuffer.flip();
                            channel.write(uploadBuffer);
                        }

                        fileUploadChannel.close();
                        fileInputStream.close();

                        // Receive success message
                        replyBuffer.clear();
                        bytesRead = channel.read(replyBuffer);
                        replyBuffer.flip();
                        System.out.println(new String(replyBuffer.array(), 0, bytesRead));

                        System.out.println("Upload complete.");
                        break;
                }
            }
            replyBuffer.clear();

        }
    }
}