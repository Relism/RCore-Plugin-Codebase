package dev.relismdev.rcore.utils;

import dev.relismdev.rcore.api.dataHandler;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class nodeTester {
    private static final int NUM_PINGS_PER_ENDPOINT = 3;
    private static final int PING_TIMEOUT_MS = 1000; // timeout for ping response in ms

    private final IO.Options options;
    private final Map < String, List < Long >> latencyMap; // endpoint -> list of latencies in ms
    private final Map < String, Long > bestLatencyMap; // endpoint -> best latency in ms
    private Socket socket;
    private long bestLatency = Long.MAX_VALUE;

    private static dataHandler dh = new dataHandler();

    public nodeTester() {
        options = new IO.Options();
        options.reconnection = false;
        options.reconnectionDelay = 100;
        latencyMap = new HashMap < > ();
        bestLatencyMap = new HashMap < > ();
    }

    public String run(String apinode){
        String node;
        if(apinode.equals("auto")) {
            msg.log("Node set to auto, testing all the mirrors now.");
            String[] endpoints = {"https://server.relism.repl.co", "https://api.relimc.com"};
            JSONObject nodeObj = findBest(endpoints);
            node = nodeObj.getString("endpoint");
        } else {
            node = apinode;
            msg.log("Node set to '" + node + "', setting it now.");
        }
        return node;
    }

    public JSONObject findBest(String[] endpoints) {
        msg.log("Checking which api nodes are up...");
        String[] upEndpoints = checkIsUp(endpoints);
        for (String endpoint : upEndpoints) {
            msg.log(endpoint);
        }
        msg.log("Performing ping tests on the " + upEndpoints.length + " node(s)...");
        try {
            CountDownLatch latch = new CountDownLatch(upEndpoints.length);
            Map<String, Long> bestLatencyMap = new HashMap<>();

            for (String endpoint : upEndpoints) {
                List<Long> latencies = new ArrayList<>();

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
                                msg.log("&#eb4034• &fConnection error with endpoint: " + endpoint);
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
                        msg.log("&#eb4034• &fInvalid endpoint URI: " + endpoint);
                    }
                }

                long bestLatencyForEndpoint = Long.MAX_VALUE;
                for (long latency : latencies) {
                    if (latency < bestLatencyForEndpoint) {
                        bestLatencyForEndpoint = latency;
                    }
                }
                bestLatencyMap.put(endpoint, bestLatencyForEndpoint);
                //msg.log("&#f5f542• &fEndpoint " + endpoint + " scored best latency of " + bestLatencyForEndpoint + "ms");
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

            msg.log("&#f5f542• &fUsing: &#f5f542" + bestEndpoint + "&f, latency: &b" + bestLatency + "ms");
            bestObj.put("endpoint", bestEndpoint);
            bestObj.put("latency", bestLatency);

            return bestObj;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Default return statement in case of exception
        return null;
    }

    public static String[] checkIsUp(String[] endpoints) {
        List<String> upEndpoints = new ArrayList<>();
        try {
            CountDownLatch latch = new CountDownLatch(endpoints.length);

            for (String endpoint : endpoints) {
                try {
                    JSONObject json = dh.reqAPI(endpoint + "/status");
                    boolean up = json.getBoolean("up");
                    if (up) {
                        msg.log("&#2ECC71• &f" + endpoint + " \u2714 &#2ECC71IS UP");
                        upEndpoints.add(endpoint);
                    } else {
                        msg.log("&#eb4034• &f" + endpoint + " \u2718 &#eb4034IS DOWN");
                    }
                    latch.countDown(); // count down the latch after each connection attempt
                } catch (Exception e) {
                    msg.log("&#eb4034• &f" + endpoint + " \u2718 &#eb4034IS DOWN");
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