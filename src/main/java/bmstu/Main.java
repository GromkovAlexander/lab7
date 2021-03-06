package bmstu;
import javafx.util.Pair;
import org.zeromq.ZContext;
import org.zeromq.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Main {

    private static HashMap<Pair<Integer, Integer>, Pair<ZFrame, Long>> data = new HashMap<>();

    private final static String SOCKET_FRONT = "tcp://localhost:5579";
    private final static String SOCKET_BACK = "tcp://localhost:5580";

    private final static String PUT = "PUT";
    private final static String GET = "GET";
    private final static String NEW = "NEW";
    private final static String NOTIFY = "NOTIFY";
    private final static String DELIMETER = "#";
    private final static String NOT_FOUND = "NOT FOUND";

    private final static int TIME_DELAY = 3000;
    private final static int DOUBLE_TIME_DELAY = TIME_DELAY * 2;


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
                    boolean isContains = false;

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
                            if (left <= parseIndex && right > parseIndex && isAlive(serverInfo)) {
                                isContains = true;
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
                            if (left <= parseIndex && right > parseIndex && isAlive(serverInfo)) {
                                isContains = true;
                                ZFrame serverAdress = serverInfo.getValue().getKey().duplicate();
                                sendMsg(backend, serverAdress, adress, index);
//                                break;
                            }
                        }
                    }

                    if (!isContains) {
                        sendErorMsg(frontend, adress);
                    }



                    more = frontend.hasReceiveMore();
                    if (!more) {
                        break;
                    }

                }

            }

            if (items.pollin(1)) {
                while (true) {
                    ZMsg messageFromBack = ZMsg.recvMsg(backend);

                    ZFrame adress = messageFromBack.pop();

                    String command = messageFromBack.popString();

                    switch (command) {
                        case NEW:
                            String[] interval1 = messageFromBack.popString().split(DELIMETER);
                            data.put(
                                    new Pair<>(Integer.parseInt(interval1[0]), Integer.parseInt(interval1[1])),
                                    new Pair<>(adress, System.currentTimeMillis())
                            );
                            break;
                        case NOTIFY:
                            String[] interval2 = messageFromBack.popString().split(DELIMETER);
                            data.replace(
                                    new Pair<>(Integer.parseInt(interval2[0]), Integer.parseInt(interval2[1])),
                                    new Pair<>(adress, System.currentTimeMillis())
                            );
                            break;
                        default:
                            messageFromBack.wrap(messageFromBack.pop());
                            messageFromBack.send(frontend);
                            break;

                    }

                    more = backend.hasReceiveMore();
                    if (!more) {
                        break;
                    }
                }

            }

        }

    }

    private static boolean isAlive(Map.Entry<Pair<Integer, Integer>, Pair<ZFrame, Long>> storage) {
        long now = System.currentTimeMillis();
        if (now - storage.getValue().getValue() > DOUBLE_TIME_DELAY) {
            data.remove(storage);
            return false;
        }
        return true;
    }



    private static void sendMsg(ZMQ.Socket backend, ZFrame ... frames) {
        ZMsg msgToBackend = new ZMsg();
        msgToBackend.addAll(Arrays.asList(frames));
        msgToBackend.send(backend);
    }

    private static void sendErorMsg(ZMQ.Socket frontend, ZFrame adress) {
        ZMsg errMsg = new ZMsg();
        errMsg.add(NOT_FOUND);
        errMsg.wrap(adress);
        errMsg.send(frontend);
    }
}
