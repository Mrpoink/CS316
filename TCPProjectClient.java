import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
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

                        ByteBuffer download_file = ByteBuffer.allocate(1024);
                        FileOutputStream fileOutStream = new FileOutputStream(filepath + filename2);
                        FileChannel fc = fileOutStream.getChannel();
                        System.out.println("File being downloaded: " + filename2);
                        do{
                            download_file.flip();
                            fc.write(download_file);
                            download_file.clear();
                        }while(channel.read(download_file) >= 0);
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
                }
            }
            replyBuffer.clear();

        }
    }
}