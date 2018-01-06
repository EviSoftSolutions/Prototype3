package abdullahhafeez.me.prototype3.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import abdullahhafeez.me.prototype3.R;
import abdullahhafeez.me.prototype3.data.StoreOverlay;

/**
 * Created by Abdullah on 11/22/2017.
 */

public class StoreOverlayAdapter extends RecyclerView.Adapter<StoreOverlayAdapter.MyViewHolder> implements Filterable {

    private ArrayList<StoreOverlay> storeOverlayList;
    private ArrayList<StoreOverlay> storeOverlayFilteredList;
    private Context context;

    public StoreOverlayAdapter(Context context, ArrayList<StoreOverlay> storeOverlayList) {

        this.context = context;
        this.storeOverlayList = storeOverlayList;
        this.storeOverlayFilteredList = storeOverlayList;

    }



    @Override
    public StoreOverlayAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.overlay_store_item_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(StoreOverlayAdapter.MyViewHolder holder, int position) {

        Glide.with(context).load(storeOverlayFilteredList.get(position).getOverlay()).into(holder.overlay_imageview);

        holder.overlay_name_text.setText(storeOverlayFilteredList.get(position).getOverlayName());



    }

    @Override
    public int getItemCount() {
        return storeOverlayFilteredList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();

                if (charString.isEmpty()) {

                    storeOverlayFilteredList = storeOverlayList;
                } else {

                    ArrayList<StoreOverlay> filteredList = new ArrayList<>();


                    for (StoreOverlay overlayList : storeOverlayList) {

                        if (overlayList.getOverlayName().toLowerCase().contains(charString)) {

                            filteredList.add(overlayList);
                        }
                    }

                    storeOverlayFilteredList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = storeOverlayFilteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                storeOverlayFilteredList = (ArrayList<StoreOverlay>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView overlay_imageview;
        public TextView overlay_name_text;

        public MyViewHolder(View view) {
            super(view);
            overlay_imageview = (ImageView) view.findViewById(R.id.overlay_imageview);
            overlay_name_text = (TextView) view.findViewById(R.id.overlay_name_text);

        }
    }


}
