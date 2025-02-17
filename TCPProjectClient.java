import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class TCPProjectClient {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Please provide <serverIP> and <serverPort>");
            return;
        }
        int serverPort = Integer.parseInt(args[1]);
        SocketChannel channel = SocketChannel.open();
        channel.connect(
                new InetSocketAddress(args[0], serverPort)
        );
        while(true) {
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
                    case "File name?":
                        buffer.clear();
                        System.out.println("Enter here: ");
                        String filename = keyboard.nextLine();
                        ByteBuffer buffer2 = ByteBuffer.wrap(message.getBytes());
                        int bytesWritten2 = channel.write(buffer2);
                        System.out.println(bytesWritten2);
                }
            }
            replyBuffer.clear();

        }
    }
}