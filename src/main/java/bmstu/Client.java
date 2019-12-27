package bmstu;
import org.zeromq.ZContext;
import org.zeromq.*;

import java.util.Scanner;

public class Client {

    private final static String SOCKET_FRONT = "tcp://localhost:5559";

    public static void main(String[] args) {
        ZContext context = new ZContext();
        ZMQ.Socket socket = context.createSocket(SocketType.REQ);
        socket.bind(SOCKET_FRONT);
        Scanner in = new Scanner(System.in);


        while (!Thread.currentThread().isInterrupted()) {

            String com = in.nextLine();

            ZMsg msg = new ZMsg();
            for (int i = 0; i < com.split(" ").length; i++) {
                msg.add(com.split(" ")[i]);
            }

            msg.send(socket);



        }
    }
}
