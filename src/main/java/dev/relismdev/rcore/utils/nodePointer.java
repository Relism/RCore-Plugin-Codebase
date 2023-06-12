package dev.relismdev.rcore.utils;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class nodePointer {
    private static final int NUM_PINGS_PER_ENDPOINT = 3;
    private static final int PING_TIMEOUT_MS = 1000; // timeout for ping response in ms

    private final IO.Options options;
    private final Map < String, List < Long >> latencyMap; // endpoint -> list of latencies in ms
    private final Map < String, Long > bestLatencyMap; // endpoint -> best latency in ms
    private Socket socket;
    private String bestEndpoint;
    private long bestLatency = Long.MAX_VALUE;

    public nodePointer() {
        options = new IO.Options();
        options.reconnection = false;
        options.reconnectionDelay = 100;
        latencyMap = new HashMap < > ();
        bestLatencyMap = new HashMap < > ();
    }

    public JSONObject findBest(String[] endpoints) {
        msg.log("Checking which api nodes are up...");
        String[] upEndpoints = checkIsUp(endpoints);
        msg.log(upEndpoints.toString());
        msg.log("Performing ping tests on the " + upEndpoints.length + " node(s)...");
        try {
            CountDownLatch latch = new CountDownLatch(upEndpoints.length);
            Map<String, Long> bestLatencyMap = new HashMap<>();

            for (String endpoint : upEndpoints) {
                List<Long> latencies = new ArrayList<>();

                msg.log("&#FEC8D8[INFO] &#ffffffPinging: " + endpoint);

                for (int i = 0; i < NUM_PINGS_PER_ENDPOINT; i++) {
                    final long[] timePing = new long[1];

                    try {
                        socket = IO.socket(endpoint, options);
                        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                            @Override
                            public void call(Object... args) {
                                socket.emit("ping");
                                timePing[0] = System.currentTimeMillis();
                            }
                        }).on("pong", new Emitter.Listener() {
                            @Override
                            public void call(Object... args) {
                                long latency = System.currentTimeMillis() - timePing[0];
                                latencies.add(latency);
                                socket.disconnect();
                            }
                        }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                            @Override
                            public void call(Object... args) {
                                msg.log("&#FCA5A5[ERROR] &#ffffffConnection error with endpoint: " + endpoint);
                                socket.disconnect();
                            }
                        });

                        socket.connect();

                        // wait for ping response or timeout
                        synchronized (this) {
                            try {
                                wait(PING_TIMEOUT_MS);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (URISyntaxException e) {
                        msg.log("&#FCA5A5[ERROR] &#ffffffInvalid endpoint URI: " + endpoint);
                    }
                }

                long bestLatencyForEndpoint = Long.MAX_VALUE;
                for (long latency : latencies) {
                    if (latency < bestLatencyForEndpoint) {
                        bestLatencyForEndpoint = latency;
                    }
                }
                bestLatencyMap.put(endpoint, bestLatencyForEndpoint);
                msg.log("&#FEC8D8[INFO] &#ffffffEndpoint " + endpoint + " scored best latency of " + bestLatencyForEndpoint + "ms");
                latch.countDown();
            }

            // Wait for all endpoints to be evaluated
            latch.await();

            String bestEndpoint = null;
            JSONObject bestObj = new JSONObject();
            long bestLatency = Long.MAX_VALUE;

            for (Map.Entry<String, Long> entry : bestLatencyMap.entrySet()) {
                String endpoint = entry.getKey();
                long latency = entry.getValue();

                if (latency < bestLatency) {
                    bestEndpoint = endpoint;
                    bestLatency = latency;
                }
            }

            msg.log("&#FEC8D8[INFO] &#ffffffBest endpoint: " + bestEndpoint + ", latency: " + bestLatency + "ms");
            bestObj.put("endpoint", bestEndpoint);
            bestObj.put("latency", bestLatency);

            return bestObj;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Default return statement in case of exception
        return null;
    }

    public String[] checkIsUp(String[] endpoints) {
        List<String> upEndpoints = new ArrayList<>();
        try {
            CountDownLatch latch = new CountDownLatch(endpoints.length);

            for (String endpoint : endpoints) {
                try {
                    socket = IO.socket(endpoint, options);

                    socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            msg.log("&#2ECC71" + endpoint + " \u2714 OK");
                            upEndpoints.add(endpoint);
                            socket.disconnect();
                            latch.countDown(); // count down the latch when the connection is successful
                        }
                    }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            msg.log("&#E74C3C" + endpoint + " \u2718 NO");
                            socket.disconnect();
                            latch.countDown(); // count down the latch when there's a connection error
                        }
                    });

                    socket.connect();

                } catch (URISyntaxException e) {
                    msg.log("&#FCA5A5[ERROR] &#ffffffInvalid endpoint URI: " + endpoint);
                    latch.countDown(); // count down the latch when there's an exception
                }
            }

            // Wait for all connections to be attempted
            latch.await();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Rebuild array with only up endpoints
        String[] upEndpointsArray = new String[upEndpoints.size()];
        upEndpoints.toArray(upEndpointsArray);
        return upEndpointsArray;
    }
}