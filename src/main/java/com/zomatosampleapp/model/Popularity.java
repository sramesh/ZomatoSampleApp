package com.zomatosampleapp.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ramesh on 5/3/16.
 */
public class Popularity {

    @SerializedName("popularity")
    @Expose
    private String popularity;
    @SerializedName("nightlife_index")
    @Expose
    private String nightlifeIndex;
    @SerializedName("top_cuisines")
    @Expose
    private List<String> topCuisines = new ArrayList<>();

    /**
     *
     * @return
     * The popularity
     */
    public String getPopularity() {
        return popularity;
    }

    /**
     *
     * @param popularity
     * The popularity
     */
    public void setPopularity(String popularity) {
        this.popularity = popularity;
    }

    /**
     *
     * @return
     * The nightlifeIndex
     */
    public String getNightlifeIndex() {
        return nightlifeIndex;
    }

    /**
     *
     * @param nightlifeIndex
     * The nightlife_index
     */
    public void setNightlifeIndex(String nightlifeIndex) {
        this.nightlifeIndex = nightlifeIndex;
    }

    /**
     *
     * @return
     * The topCuisines
     */
    public List<String> getTopCuisines() {
        return topCuisines;
    }

    /**
     *
     * @param topCuisines
     * The top_cuisines
     */
    public void setTopCuisines(List<String> topCuisines) {
        this.topCuisines = topCuisines;
    }
}
