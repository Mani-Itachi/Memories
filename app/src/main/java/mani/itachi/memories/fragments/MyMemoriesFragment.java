package mani.itachi.memories.fragments;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import mani.itachi.memories.MainActivity;
import mani.itachi.memories.R;
import mani.itachi.memories.database.DbHelper;
import mani.itachi.memories.database.MemoryDto;
import mani.itachi.memories.recyclerviewparts.ItemclickSupport;
import mani.itachi.memories.recyclerviewparts.MemoryListAdapter;
import mani.itachi.memories.recyclerviewparts.RecyclerViewEmptyExtdener;
import mani.itachi.memories.uicomponents.CommonAdapter;
import mani.itachi.memories.utils.Utils;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyMemoriesFragment extends Fragment implements CommonAdapter.OnGetViewListener {

    MemoryListAdapter memoryListAdapter;
    RecyclerViewEmptyExtdener mRecyclerView;
    TextView mEmptyView;
    List<MemoryDto> nameList;
    ProgressDialog progressDialog;
    int temp = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle("My Cards");
        nameList = DbHelper.getInstance().getAllCards();
        Collections.reverse(nameList);
        View view = inflater.inflate(R.layout.fragment_my_memories, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View v) {
        mRecyclerView = v.findViewById(R.id.pokemonRecyclerView);
        mEmptyView = v.findViewById(R.id.emptyRecyclerView);
        mEmptyView.setText("No Memories :(");
        mRecyclerView.setEmptyView(mEmptyView);
        if (nameList.isEmpty()) {
            mEmptyView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
        memoryListAdapter = new MemoryListAdapter(getActivity(), nameList);
        mRecyclerView.setAdapter(memoryListAdapter);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        ItemclickSupport.addTo(mRecyclerView).setOnItemLongClickListener(new ItemclickSupport.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClicked(RecyclerView recyclerView, final int position, View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Select action");
                final CommonAdapter<String> commonAdapter = new CommonAdapter<>(MyMemoriesFragment.this);
                final List<String> list = new ArrayList<>();
                list.add("Delete this card");
                commonAdapter.setList(list);
                builder.setAdapter(commonAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (list.get(i).equals("Delete this card")) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Are you sure??");
                            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    progressDialog = new ProgressDialog(getActivity());
                                    progressDialog.setTitle("Deleting the card");
                                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                    progressDialog.setCancelable(false);
                                    progressDialog.show();
                                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                                    executorService.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                @Override
                                                public void onDismiss(DialogInterface dialogInterface) {
                                                    dismissListener();
                                                }
                                            });
                                            temp = DbHelper.getInstance().deleteCard(memoryListAdapter.
                                                    getMemList().get(position).getId());
                                            Log.d("TAG", "temp" + temp + "");
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    nameList = DbHelper.getInstance().getAllCards();
                                                    memoryListAdapter.setMemList(nameList);
                                                    memoryListAdapter.notifyDataSetChanged();
                                                }
                                            });
                                            progressDialog.dismiss();
                                        }
                                    });
                                    executorService.shutdown();
                                }
                            });
                            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                            builder.show();
                        }
                    }
                });
                builder.show();
                return true;
            }
        });
        ItemclickSupport.addTo(mRecyclerView).setOnItemClickListener(new ItemclickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                MemoryDto clickedPokemon = memoryListAdapter.getMemList().get(position);
                MainActivity mainActivity = (MainActivity) getActivity();
                Bundle bundle = new Bundle();
                bundle.putSerializable("MemoryDto", clickedPokemon);
                Log.v("calling", clickedPokemon.getName());
                MemoryDetailFragment detailFragment = new MemoryDetailFragment();
                detailFragment.setArguments(bundle);
                Utils.hideKeyboard(mainActivity);
                mainActivity.setFragment(detailFragment);
            }
        });
    }

    private void dismissListener() {
        if (temp == 0) {
            Toast.makeText(getActivity(), "Oops we couldn't delete the pokemon.", Toast.LENGTH_SHORT).show();
        } else {
            memoryListAdapter.notifyDataSetChanged();
        }
    }

    public void search(String query) {
        Set<MemoryDto> newSet = new HashSet<>();
        for (MemoryDto p : nameList) {
            if (p.getName().toLowerCase().contains(query.toLowerCase())) {
                newSet.add(p);
            }
        }
        memoryListAdapter.setMemList(new ArrayList<MemoryDto>(newSet));
        memoryListAdapter.notifyDataSetChanged();
    }

    @Override
    public View getView(View convertView, Object item, int position) {

        MyDialogViewHolder myDialogViewHolder;
        if (convertView == null) {
            myDialogViewHolder = new MyDialogViewHolder();
            convertView = LayoutInflater.from(getActivity()).inflate(R.layout.activity_imagepicker, null);
            myDialogViewHolder.mTextView = convertView.findViewById(R.id.dialogListText);
            myDialogViewHolder.mImageView = convertView.findViewById(R.id.dialogListImage);
            convertView.setTag(myDialogViewHolder);
        } else {
            myDialogViewHolder = (MyDialogViewHolder) convertView.getTag();
        }
        myDialogViewHolder.mTextView.setText("Delete this card");
        myDialogViewHolder.mImageView.setImageDrawable(getResources().getDrawable(R.drawable.delete));
        return convertView;

    }

    private class MyDialogViewHolder {
        TextView mTextView;
        ImageView mImageView;
    }
}
