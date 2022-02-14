package com.tungsten.hmclpe.launcher.uis.game.download.right.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.tungsten.hmclpe.R;
import com.tungsten.hmclpe.launcher.MainActivity;
import com.tungsten.hmclpe.launcher.download.minecraft.fabric.FabricGameVersion;
import com.tungsten.hmclpe.launcher.download.minecraft.fabric.FabricLoaderVersion;
import com.tungsten.hmclpe.launcher.download.minecraft.forge.ForgeVersion;
import com.tungsten.hmclpe.launcher.list.download.minecraft.fabric.DownloadFabricListAdapter;
import com.tungsten.hmclpe.launcher.list.download.minecraft.forge.DownloadForgeListAdapter;
import com.tungsten.hmclpe.launcher.uis.tools.BaseUI;
import com.tungsten.hmclpe.utils.animation.CustomAnimationUtils;
import com.tungsten.hmclpe.utils.io.NetworkUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class DownloadFabricUI extends BaseUI implements View.OnClickListener {

    public LinearLayout downloadFabricUI;

    public String version;

    private LinearLayout hintLayout;

    private ListView fabricListView;
    private ProgressBar progressBar;
    private TextView back;

    private static final String LOADER_META_URL = "https://meta.fabricmc.net/v2/versions/loader";
    private static final String GAME_META_URL = "https://meta.fabricmc.net/v2/versions/game";

    public DownloadFabricUI(Context context, MainActivity activity) {
        super(context, activity);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        downloadFabricUI = activity.findViewById(R.id.ui_install_fabric_list);

        hintLayout = activity.findViewById(R.id.download_fabric_hint_layout);
        hintLayout.setOnClickListener(this);

        fabricListView = activity.findViewById(R.id.fabric_version_list);
        progressBar = activity.findViewById(R.id.loading_fabric_list_progress);
        back = activity.findViewById(R.id.back_to_install_ui_fabric);

        back.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        activity.showBarTitle(context.getResources().getString(R.string.fabric_list_ui_title),false,true);
        CustomAnimationUtils.showViewFromLeft(downloadFabricUI,activity,context,true);
        init();
    }

    @Override
    public void onStop() {
        super.onStop();
        CustomAnimationUtils.hideViewToLeft(downloadFabricUI,activity,context,true);
    }

    private void init(){
        new Thread(){
            @Override
            public void run() {
                loadingHandler.sendEmptyMessage(0);
                ArrayList<FabricGameVersion> gameVersions = new ArrayList<>();
                ArrayList<FabricLoaderVersion> loaderVersions = new ArrayList<>();
                try {
                    String gameResponse = NetworkUtils.doGet(NetworkUtils.toURL(GAME_META_URL));
                    Gson gson = new Gson();
                    FabricGameVersion[] fabricGameVersions = gson.fromJson(gameResponse, FabricGameVersion[].class);
                    gameVersions.addAll(Arrays.asList(fabricGameVersions));
                    ArrayList<String> mcVersions = new ArrayList<>();
                    for (FabricGameVersion version : gameVersions){
                        mcVersions.add(version.version);
                    }
                    String loaderResponse = NetworkUtils.doGet(NetworkUtils.toURL(LOADER_META_URL));
                    FabricLoaderVersion[] fabricLoaderVersions = gson.fromJson(loaderResponse, FabricLoaderVersion[].class);
                    loaderVersions.addAll(Arrays.asList(fabricLoaderVersions));
                    if (!mcVersions.contains(version)){
                        loadingHandler.sendEmptyMessage(2);
                    }
                    else {
                        DownloadFabricListAdapter downloadFabricListAdapter = new DownloadFabricListAdapter(context,activity,version,loaderVersions);
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fabricListView.setAdapter(downloadFabricListAdapter);
                            }
                        });
                        loadingHandler.sendEmptyMessage(1);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void onClick(View view) {
        if (view == hintLayout){
            Uri uri = Uri.parse("https://afdian.net/@bangbang93");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        }
        if (view == back){
            activity.backToLastUI();
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler loadingHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0){
                fabricListView.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                back.setVisibility(View.GONE);
            }
            if (msg.what == 1){
                fabricListView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                back.setVisibility(View.GONE);
            }
            if (msg.what == 2){
                fabricListView.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                back.setVisibility(View.VISIBLE);
            }
        }
    };
}
