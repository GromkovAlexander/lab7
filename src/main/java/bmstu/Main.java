package bmstu;
import org.zeromq.ZContext;
import org.zeromq.*;

import java.net.Socket;

public class Main {

    private final static String SOCKET_FRONT = "tcp://localhost:5559";
    private final static String SOCKET_BACK = "tcp://localhost:5560";

    private final static String PUT = "PUT";
    private final static String GET = "GET";

    public static void main(String[] args) {

        ZContext context = new ZContext();

        ZMQ.Socket frontend = context.createSocket(SocketType.ROUTER);
        ZMQ.Socket backend = context.createSocket(SocketType.ROUTER);

        frontend.bind(SOCKET_FRONT);
        backend.bind(SOCKET_BACK);

        ZMQ.Poller items = context.createPoller(2);
        items.register(frontend, ZMQ.Poller.POLLIN);
        items.register(backend, ZMQ.Poller.POLLIN);

        boolean more;

        while (!Thread.currentThread().isInterrupted()) {
            items.poll();

            if (items.pollin(0)) {
                ZMsg messageFromFront = ZMsg.recvMsg(frontend);
                more = frontend.hasReceiveMore();

                ZFrame adress = messageFromFront.pop();
                ZFrame nullFrame = messageFromFront.pop();

                String command = messageFromFront.popString();

                if (command.equals(PUT)) {
                    ZFrame index = messageFromFront.pop();
                } else if (command.equals(GET)) {

                }
            }

        }

    }
}
