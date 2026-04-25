package game;

import java.net.InetSocketAddress;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server to run game
 */
public class GameServer extends WebSocketServer {
    private final ObjectMapper objectMapper;
    private final Map<Integer, GameSession> sessions;
    private final Map<WebSocket, Integer> playerSessions; // maps player to their session id
    private final Timer gameLoopInterval;

    /**
     * constructs new server
     */
    public GameServer() {
        super(new InetSocketAddress(getBindAddress(), getEnvPort()));
        this.gameLoopInterval = new Timer(true);
        this.objectMapper = new ObjectMapper();
        this.playerSessions = new ConcurrentHashMap<>();
        //initialize sessions
        this.sessions = new ConcurrentHashMap<>();
        sessions.put(0, new GameSession(0,0));
        sessions.put(1, new GameSession(1,1));
        sessions.put(2, new GameSession(2,2));
    }

    private static int getEnvPort() {
        String portEnv = System.getenv("PORT");
        return portEnv != null ? Integer.parseInt(portEnv) : 8080;
    }

    private static String getBindAddress() {
        return System.getenv("PORT") != null ? "0.0.0.0" : "localhost";
    }

    @Override
    public void onStart() {
        setConnectionLostTimeout(100);

        // set game update loop
        gameLoopInterval.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //System.out.println("UPDATE RUN — NO MOVEMENT");
                gameLoop();
            }
        }, 0, 100);
    }

    @Override
    public void onOpen(WebSocket ws, ClientHandshake hnsk) {
        return;
    }

    private void playerJoinSession(WebSocket ws, JsonNode jsonNode) {
        int sessionNumber = jsonNode.get("session").asInt();
        GameSession session = sessions.get(sessionNumber);
        playerSessions.put(ws, sessionNumber);
        session.handleJoin(ws, jsonNode);
    }


    @Override    
    public void onMessage(WebSocket ws, String msg) {
        try {
            JsonNode jsonNode = objectMapper.readTree(msg); // what da hell is this
            String type = jsonNode.get("type").asText();

            if (type.equals("join")) {
                playerJoinSession(ws, jsonNode);
            }
            
            if (playerSessions.get(ws) == null) {
                System.out.println("session not found for websocket " + type);
                return;
            }
            GameSession session = sessions.get(playerSessions.get(ws));
            if (session == null) {
                System.out.println("session not found");
                return;
            }

            switch (type) {
                case "join":
                    session.broadcast(session.packageInitData());
                    break;
                case "ping":
                    session.handlePingMessage(ws, jsonNode);
                    break;
                case "move":
                    session.handleMovement(ws, jsonNode);
                    break;
                case "attack":
                    session.handleAttack(ws, jsonNode);
                    break;
                default:
                    System.out.println("what the fuck is this message " + type);
                    break;
            }
        } catch (Exception e) {
            System.out.println("wwaaaaaa " + e);
        }
    }

    @Override
    public void onClose(WebSocket ws, int code, String reason, boolean remote) { 
        GameSession session = sessions.get(playerSessions.get(ws));
        if (session == null) {
            return;
        }
        session.removePlayer(ws);
        
    }

    @Override
    public void onError(WebSocket ws, Exception eeee) {
        System.out.println("error ok ::: " + eeee);
    }

    // juicy update logic ahead!!!
    private void gameLoop() {
        for (GameSession session : sessions.values()) {
            if (session.getPlayerCount() == 0) {
                continue;
            }
            try {
                session.update();
                session.broadcast(session.packagePlayerData());
                session.broadcast(session.packageProjectileData());
            } catch (Exception e) {
                System.out.println("booo err " + e);
            }
        }
    }

    // classic java...
    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.start();

        System.out.println("ws server running on ws://localhost:{port}");
        System.out.println("working");
    }
}
