package game;

import java.net.InetSocketAddress;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import game.Templates.*;
import game.GameObjects.*;
import game.Classes.*;
import game.Projectiles.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameSession {

    private final ObjectMapper objectMapper;
    private final Map<WebSocket, Player> players;
    private final Set<Projectile> projectiles;
    private final Set<Obstacle> obstacles;
    private final CapturePoint capturepoint;
    private final int gamemode; // 0 for free for all, 1 for teams, 2 for capture point
    private final int sessionId;
    private int mapDim = 4000;

    /**
     * constructs new server
     */
    public GameSession(int gamemode, int sessionId) {
        this.objectMapper = new ObjectMapper();
        this.players = new ConcurrentHashMap<>();
        this.projectiles = Collections.newSetFromMap(new ConcurrentHashMap<>());
        //gamemode specific inits
        this.gamemode = gamemode;
        if (gamemode == 2) {
            mapDim = 8000;
            this.capturepoint = new CapturePoint(mapDim);
        } else {
            this.capturepoint = null;
        }
        //obstacle init
        this.obstacles = new HashSet<Obstacle>();
        for (int i = 0; gamemode != 2 ? i < 30 : i<160; i++) {
            this.obstacles.add(new Obstacle(i+"", Math.random()*mapDim,Math.random()*mapDim,40 + Math.random()*50));
        }
        this.sessionId = sessionId;
    }

    public void broadcast(String msg) {
        for (WebSocket ws : players.keySet()) {
            if (ws.isOpen()) {
                ws.send(msg);
            }
        }
    }

    public void handleJoin(WebSocket ws, JsonNode jsonNode) {
        String name = jsonNode.has("name") ? jsonNode.get("name").asText() : "unnamed";
        String gameClass = jsonNode.get("class").asText();
        String playerID = UUID.randomUUID().toString();
        Player player;
        int locx = (int)(Math.random()*mapDim);
        int locy = (int)(Math.random()*mapDim);
        switch (gameClass) {
            case "fire":
                player = new Fire(playerID, name, locx, locy);
                break;
            case "ice":
                player = new Ice(playerID, name, locx, locy);
                break;
            case "earth":
                player = new Earth(playerID, name, locx, locy);
                break;
            case "blood":
                player = new Blood(playerID, name, locx, locy);
                break;
            case "lightning":
                player = new Lightning(playerID, name, locx, locy);
                break;
            case "void":
                player = new game.Classes.Void(playerID, name, locx, locy);
                break;
            default:
                System.out.println("Error: class " + gameClass + " does not exist");
                return;
        }  
        if (gamemode == 1 || gamemode == 2) {
            if (jsonNode.has("team")) {
                player.team = jsonNode.get("team").asInt();
            } else {
                player.team = findSmallestTeam();
            }
        }
        if (gamemode == 2) {
            if (player.team == 0) {
                player.x = 100; player.y = mapDim/2;
            } else {
                player.x = mapDim - 100; player.y = mapDim/2;
            }
        }
        players.put(ws, player);
        ObjectNode response = objectMapper.createObjectNode();
        response.put("type", "init");
        response.put("id", playerID);
        ws.send(response.toString());

        System.out.println("Player joined: " + name + " (" + playerID + ")" + "at: " + java.time.LocalDate.now());
    }

    public void removePlayer(WebSocket ws) {
        if (ws.isOpen()) {
            ws.send("{\"type\": \"death\"}");
        }
        System.out.println("Player removed: " + ws);
        players.remove(ws); // what the hell this is way beter then finding the index and slicing it out
    }


    private int findSmallestTeam() {
        Map<Integer, Integer> teamSizes = new HashMap<>();
        //insert each team into map with size 0
        if (gamemode == 1 || gamemode == 2) {
            for (int i = 0; i < 2; i++) {
                teamSizes.put(i, 0);
            }
        }
        //count team sizes
        for (Player player : players.values()) {
            teamSizes.put(player.team, teamSizes.getOrDefault(player.team, 0) + 1);
        }
        return teamSizes.entrySet().stream().min(Comparator.comparingInt(Map.Entry::getValue)).get().getKey();
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
                if (opp.id != pl.id && swp.collision(opp) && !((gamemode == 1 || gamemode == 2) && opp.team == pl.team)) {
                    if (opp.invincible_time == 0) {
                        double dmg_dealt = swp.damage * opp.defense_mult;
                        //blood class shit
                        if (opp.frenzy_time > 0) {
                            dmg_dealt *= 1.2;
                        }
                        opp.health -= dmg_dealt;
                        opp.stun_time = swp.stun_time;
                        //more blood class shit
                        if (pl.gameClass.equals("blood")) {
                            pl.health = Math.min(100.0, pl.health+10);
                            if (pl.frenzy_time > 0) {
                                pl.health = Math.min(100.0, pl.health+5);
                            }
                        }
                        opp.combat_time = 50;
                    }
                    if (opp.health <= 0.0) {
                        pl.killcount++;
                        removePlayer(oppws);
                        return;
                    }
                }
            }
        }
    }

    private void playerProjectileCollisions(WebSocket ws) {
        Player pl = players.get(ws);
        if (pl == null) return;
        for (Projectile proj : projectiles) {
            if (!proj.hitPlayers.contains(pl.id) && pl.collision(proj) && !((gamemode == 1 || gamemode == 2) && proj.myPlayer.team == pl.team)) {
                if (pl.invincible_time == 0) {
                    double dmg_dealt = proj.damage * pl.defense_mult;
                    //blood class shit
                    if (pl.frenzy_time > 0) {
                        dmg_dealt *= 1.2;
                    }
                    pl.health -= dmg_dealt;
                    //check projectile effects
                    pl.slow = proj.slow;
                    pl.slow_time = proj.slow_time;
                    pl.stun_time = proj.stun_time;
                }
                if (!proj.multhit) {
                    proj.hitPlayers.add(pl.id);
                }
                if (pl.health <= 0.0) {
                    proj.myPlayer.killcount++;
                    removePlayer(ws);
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
                //void pull pulls in player
                if (proj.type.equals("voidpull")) {
                    pl.x = proj.x+proj.x_vel; pl.y = proj.y+proj.y_vel;
                }
                //black hole sucks in player towards the center
                if (proj.type.equals("blackhole")) {
                    double pull_dir = Math.atan2(proj.y-pl.y, proj.x-pl.x);
                    double pull_dist = Math.hypot(proj.x-pl.x, proj.y-pl.y);
                    double pull_strength = 5+(250-pull_dist)/17;
                    pl.x += pull_strength*Math.cos(pull_dir);
                    pl.y += pull_strength*Math.sin(pull_dir);
                }
                pl.combat_time = 50;
            }
        }
    }

    public void getMessage(WebSocket ws, String msg) {
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
    public void handlePingMessage(WebSocket ws, JsonNode jsonNode) {
        // so uh apparently we have to craft json stuff like this
        // shouldnt be too big of a deal
        String msg = "{\"type\": \"ping\"}";
        ws.send(msg);
    }

    public void handleMovement(WebSocket ws, JsonNode jsonNode) {
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

    public void handleAttack(WebSocket ws, JsonNode jsonNode) {
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

    private void playerCapturePointInteractions() {
        int team0 = 0; int team1 = 0;
        for (Player pl : players.values()) {
            if (capturepoint.collision(pl)) {
                System.out.println("player" + pl.name + " at " + pl.x + " " + pl.y + " collided with capture point at " + capturepoint.x + " " + capturepoint.y);
                if (pl.team == 0) {
                    team0++;
                } else if (pl.team == 1) {
                    team1++;
                }
            }
        }
        String displayText = capturepoint.updateCaptureState(team0, team1);
        System.out.println(displayText);
        ObjectNode resp = objectMapper.createObjectNode();
        resp.put("type", "capturepoint");
        resp.put("x", capturepoint.x);
        resp.put("y", capturepoint.y);
        resp.put("radius", capturepoint.radius);
        resp.put("text", displayText);
        resp.put("captureState", capturepoint.captureState);
        resp.put("percentage", capturepoint.getBarPercentage());
        broadcast(resp.toString());
    }

    public void update() {
        for (WebSocket ws : players.keySet()) {
            Player pl = players.get(ws);
            pl.update(mapDim);
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
                ((LightningBall)p).computeHoming(players.values(), gamemode);
            } else if (p.type.equals("clusterfireball")) {
                ((ClusterFireball)p).computeHoming(players.values(), gamemode);
            } else if (p.type.equals("voidorb")) {
                ((VoidOrb)p).computeHoming(players.values(), gamemode);
            } else if (p.type.equals("voidpull")) {
                ((VoidPull)p).computeHoming();
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
        if (gamemode == 2) {
            playerCapturePointInteractions();
        }
    }

    public String packagePlayerData() {
        ObjectNode resp = objectMapper.createObjectNode();
        resp.put("type", "players");
        resp.set("players", objectMapper.valueToTree(
            players.values().stream().map(pl ->
                Map.ofEntries(
                    Map.entry("id", pl.id),
                    Map.entry("team", pl.team),
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
        return msg;
    }

    public String packageProjectileData() {
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
        return msg;
    }

    public String packageInitData() {
        ObjectNode resp = objectMapper.createObjectNode();
        resp.put("type", "gameStart");
        resp.set("obstacles", objectMapper.valueToTree(
            obstacles.stream().map(ob -> 
                Map.of(
                    "id", ob.id, 
                    "x", ob.x, 
                    "y", ob.y,
                    "radius", ob.radius)).toList())
        );
        resp.put("sessionId", sessionId);
        resp.put("gamemode", gamemode);
        resp.put("mapDim", mapDim);
        String msg = resp.toString();
        return msg;
    }

    public int getPlayerCount() {
        return players.size();
    }

}
