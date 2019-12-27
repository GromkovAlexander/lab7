package bmstu;
import org.zeromq.ZContext;
import org.zeromq.*;

import java.net.Socket;

public class Main {
    public static void main(String[] args) {

        ZContext context = new ZContext();

        ZMQ.Socket frontend = context.createSocket(SocketType.ROUTER);
        ZMQ.Socket backend = context.createSocket(SocketType.ROUTER);

    }
}
