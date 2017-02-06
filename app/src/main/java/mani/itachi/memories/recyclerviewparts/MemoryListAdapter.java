package mani.itachi.memories.recyclerviewparts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import mani.itachi.memories.R;
import mani.itachi.memories.database.MemoryDto;

/**
 * Created by ManikantaInugurthi on 03-02-2017.
 */

public class MemoryListAdapter extends RecyclerView.Adapter<MemoryListAdapter.MyViewHolder> {

    List<MemoryDto> memList;
    Context mContext;
    Bitmap memImage;

    public MemoryListAdapter(Context c, List<MemoryDto> memList) {
        this.memList = memList;
        mContext = c;
    }

    public List<MemoryDto> getMemList() {
        return memList;
    }

    public void setMemList(List<MemoryDto> memList) {
        this.memList = memList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.name.setText(memList.get(position).getName());
        if (memList.get(position).getImagePath().equals("-1")) {
            String fname=memList.get(position).getBitmapPath();
            fname=fname.substring(0,fname.length()-4);
            fname=fname+"thumb.png";
            memImage= BitmapFactory.decodeFile(fname);
        } else {
            memImage = BitmapFactory.decodeResource(mContext.getResources(), mContext.getResources()
                    .getIdentifier(memList.get(position).getImagePath(), "drawable", mContext.
                            getPackageName()));
        }
        holder.image.setImageBitmap(memImage);
    }

    @Override
    public int getItemCount() {
        return memList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView image;

        public MyViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.pokemonItemName);
            image = (ImageView) itemView.findViewById(R.id.pokemonItemImage);
        }
    }
}
