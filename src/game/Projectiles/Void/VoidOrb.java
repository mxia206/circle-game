package game.Projectiles;

import java.util.Collection;

import game.*;


public class VoidOrb extends Projectile {
    public VoidOrb(String id, double x_pos, double y_pos, double dir, Player pl) {
        super(id, x_pos, y_pos, 40.0*Math.cos(dir), 40.0*Math.sin(dir), 40.0, pl);
        this.time = 30;
        this.radius = 10.0;
        this.damage = 3.0;
        this.type = "voidorb";
    }

    public void update() {
        if (time % 7 == 0) {
            this.hitPlayers.clear();
            this.hitPlayers.add(this.myPlayer.id);
        }
        super.update();
    }

    public void computeHoming(Collection<Player> c, int gamemode) {
        Player closestPlayer = null; double dist = 1500.0;
        for (Player pl : c) {
            if (gamemode == 1 && pl.team == this.myPlayer.team) continue;
            if (pl.id != this.myPlayer.id && Math.hypot(pl.x-this.x, pl.y-this.y) < dist) {
                closestPlayer = pl;
                dist =  Math.hypot(pl.x-this.x, pl.y-this.y);
            }
        }
        if (closestPlayer == null) return;
        double dir = Math.atan2(this.y_vel, this.x_vel);
        double targetdir = Math.atan2(closestPlayer.y-this.y, closestPlayer.x-this.x);
        double dirchange = wrapAngle(targetdir-dir);
        double angular_vel = 0.5;
        dirchange = Math.max(-angular_vel, Math.min(angular_vel, dirchange));
        this.x_vel = (max_vel*Math.cos(dir+dirchange));
        this.y_vel = (max_vel*Math.sin(dir+dirchange));
    }

}
