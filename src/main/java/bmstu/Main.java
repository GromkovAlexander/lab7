package bmstu;
import javafx.util.Pair;
import org.zeromq.ZContext;
import org.zeromq.*;

import java.util.HashMap;
import java.util.Map;

public class Main {

    private static HashMap<Pair<Integer, Integer>, Pair<ZFrame, Long>> data = new HashMap<>();

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
                while (true) {
                    ZMsg messageFromFront = ZMsg.recvMsg(frontend);

                    ZFrame adress = messageFromFront.pop();
                    ZFrame nullFrame = messageFromFront.pop();

                    String command = messageFromFront.popString();

                    if (command.equals(PUT)) {
                        ZFrame index = messageFromFront.pop();
                        int parseIndex = Integer.parseInt(index.toString());

                        ZFrame characher = messageFromFront.pop();

                        for (Map.Entry<Pair<Integer, Integer>, Pair<ZFrame, Long>> serverInfo : data.entrySet()) {
                            int left = serverInfo.getKey().getKey();
                            int right = serverInfo.getKey().getValue();
                            if (left <= parseIndex && right > parseIndex) {
                                ZFrame serverAdress = serverInfo.getValue().getKey().duplicate();
                                sendMsg(backend, serverAdress, adress, index, characher);
                            }
                        }


                    } else if (command.equals(GET)) {
                        ZFrame index = messageFromFront.pop();
                        int parseIndex = Integer.parseInt(index.toString());

                        for (Map.Entry<Pair<Integer, Integer>, Pair<ZFrame, Long>> serverInfo : data.entrySet()) {
                            int left = serverInfo.getKey().getKey();
                            int right = serverInfo.getKey().getValue();
                            if (left <= parseIndex && right > parseIndex) {
                                ZFrame serverAdress = serverInfo.getValue().getKey().duplicate();
                                sendMsg(backend, serverAdress, adress, index);
                            }
                        }
                    }

                    more = frontend.hasReceiveMore();

                    if (!more) {
                        break;
                    }

                }

            }

            if (items.pollin(1)) {

            }

        }

    }



    private static void sendMsg(ZMQ.Socket backend, ZFrame ... frames) {
        ZMsg msgToBackend = new ZMsg();
        for (ZFrame frame : frames) {
            msgToBackend.add(frame);
        }
        msgToBackend.send(backend);
    }
}
