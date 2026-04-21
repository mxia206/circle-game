package tumtumtumsahur;

import java.net.InetSocketAddress;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import tumtumtumsahur.Classes.*;
import tumtumtumsahur.Projectiles.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server to run game
 */
public class GameServer extends WebSocketServer {
    private final ObjectMapper objectMapper;
    private final Map<WebSocket, Player> players;
    private final Set<Projectile> projectiles;
    private final Set<Obstacle> obstacles;
    private final Timer gameLoopInterval;

    /**
     * constructs new server
     */
    public GameServer() {
        super(new InetSocketAddress("0.0.0.0", getEnvPort()));
        //super(new InetSocketAddress("localhost", 8080));
        this.objectMapper = new ObjectMapper();
        this.players = new ConcurrentHashMap<>();
        this.projectiles = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.obstacles = new HashSet<Obstacle>();
        this.gameLoopInterval = new Timer(true);
    }

    private static int getEnvPort() {
        String portEnv = System.getenv("PORT");
        return portEnv != null ? Integer.parseInt(portEnv) : 4269;
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

    private void handleJoin(WebSocket ws, JsonNode jsonNode) {
        String name = jsonNode.has("name") ? jsonNode.get("name").asText() : "unnamed";
        String gameClass = jsonNode.get("class").asText();
        String playerID = UUID.randomUUID().toString();
        Player player;
        int locx = (int)(Math.random()*4000);
        int locy = (int)(Math.random()*4000);
        switch (gameClass) {
            case "fire":
                player = new Fire(playerID, name, locx, locy);
                players.put(ws, player);
                break;
            case "ice":
                player = new Ice(playerID, name, locx, locy);
                players.put(ws, player);
                break;
            case "earth":
                player = new Earth(playerID, name, locx, locy);
                players.put(ws, player);
                break;
            case "blood":
                player = new Blood(playerID, name, locx, locy);
                players.put(ws, player);
                break;
            case "lightning":
                player = new Lightning(playerID, name, locx, locy);
                players.put(ws, player);
                break;
            default:
                System.out.println("Error: class " + gameClass + " does not exist");
        }  
        ObjectNode response = objectMapper.createObjectNode();
        response.put("type", "init");
        response.put("id", playerID);
        ws.send(response.toString());
        broadcastObstacleData();

        System.out.println("Player joined: " + name + " (" + playerID + ")" + "at: " + java.time.LocalDate.now());
    }

    @Override
    public void onOpen(WebSocket ws, ClientHandshake hnsk) {
        return;
    }
    
    private void createProjectile(Set<Projectile> newproj) {
        if (newproj == null) {
            return;
        }
        for (Projectile proj : newproj) {
            projectiles.add(proj);
        }
    }

    private void meleeAttack(Sweep swp, Player pl, double time) {
        if (pl.lightingspeed_time > 0) return;
        if(pl.isHitting == false) {
            pl.isHitting = true;
            pl.timeFromLastHit = time;
        }
        if (swp != null) {
            for (WebSocket oppws : players.keySet()) {
                Player opp = players.get(oppws);
                if (opp.id != pl.id && swp.collision(opp)) {
                    if (opp.invincible_time == 0) {
                        opp.health -= swp.damage;
                        opp.stun_time = swp.stun_time;
                        //blood class shit
                        if (opp.frenzy_time > 0) {
                            opp.health -= swp.damage * 0.3;
                        }
                        if (pl.gameClass.equals("blood")) {
                            pl.health = Math.min(100.0, pl.health+10);
                            if (pl.frenzy_time > 0) {
                                pl.health = Math.min(100.0, pl.health+5);
                            }
                        }
                    }
                    if (opp.health <= 0.0) {
                        pl.killcount++;
                        players.remove(oppws);
                        return;
                    }
                }
            }
        }
    }

    private void handleAttack(WebSocket ws, JsonNode jsonNode) {
        if (jsonNode.get("move") == null || jsonNode.get("move").isNull()) return;
        if (jsonNode.get("dir") == null || jsonNode.get("dir").isNull()) return;

        String move = jsonNode.get("move").asText(); //which attack is being used
        double dir = jsonNode.get("dir").asDouble(); // mouse direction
        double time = System.currentTimeMillis();

        Player pl = players.get(ws);
        if (pl == null) return;
        if (pl.stun_time > 0) return;
        switch (move) {
            case "basicMelee":
                meleeAttack(pl.basicMelee(dir), pl, time);
                break;
            case "skill1":
                createProjectile( pl.skill_1(dir));
                break;
            case "skill2":
                createProjectile(pl.skill_2(dir));
                break;
            case "skill3":
                createProjectile(pl.skill_3(dir));
                break;
            default:
                System.out.println("unknown move " + move);
                break;
        }   
    }


    @Override    
    public void onMessage(WebSocket ws, String msg) {
        try {
            JsonNode jsonNode = objectMapper.readTree(msg); // what da hell is this
            String type = jsonNode.get("type").asText();

            // wtf this is just js on steroids
            switch (type) {
                case "join":
                    handleJoin(ws, jsonNode);
                    break;
                case "ping":
                    handlePingMessage(ws, jsonNode);
                    break;
                case "move":
                    handleMovement(ws, jsonNode);
                    break;
                case "attack":
                    handleAttack(ws, jsonNode);
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
    public void onClose(WebSocket ws, int code, String reason, boolean remote) { // why the fuck do we need to include
                                                                                 // all the args if we arent gonna use
                                                                                 // them
        players.remove(ws); // what the hell this is way beter then finding the index and slicing it out
    }

    @Override
    public void onError(WebSocket ws, Exception eeee) {
        System.out.println("error ok ::: " + eeee);
    }

    private void handlePingMessage(WebSocket ws, JsonNode jsonNode) {
        // so uh apparently we have to craft json stuff like this
        // shouldnt be too big of a deal
        String msg = "{\"type\": \"ping\"}";
        ws.send(msg);
    }

    // juicy update logic ahead!!!
    private void gameLoop() {
        if (players.isEmpty())
            return;

        try {
            update();
            broadcastPlayerData();
            broadcastProjectileData();
        } catch (Exception e) {
            System.out.println("booo err " + e);
        }
    }

    private void handleMovement(WebSocket ws, JsonNode jsonNode) {
        if (jsonNode.get("x") == null || jsonNode.get("x").isNull()) return;
        if (jsonNode.get("y") == null || jsonNode.get("y").isNull()) return;
        if (jsonNode.get("dir") == null || jsonNode.get("dir").isNull()) return;
        double x = jsonNode.get("x").asDouble(); // x component
        double y = jsonNode.get("y").asDouble(); // y component
        double dir = jsonNode.get("dir").asDouble(); // mouse direction

        Player player = players.get(ws);
        if (player != null) {
            player.updateVelocity(x, y);
            player.last_dir = player.dir;
            player.dir = dir;
        }
    }  

    private void playerProjectileCollisions(WebSocket ws) {
        Player pl = players.get(ws);
        if (pl == null) return;
        for (Projectile proj : projectiles) {
            if (!proj.hitPlayers.contains(pl.id) && pl.collision(proj)) {
                if (pl.invincible_time == 0) {
                    pl.health -= proj.damage;
                    //blood class shit
                    if (pl.frenzy_time > 0) {
                        pl.health -= proj.damage * 0.3;
                    }
                    //check projectile effects
                    pl.slow = proj.slow;
                    pl.slow_time = proj.slow_time;
                    pl.stun_time = proj.stun_time;
                }
                proj.hitPlayers.add(pl.id);
                if (pl.health <= 0.0) {
                    proj.myPlayer.killcount++;
                    players.remove(ws);
                    return;
                }
                //some proj types end on impact
                if (proj.type.equals("clusterfireball") || proj.type.equals("lightningball")) {
                    proj.time = 0;
                }
                //iceblade stops on hit and sucks in player
                if (proj.type.equals("iceblade") && proj.time > 45) {
                    pl.x = proj.x; pl.y = proj.y;
                    proj.time = 45;
                }
            }
        }
    }

    private void projectileObstacleCollisions(Projectile proj) {
        if (proj == null) return;
        if (proj.type.equals("snowstorm") || proj.type.equals("shockwave")) return;
        for (Obstacle ob : obstacles) {
            if (proj.collision(ob)) {
                if (proj.type.equals("iceblade")) {
                    if (proj.time > 47) {
                        proj.time = 47;
                    }
                } else {
                    proj.time = 0;
                }
            }
        }
    }

    private void playerObstacleCollisions(WebSocket ws) {
        Player pl = players.get(ws);
        if (pl == null) return;
        for (Obstacle ob : obstacles) {
            if (pl.collision(ob)) {
                pl.obstacleCollision(ob);
            }
        }
    }

    private void update() {
        for (WebSocket ws : players.keySet()) {
            Player pl = players.get(ws);
            pl.update();
            if (pl.lightingspeed_time % 2 == 1) {
                for (int i = 0; i < 5; i++) {
                    double angle = 2*Math.PI*Math.random();
                    projectiles.add(new LightningSpark(UUID.randomUUID().toString(), pl.x+40*Math.cos(angle), pl.y+40*Math.sin(angle), angle, pl));
                }
            }
            playerProjectileCollisions(ws);
            playerObstacleCollisions(ws);
        }
        for (Projectile p : projectiles) {
            projectileObstacleCollisions(p);
            if (p.type.equals("lightningball")) {
                ((LightningBall)p).computeHoming(players.values());
            }
            p.update();
            if (p.time <= 0) {
                //cluster shot case
                if (p.type.equals("clusterfireball")) {
                    for (int i = 0; i < 8; i++) {
                        double angle = i*Math.PI/4;
                        Projectile newproj = new Fireball(UUID.randomUUID().toString(), p.x+40*Math.cos(angle), p.y+40*Math.sin(angle), angle, p.myPlayer);
                        for (String id : p.hitPlayers) {
                            newproj.hitPlayers.add(id);
                        }
                        projectiles.add(newproj);
                    }
                }
                projectiles.remove(p);
            }
        }
    }

    private void broadcastPlayerData() {
        ObjectNode resp = objectMapper.createObjectNode();
        resp.put("type", "players");
        resp.set("players", objectMapper.valueToTree(
            players.values().stream().map(pl ->
                Map.ofEntries(
                    Map.entry("id", pl.id),
                    Map.entry("x", pl.x),
                    Map.entry("y", pl.y),
                    Map.entry("name", pl.name),
                    Map.entry("last_x", pl.last_x),
                    Map.entry("last_y", pl.last_y),
                    Map.entry("dir", pl.dir),
                    Map.entry("last_dir", pl.last_dir),
                    Map.entry("health", pl.health),
                    Map.entry("mana", pl.mana),
                    Map.entry("gameClass", pl.gameClass),
                    Map.entry("timeFromLastHit", pl.timeFromLastHit),
                    //player states
                    Map.entry("basicEnhanced", pl.basicEnhanced),
                    Map.entry("isInvincible", (pl.invincible_time>0)),
                    Map.entry("isFrenzy", (pl.frenzy_time>0)),
                    Map.entry("isLightningSpeed", (pl.lightingspeed_time>0)),
                    Map.entry("isHitting", pl.isHitting),
                    //skill cooldowns
                    Map.entry("skill1cd", ((double)pl.skill1cd/pl.skill1maxcd)),
                    Map.entry("skill2cd", ((double)pl.skill2cd/pl.skill2maxcd)),
                    Map.entry("skill3cd", ((double)pl.skill3cd/pl.skill3maxcd)),
                    //killcount
                    Map.entry("killcount", pl.killcount)
                )
            ).toList()
        ));
        String msg = resp.toString();
        broadcast(msg);
    }

    private void broadcastProjectileData() {
        ObjectNode resp = objectMapper.createObjectNode();
        resp.put("type", "projectiles");
        resp.set("projectiles", objectMapper.valueToTree(
            projectiles.stream().map(pr -> 
                Map.of(
                    "id", pr.id, 
                    "x", pr.x, 
                    "y", pr.y,
                    "last_x", pr.last_x, 
                    "last_y",pr.last_y,
                    "radius", pr.radius,
                    "type", pr.type,
                    "dir", (Math.atan2(pr.y_vel,pr.x_vel)))).toList())
                    );

        String msg = resp.toString();
        broadcast(msg);
    }

    private void broadcastObstacleData() {
        ObjectNode resp = objectMapper.createObjectNode();
        resp.put("type", "obstacles");
        resp.set("obstacles", objectMapper.valueToTree(
            obstacles.stream().map(ob -> 
                Map.of(
                    "id", ob.id, 
                    "x", ob.x, 
                    "y", ob.y,
                    "radius", ob.radius)).toList()));
        String msg = resp.toString();
        broadcast(msg);
    }

    // classic java...
    public static void main(String[] args) {
        GameServer server = new GameServer();
        for (int i = 0; i < 30; i++) {
            server.obstacles.add(new Obstacle(i+"", Math.random()*4000,Math.random()*4000,40 + Math.random()*40));
        }
        server.start();

        System.out.println("ws server running on ws://localhost:{port}");
        System.out.println("working");
    }
}
