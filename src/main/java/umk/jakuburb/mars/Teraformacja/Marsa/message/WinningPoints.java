package umk.jakuburb.mars.Teraformacja.Marsa.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.CardSkills;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class WinningPoints implements Serializable {
    @JsonProperty("temp")
    private int temp;
    @JsonProperty("ocean")
    private int ocean;
    @JsonProperty("oxygen")
    private int oxygen;

    @JsonProperty("winTemp")
    private int winTemp;
    @JsonProperty("winOxygen")
    private int winOxygen;
    @JsonProperty("winOcean")
    private int winOcean;

    public WinningPoints() {
        temp = 0;
        ocean = 0;
        oxygen = 0;
        winTemp = 14;
        winOxygen = 14;
        winOcean = 9;
    }

    public WinningPoints(int temp, int ocean, int oxygen, int winTemp, int winOxygen, int winOcean) {
        this.temp = temp;
        this.ocean = ocean;
        this.oxygen = oxygen;
        this.winTemp = winTemp;
        this.winOxygen = winOxygen;
        this.winOcean = winOcean;
    }

    public Error put(CardSkills.Resource resource, int amount, boolean plus, AtomicInteger pz){
        int value = 0;
        switch(resource){
            case TEMP -> {
                value = temp + amount * (plus?1:-1);
                if(value < 0) return Error.TEMP;
                else if(value > winTemp){
                    value = winTemp;
                    pz.set(winTemp - temp);
                }
                else if(plus == true){
                    pz.set(amount);
                }
                temp = value;
            }
            case OCEAN -> {
                value = ocean + amount * (plus?1:-1);
                if(value < 0) return Error.OCEAN;
                else if(value > winOcean){
                    value = winOcean;
                    pz.set(winOcean - ocean);
                }
                else if(plus == true){
                    pz.set(amount);
                }
                ocean = value;
            }
            case OXYGEN -> {
                value = oxygen + amount * (plus?1:-1);
                if(value < 0) return Error.OXYGEN;
                else if(value > winOxygen){
                    value = winOxygen;
                    pz.set(winOxygen - oxygen);
                }
                else if(plus == true){
                    pz.set(amount);
                }
                oxygen = value;
            }
        }
        return Error.NO_ERROR;
    }

    public boolean winBool(){

        if(temp < winTemp){
            return false;
        }
        else if(ocean < winOcean){
            return false;
        }
        else if(oxygen < winOxygen){
            return false;
        }

        return true;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public int getOcean() {
        return ocean;
    }

    public void setOcean(int ocean) {
        this.ocean = ocean;
    }

    public int getOxygen() {
        return oxygen;
    }

    public void setOxygen(int oxygen) {
        this.oxygen = oxygen;
    }

    public int getWinTemp() {
        return winTemp;
    }

    public void setWinTemp(int winTemp) {
        this.winTemp = winTemp;
    }

    public int getWinOxygen() {
        return winOxygen;
    }

    public void setWinOxygen(int winOxygen) {
        this.winOxygen = winOxygen;
    }

    public int getWinOcean() {
        return winOcean;
    }

    public void setWinOcean(int winOcean) {
        this.winOcean = winOcean;
    }
}
