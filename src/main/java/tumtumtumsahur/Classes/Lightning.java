package tumtumtumsahur.Classes;

import java.util.*;

import tumtumtumsahur.*;
import tumtumtumsahur.Projectiles.*;
/**
 * Blood class
 */
public class Lightning extends Player {

    public Lightning(String id, String name, double x, double y) {
        super(id,name, x, y);
        mana_regen = 2.0;
        health_regen = 0.5;
        this.gameClass = "lightning";
        
        this.skill1maxcd = 70;
        this.skill2maxcd = 150;
        this.skill3maxcd = 40;
    }

    //weakened melee attack
    public Sweep basicMelee(double dir) {
        if (this.lightingspeed_time > 0) {
            return null;
        }
        if (basicMeleeCD > 0) return null;
        this.basicMeleeCD += 5;
        return new Sweep(this.x, this.y, dir, 100.0, Math.PI*1.2, 15.0);
    }

    //homing stunning lightning ball
    public Set<Projectile> skill_1 (double dir) {
        if (this.lightingspeed_time > 0) {
            return null;
        }
        double manacost =20.0;
        if (mana <= manacost || skill1cd > 0) {
            return null;
        }
        skill1cd = skill1maxcd;
        mana -= manacost;
        this.stun_time = 5;
        return new HashSet<Projectile> (Arrays.asList(new LightningBall(UUID.randomUUID().toString(), this.x, this.y, dir, this)));
    }

    //lightning speed
    public Set<Projectile>  skill_2 (double dir) {
        double manacost =30.0;
        if (mana <= manacost || skill2cd > 0) {
            return null;
        }
        skill2cd = skill2maxcd;
        mana -= manacost;
        this.lightingspeed_time = 15;
        return null;
    }

    //snipe
    public Set<Projectile>  skill_3 (double dir) {
        if (this.lightingspeed_time > 0) {
            return null;
        }
        double manacost = 50.0;
        if (mana <= manacost || skill3cd > 0) {
            return null;
        }
        skill3cd = skill3maxcd;
        mana -= manacost;
        this.stun_time = 5;
        return new HashSet<Projectile> (Arrays.asList(new LightningBolt(UUID.randomUUID().toString(), this.x, this.y, dir, this)));

    }

    //cc immune of lightning speed
    public void update() {
        if (this.lightingspeed_time > 0) {
            this.stun_time = 0;
            this.slow_time = 0;
        }
        super.update();
    }

}
