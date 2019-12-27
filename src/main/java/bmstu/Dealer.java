package bmstu;
import org.zeromq.ZContext;
import org.zeromq.*;

import java.util.Scanner;


public class Dealer {

    private final static String dataForAll = "asdasdasdasdhasdahsjbdlashdblasdhbalshdblasdbhasdbahsdlahsd";

    private final static String SOCKET_FRONT = "tcp://localhost:5559";
    private final static String SOCKET_BACK = "tcp://localhost:5560";

    private final static String PUT = "PUT";
    private final static String GET = "GET";

    private final static int TIME_DELAY = 10000;

    public static void main(String[] args) {
        ZContext context = new ZContext();
        ZMQ.Socket socket = context.createSocket(SocketType.REQ);
        socket.bind(SOCKET_BACK);

        Scanner in = new Scanner(System.in);
        int left = in.nextInt();
        int right = in.nextInt();

        StringBuilder data = new StringBuilder(dataForAll.substring(left, right));

        long time = System.currentTimeMillis();

        ZMQ.Poller items = context.createPoller(1);
        items.register(socket, ZMQ.Poller.POLLIN);


        while(!Thread.currentThread().isInterrupted()) {
            items.poll(1);

            if (System.currentTimeMillis() - time > TIME_DELAY) {
                time = System.currentTimeMillis();
                ZMsg msg = new ZMsg();

            }

        }



    }
}
