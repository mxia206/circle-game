package game.Projectiles;

import java.util.Collection;

import game.*;


public class LightningBall extends Projectile {
    public LightningBall(String id, double x_pos, double y_pos, double dir, Player pl) {
        super(id, x_pos, y_pos, 40.0*Math.cos(dir), 40.0*Math.sin(dir), 40.0, pl);
        this.time = 30;
        this.radius = 30.0;
        this.damage = 30.0;
        this.stun_time = 15;
        this.type = "lightningball";
    }

    private double wrapAngle(double angle) {
        while (angle > Math.PI) angle -= (double)(2 * Math.PI);
        while (angle < -Math.PI) angle += (double)(2 * Math.PI);
        return angle;
    }

    public void computeHoming(Collection<Player> c, int gamemode) {
        Player closestPlayer = null; double dist = 10000.0;
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
        double angular_vel = 0.25;
        dirchange = Math.max(-angular_vel, Math.min(angular_vel, dirchange));
        this.x_vel = (max_vel*Math.cos(dir+dirchange));
        this.y_vel = (max_vel*Math.sin(dir+dirchange));
    }

}
