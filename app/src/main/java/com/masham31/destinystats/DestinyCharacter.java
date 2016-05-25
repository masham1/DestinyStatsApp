package com.masham31.destinystats;

/**
 * Created by admin on 20/03/2016.
 */
public class DestinyCharacter {
    public String classType, lightLevel, emblemPath, backgroundPath, characterId;

    public String getCharacterId() {return  this.characterId; }

    public String getClassType() {
        return this.classType;
    }

    public String getLight() {
        return this.lightLevel;
    }

    public String getEmblemPath() {
        return this.emblemPath;
    }

    public String getBackgroundPath() {
        return this.backgroundPath;
    }

    public void setClassType(String apiClass) {
        classType = apiClass;
    }

    public void setLightLevel(String apiLight) {
        lightLevel = apiLight;
    }

    public void setEmblemPath(String apiEmblem) {
        emblemPath = apiEmblem;
    }

    public void setBackgroundPath(String apiBackground) {
        backgroundPath = apiBackground;
    }

    public void setCharacterId(String apiCharId) {
        characterId = apiCharId;
    }

    @Override
    public String toString() {
        return "Emblem: " + getEmblemPath() + " BG: " + getBackgroundPath();


    }
}
