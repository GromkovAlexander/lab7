package bmstu;
import org.zeromq.ZContext;
import org.zeromq.*;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        ZContext context = new ZContext();
        ZMQ.Socket socket = context.createSocket(SocketType.REQ);
        socket.bind("tcp://localhost:5559");
        Scanner in = new Scanner(System.in);


        while (true) {

            String com = in.nextLine();

            if (com.startsWith("GET")) {

            } else if (com.startsWith("PUT")) {
                
            }

        }
    }
}
