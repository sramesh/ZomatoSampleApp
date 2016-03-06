package com.zomatosampleapp.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.zomatosampleapp.R;
import com.zomatosampleapp.model.AllDetailsOnLocation;
import com.zomatosampleapp.model.BestRatedRestaurant;



/**
 * Created by Ramesh on 5/3/16.
 */
public class RestaurantsAdapter extends RecyclerView.Adapter<RestaurantsAdapter.RestaurantsViewHolder>{
    private final Activity mContext;
    private final LayoutInflater mInflater;
    private List<BestRatedRestaurant> mRestaurantsList;
    private List<BestRatedRestaurant> visibleObjects;

    public RestaurantsAdapter(Activity context, List<BestRatedRestaurant> restaurantsList) {
        this.mRestaurantsList = new ArrayList<>(restaurantsList);
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);

    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public RestaurantsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = mInflater.inflate(R.layout.restaurant_details_layout, parent, false);
        return new RestaurantsViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(RestaurantsViewHolder holder, int position) {
        final BestRatedRestaurant bestRatedRestaurant = mRestaurantsList.get(position);

        if (bestRatedRestaurant.getRestaurant().getFeaturedImage() != null && !bestRatedRestaurant.getRestaurant().getFeaturedImage().isEmpty()){
            Picasso.with(mContext).load(bestRatedRestaurant.getRestaurant().getFeaturedImage()).placeholder(R.drawable.placeholder_rev).into(holder.restaurantImage);
        }

        holder.restaurantName.setText(bestRatedRestaurant.getRestaurant().getName());
        holder.restaurantLocation.setText(bestRatedRestaurant.getRestaurant().getLocation().getLocality());
        holder.restaurantPrice.setText("Avg cost for 2 : Rs " + bestRatedRestaurant.getRestaurant().getAverageCostForTwo() + "/-");
        holder.restaurantRating.setRating(Float.parseFloat(bestRatedRestaurant.getRestaurant().getUserRating().getAggregateRating()));
        holder.topCuisines.setText("Top cuisines : " + bestRatedRestaurant.getRestaurant().getCuisines());
        holder.votes.setText(bestRatedRestaurant.getRestaurant().getUserRating().getVotes() + " votes");
        holder.votes.setTextColor(Color.parseColor("#"+bestRatedRestaurant.getRestaurant().getUserRating().getRatingColor()));
        holder.getDirections.setTag(bestRatedRestaurant.getRestaurant().getLocation().getLatitude() + ":" + bestRatedRestaurant.getRestaurant().getLocation().getLongitude());

        holder.getDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String lat_lon = view.getTag().toString();
                    if (lat_lon != null) {
                        String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?daddr=%f,%f", Double.parseDouble(lat_lon.split(":")[0]), Double.parseDouble(lat_lon.split(":")[1]));
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                        mContext.startActivity(intent);
                    }
                } catch (Exception e) {

                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return mRestaurantsList.size();
    }


    class RestaurantsViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.restaurants_holder)
        CardView cardView;
        @InjectView(R.id.restaurant_image)
        ImageView restaurantImage;
        @InjectView(R.id.restaurant_name)
        TextView restaurantName;
        @InjectView(R.id.restaurant_location)
        TextView restaurantLocation;
        @InjectView(R.id.restaurant_price)
        TextView restaurantPrice;
        @InjectView(R.id.restaurant_rating)
        RatingBar restaurantRating;
        @InjectView(R.id.restaurant_topcuisines)
        TextView topCuisines;
        @InjectView(R.id.restaurant_votes)
        TextView votes;
        @InjectView(R.id.get_directions)
        Button getDirections;


        public RestaurantsViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }

    }

    public void animateTo(List<BestRatedRestaurant> restaurants) {
        if(restaurants.size() >1) {
            applyAndAnimateRemovals(restaurants);
            applyAndAnimateAdditions(restaurants);
            applyAndAnimateMovedItems(restaurants);
        }
    }

    private void applyAndAnimateRemovals(List<BestRatedRestaurant> newRestaurants) {
        for (int i = mRestaurantsList.size() - 1; i >= 0; i--) {
            final BestRatedRestaurant restaurant = mRestaurantsList.get(i);
            if (!newRestaurants.contains(restaurant)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<BestRatedRestaurant> newRestaurants) {
        for (int i = 0, count = newRestaurants.size(); i < count; i++) {
            final BestRatedRestaurant restaurant = newRestaurants.get(i);
            if (!mRestaurantsList.contains(restaurant)) {
                addItem(i, restaurant);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<BestRatedRestaurant> newRestaurants) {
        for (int toPosition = newRestaurants.size() - 1; toPosition >= 0; toPosition--) {
            final BestRatedRestaurant restaurant = newRestaurants.get(toPosition);
            final int fromPosition = mRestaurantsList.indexOf(restaurant);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    public BestRatedRestaurant removeItem(int position) {
        final BestRatedRestaurant restaurant = mRestaurantsList.remove(position);
        notifyItemRemoved(position);
        return restaurant;
    }

    public void addItem(int position, BestRatedRestaurant restaurant) {
        mRestaurantsList.add(position, restaurant);
        notifyItemInserted(position);
    }


    public void moveItem(int fromPosition, int toPosition) {
        final BestRatedRestaurant restaurant = mRestaurantsList.remove(fromPosition);
        mRestaurantsList.add(toPosition, restaurant);
        notifyItemMoved(fromPosition, toPosition);
    }


    public void updateData(List<BestRatedRestaurant> restaurantArrayList) {
        mRestaurantsList.clear();
        mRestaurantsList.addAll(restaurantArrayList);
        notifyDataSetChanged();
    }
}
