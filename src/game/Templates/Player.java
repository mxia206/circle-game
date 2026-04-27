package game;

import java.util.*;

/**
 * Template for player classes
 */
public abstract class Player extends Circle {    
    //attributes
    String name; 
    public String gameClass;
    public int team = 0;

    //stats
    public double health = 100.0;
    public double mana = 100.0;
    public double mana_regen;
    public double health_regen;
    public double defense_mult = 1.0;

    //status
    public double last_dir;
    public double dir;
    public double timeFromLastHit;
    public boolean isHitting = false;
    public double slow;

    //timers
    public int skill1cd = 0;
    public int skill2cd = 0;
    public int skill3cd = 0;
    public int skill1maxcd;
    public int skill2maxcd;
    public int skill3maxcd;
    public int basicMeleeCD = 0;
    public int combat_time = 0;
    public int slow_time = 0;
    public int stun_time = 0;
    public int invincible_time = 0;
    public int frenzy_time = 0;
    public int lightingspeed_time = 0;

    public int killcount = 0;

    //earth class shenanigans
    public boolean basicEnhanced = false;


    /**
     * constructs Player
     * @param id id
     * @param name player username
     * @param x x position
     * @param y y position
     */
    public Player(String id, String name, double x, double y) {
        super(id, x, y);
        this.name = name;
        this.max_vel = 30.0;
        this.radius = 20.0;
    }

    //if type is projectile return projectile set, if melee return sweep
    /**
     * player 1st skill
     * @param dir direction of skill
     * @return set of generated projectiles
     */
    public abstract Set<Projectile> skill_1(double dir);

    /**
     * player 2nd skill
     * @param dir direction of skill
     * @return set of generated projectiles
     */
    public abstract Set<Projectile> skill_2(double dir);

    /**
     * player 3rd skill
     * @param dir direction of skill
     * @return set of generated projectiles
     */
    public abstract Set<Projectile> skill_3(double dir);

    /**
     * basic attack
     * @param dir direction of attack
     * @return generated sweep
     */
    public Sweep basicMelee(double dir) {
        if (basicMeleeCD > 0) return null;
        this.basicMeleeCD += 8;
        return new Sweep(this.x, this.y, dir, 100.0, Math.PI*1.2, 20.0);
    }


    /**
     * updates all stats and position
     */
    public void update(int mapDim) {
        double tx_vel = this.x_vel; double ty_vel = this.y_vel;
        if (this.slow_time > 0) {
            tx_vel *= slow;
            ty_vel *= slow;
        }
        if (this.frenzy_time > 0) {
            tx_vel *= 1.3;
            ty_vel *= 1.3;
        }
        if (this.lightingspeed_time > 0) {
            tx_vel *= 1.15;
            ty_vel *= 1.15;
        }
        if (this.stun_time > 0) {
            tx_vel = 0;
            ty_vel = 0;
        }
        //update
        last_x = x;
        last_y = y;
        x += tx_vel;
        y += ty_vel;
        //map bounds
        if (x >= mapDim) {
            x = mapDim;
        }
        if (x <= 0) {
            x = 0;
        }
        if (y >= mapDim) {
            y = mapDim;
        }
        if (y <= 0) {
            y = 0;
        }
        //stat updates
        if (mana < 100.0) {
            mana = Math.min(100.0, mana+mana_regen);
        }
        if (isHitting && System.currentTimeMillis() - timeFromLastHit >= 400) {
            isHitting = false;
        }  
        if (health < 100.0 && (combat_time == 0 || gameClass.equals("blood"))) {
            health = Math.min(100.0, health+health_regen);
        }
        if (skill1cd > 0) {
            skill1cd--;
        }
        if (skill2cd > 0) {
            skill2cd--;
        }
        if (skill3cd > 0) {
            skill3cd--;
        }
        if (basicMeleeCD > 0) {
            basicMeleeCD--;
        }
        if (slow_time > 0) {
            slow_time--;
        }
        if (stun_time > 0) {
            stun_time--;
        }
        if (invincible_time > 0) {
            invincible_time--;
        }
        if (frenzy_time > 0) {
            frenzy_time--;
        }
        if (lightingspeed_time > 0) {
            lightingspeed_time--;
        }
        if (combat_time > 0) {
            combat_time--;
        }
    }

    public void obstacleCollision(Obstacle ob) {
        //change position to tangential point
        double distFromCenter = Math.hypot(x-ob.x, y-ob.y);
        double scale = (ob.radius+radius)/distFromCenter;
        double new_x = ob.x+(x-ob.x)*scale;
        double new_y = ob.y+(y-ob.y)*scale;
        x = new_x;
        y = new_y;
    }

}
