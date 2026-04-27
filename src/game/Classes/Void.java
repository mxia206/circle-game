package game.Classes;

import java.util.*;

import game.*;
import game.Projectiles.*;

/**
 * Void class
 */
public class Void extends Player {

    public Void(String id, String name, double x, double y) {
        super(id,name, x, y);
        mana_regen = 2.0;
        health_regen = 0.5;
        this.gameClass = "void";

        this.skill1maxcd = 8;
        this.skill2maxcd = 50;
        this.skill3maxcd = 200;
    }
    

    //void orbs
    public Set<Projectile> skill_1 (double dir) {
        double manacost = 25.0;
        if (mana <= manacost || skill1cd > 0) {
            return null;
        }
        skill1cd = skill1maxcd;
        mana -= manacost;
        String projectileId = UUID.randomUUID().toString();
        Set<Projectile> res = new HashSet<Projectile> ();
        for (int i = -1; i <= 1; i++) {
            res.add(new VoidOrb(UUID.randomUUID().toString(),x,y,dir+2*i*Math.PI/3, this)); 
        }
        return res;
    }

    //large void pull
    public Set<Projectile>  skill_2 (double dir) {
        double manacost = 50.0;
        if (mana <= manacost || skill2cd > 0) {
            return null;
        }
        skill2cd = skill2maxcd;
        mana -= manacost;
        String projectileId = UUID.randomUUID().toString();
        return new HashSet<Projectile> (Arrays.asList(new VoidPull(projectileId, this.x+800*Math.cos(dir), this.y+800*Math.sin(dir), dir, this)));
    }

    //black hole
    public Set<Projectile>  skill_3 (double dir) {
        double manacost = 70.0;
        if (mana <= manacost || skill3cd > 0) {
            return null;
        }
        skill3cd = skill3maxcd;
        mana -= manacost;
        String projectileId = UUID.randomUUID().toString();
        return new HashSet<Projectile> (Arrays.asList(new BlackHole(projectileId, this.x, this.y, dir, this)));
    }

}
