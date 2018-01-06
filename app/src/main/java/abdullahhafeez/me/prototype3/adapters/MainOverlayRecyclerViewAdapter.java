package abdullahhafeez.me.prototype3.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

import abdullahhafeez.me.prototype3.R;

/**
 * Created by Abdullah on 11/5/2017.
 */

public class MainOverlayRecyclerViewAdapter extends RecyclerView.Adapter<MainOverlayRecyclerViewAdapter.MyViewHolder> {

    private Context mContext;
    private ArrayList<Integer> overlayList;
    private CardView previousView;


    public MainOverlayRecyclerViewAdapter(Context context, ArrayList<Integer> overlayList) {
        this.overlayList = overlayList;
        this.mContext = context;
    }

    @Override
    public MainOverlayRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.main_overlay_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MainOverlayRecyclerViewAdapter.MyViewHolder holder, int position) {

        holder.mainOverlay.setImageResource(overlayList.get(position));

        holder.mainOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (previousView != null)
                    previousView.setBackgroundColor(Color.parseColor("#ffffff"));

                holder.mainOverlayCardView.setBackgroundColor(Color.parseColor("#FFDD9A"));
                previousView = holder.mainOverlayCardView;
            }
        });


        //Glide.with(mContext).load(overlayList.get(position)).apply(RequestOptions.circleCropTransform()).into(holder.mainOverlay);

    }

    @Override
    public int getItemCount() {
        return overlayList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView mainOverlay;
        private CardView mainOverlayCardView;

        public MyViewHolder(View itemView) {
            super(itemView);
            mainOverlay = (ImageView) itemView.findViewById(R.id.mainOverlay);
            mainOverlayCardView = (CardView) itemView.findViewById(R.id.mainOverlayCardView);
        }


    }


}
