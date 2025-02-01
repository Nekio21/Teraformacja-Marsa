package umk.jakuburb.mars.Teraformacja.Marsa.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.CardSkills;

import java.io.Serializable;

public class Resources implements Serializable {

    @JsonProperty("goldProd")
    private int goldProd;
    @JsonProperty("gold")
    private int gold;
    @JsonProperty("energyProd")
    private int energyProd;
    @JsonProperty("energy")
    private int energy;
    @JsonProperty("heatProd")
    private int heatProd;
    @JsonProperty("heat")
    private int heat;
    @JsonProperty("metalProd")
    private int metalProd;
    @JsonProperty("metal")
    private int metal;
    @JsonProperty("plantsProd")
    private int plantsProd;
    @JsonProperty("plants")
    private int plants;
    @JsonProperty("titaniumProd")
    private int titaniumProd;
    @JsonProperty("titanium")
    private int titanium;

    public Resources() {
        this.goldProd = 1;
        this.gold = 25;
        this.energyProd = 1;
        this.energy = 0;
        this.heatProd = 1;
        this.heat = 0;
        this.metalProd = 1;
        this.metal = 0;
        this.plantsProd = 1;
        this.plants = 0;
        this.titaniumProd = 1;
        this.titanium = 0;
    }

    public Resources(int goldProd, int gold, int energyProd, int energy, int heatProd, int heat, int metalProd, int metal, int plantsProd, int plants, int titaniumProd, int titanium) {
        this.goldProd = goldProd;
        this.gold = gold;
        this.energyProd = energyProd;
        this.energy = energy;
        this.heatProd = heatProd;
        this.heat = heat;
        this.metalProd = metalProd;
        this.metal = metal;
        this.plantsProd = plantsProd;
        this.plants = plants;
        this.titaniumProd = titaniumProd;
        this.titanium = titanium;
    }

    public void put(CardSkills.Resource resource, int amount, boolean plus){
        switch(resource){
            case GOLD_PROD -> goldProd = goldProd + amount * (plus?1:-1);
            case GOLD -> gold = gold + amount * (plus?1:-1);
            case ENERGY_PROD -> energyProd = energyProd + amount * (plus?1:-1);
            case ENERGY -> energy = energy + amount * (plus?1:-1);
            case HEAT_PROD -> heatProd = heatProd + amount * (plus?1:-1);
            case HEAT -> heat = heat + amount * (plus?1:-1);
            case METAL_PROD -> metalProd = metalProd + amount * (plus?1:-1);
            case METAL -> metal = metal + amount * (plus?1:-1);
            case PLANTS_PROD -> plantsProd = plantsProd + amount * (plus?1:-1);
            case PLANTS -> plants = plants + amount * (plus?1:-1);
            case TITANIUM_PROD -> titaniumProd = titaniumProd + amount * (plus?1:-1);
            case TITANIUM -> titanium = titanium + amount * (plus?1:-1);
        }
    }

    public int getGoldProd() {
        return goldProd;
    }

    public void setGoldProd(int goldProd) {
        this.goldProd = goldProd;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public int getEnergyProd() {
        return energyProd;
    }

    public void setEnergyProd(int energyProd) {
        this.energyProd = energyProd;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public int getHeatProd() {
        return heatProd;
    }

    public void setHeatProd(int heatProd) {
        this.heatProd = heatProd;
    }

    public int getHeat() {
        return heat;
    }

    public void setHeat(int heat) {
        this.heat = heat;
    }

    public int getMetalProd() {
        return metalProd;
    }

    public void setMetalProd(int metalProd) {
        this.metalProd = metalProd;
    }

    public int getMetal() {
        return metal;
    }

    public void setMetal(int metal) {
        this.metal = metal;
    }

    public int getPlantsProd() {
        return plantsProd;
    }

    public void setPlantsProd(int plantsProd) {
        this.plantsProd = plantsProd;
    }

    public int getPlants() {
        return plants;
    }

    public void setPlants(int plants) {
        this.plants = plants;
    }

    public int getTitaniumProd() {
        return titaniumProd;
    }

    public void setTitaniumProd(int titaniumProd) {
        this.titaniumProd = titaniumProd;
    }

    public int getTitanium() {
        return titanium;
    }

    public void setTitanium(int titanium) {
        this.titanium = titanium;
    }

    @Override
    public String toString() {
        return "Resources{" +
                "goldProd=" + goldProd +
                ", gold=" + gold +
                ", energyProd=" + energyProd +
                ", energy=" + energy +
                ", heatProd=" + heatProd +
                ", heat=" + heat +
                ", metalProd=" + metalProd +
                ", metal=" + metal +
                ", plantsProd=" + plantsProd +
                ", plants=" + plants +
                ", titaniumProd=" + titaniumProd +
                ", titanium=" + titanium +
                '}';
    }
}
