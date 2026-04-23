package game.Classes;

import java.util.*;

import game.*;
import game.Projectiles.*;

/**
 * Earth class
 */
public class Earth extends Player {

    public Earth(String id, String name, double x, double y) {
        super(id,name, x, y);
        mana_regen = 2.0;
        health_regen = 0.5;
        this.gameClass = "earth";
        this.max_vel = 33.0;
        this.defense_mult = 0.8;

        this.skill1maxcd = 30;
        this.skill2maxcd = 50;
        this.skill3maxcd = 100;
    }

     //enhanced melee attack
     public Sweep basicMelee(double dir) {
        if (basicMeleeCD > 0) return null;
        this.basicMeleeCD += 5;
        if (basicEnhanced) {
            Sweep res = new Sweep(this.x, this.y, dir, 100.0, Math.PI*1.2, 30.0);
            res.stun_time = 10;
            skill1cd = skill1maxcd;
            basicEnhanced = false;
            return res;
        }
        return new Sweep(this.x, this.y, dir, 100.0, Math.PI*1.2, 20.0);
    }

    //enhance basic attack to stun +damage
    public Set<Projectile> skill_1 (double dir) {
        double manacost = 20.0;
        if (mana <= manacost || skill1cd > 0) {
            return null;
        }
        mana -= manacost;
        basicEnhanced = true;
        return null;
    }

    //stunning shockwave
    public Set<Projectile>  skill_2 (double dir) {
        double manacost = 50.0;
        if (mana <= manacost || skill2cd > 0) {
            return null;
        }
        skill2cd = skill2maxcd;
        mana -= manacost;
        String projectileId = UUID.randomUUID().toString();
        return new HashSet<Projectile> (Arrays.asList(new Shockwave(projectileId, this.x, this.y, dir, this)));
    }

    //temporary invincible
    public Set<Projectile>  skill_3 (double dir) {
        double manacost = 50.0;
        if (mana <= manacost || skill3cd > 0) {
            return null;
        }
        skill3cd = skill3maxcd;
        mana -= manacost;
        this.invincible_time = 20;
        return null;
    }

}
