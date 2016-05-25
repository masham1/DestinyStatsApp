package com.masham31.destinystats;

/**
 * Created by admin on 12/04/2016.
 */
public class StatsClass {
    public String kdDisplay, combatDisplay;
    public float kdActual, combatActual;
    public int id;

    public StatsClass() {
        kdDisplay = "";
        kdActual = 0;
        combatDisplay = "";
        combatActual = 0;
        id = 0;
    }

    public StatsClass(int intId, String strKd, float dbKd, String strCombat, float dbCombat){
        this.id = intId;
        this.kdDisplay = strKd;
        this.kdActual = dbKd;
        this.combatDisplay = strCombat;
        this.combatActual = dbCombat;
    }

    public int getId() { return id; }

    public String getKdDisplay() { return kdDisplay; }

    public float getKdActual() { return kdActual; }

    public String getCombatDisplay() { return combatDisplay; }

    public float getCombatActual() { return combatActual; }

    public void setId(int intId) { this.id = intId; }

    public void setKdDisplay(String strKd) {
        this.kdDisplay = strKd;
    }

    public void setKdActual (float dbKd) {
        this.kdActual = dbKd;
    }

    public void setCombatDisplay(String strCombat) {
        this.combatDisplay = strCombat;
    }

    public void setCombatActual(float dbCombat) {
        this.combatActual = dbCombat;
    }
}
