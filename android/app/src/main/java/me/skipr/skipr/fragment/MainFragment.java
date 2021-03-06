package me.skipr.skipr.fragment;

import android.app.Fragment;
import android.app.Instrumentation;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.andtinder.model.CardModel;
import com.andtinder.model.Orientations;
import com.andtinder.view.CardContainer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.skipr.skipr.R;
import me.skipr.skipr.StackAdapter;
import me.skipr.skipr.api.Constants;
import me.skipr.skipr.api.SkiprApi;
import me.skipr.skipr.api.Track;
import me.skipr.skipr.api.VotedTrack;
import me.skipr.skipr.model.SongCard;
import me.skipr.skipr.util.UserUtil;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.android.AndroidLog;
import retrofit.client.Response;

/**
 * Created by Navjot on 6/13/2015.
 */
public class MainFragment extends BasePlayerActivity{


    private CardContainer mCardContainer;
    private TextView mRoomNameTextview;
    private StackAdapter mCardAdapter;
    private Drawable defaultDrawable;
    private List<VotedTrack> mVotedTracks = new ArrayList<VotedTrack>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mRoomId = getArguments().getString("room_id", "");
        mRoomName = getArguments().getString("room_name", "");

        Boolean isPlayer = getArguments().getBoolean("isPlayer", false);
        String token = getArguments().getString("token", "");
        if(isPlayer && !TextUtils.isEmpty(token)){
            startPlaying(token);
        }

        // at this point, the user should have selected a group to join
        if(TextUtils.isEmpty(mRoomId) || TextUtils.isEmpty(mRoomName)){
            Toast.makeText(getActivity(), "Error: not in a room", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mCardContainer = (CardContainer) rootView.findViewById(R.id.card_container);
        mCardContainer.setOrientation(Orientations.Orientation.Ordered);
        mCardAdapter = new StackAdapter(getActivity());

        mRoomNameTextview = (TextView) rootView.findViewById(R.id.room_name);
        mRoomNameTextview.setText(mRoomName);

        defaultDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.picture1, null);

        rootView.findViewById(R.id.like_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCardAdapter.getCount() > 0) {
                    SongCard cardModel = (SongCard) mCardAdapter.pop();
                    reportLike(cardModel.getUniqueId());
                    mCardAdapter.notifyDataSetChanged();
                }
            }
        });
        rootView.findViewById(R.id.dislike_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCardAdapter.getCount() > 0) {
                    SongCard cardModel = (SongCard) mCardAdapter.pop();
                    reportDislike(cardModel.getUniqueId());
                    mCardAdapter.notifyDataSetChanged();
                }
            }
        });


        requestSongs();
        return rootView;
    }

    public static MainFragment newInstance(String roomId, String roomName, String token, Boolean isPlayer){
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString("room_id", roomId);
        args.putString("room_name", roomName);
        args.putString("token", token);
        args.putBoolean("isPlayer", isPlayer);
        Log.d("Skipr", roomId);
        fragment.setArguments(args);
        return fragment;
    }

    private void addSongToQueue(String uniqueId, String songTitle, String songAuthor, String image){
        final SongCard newCard = new SongCard(uniqueId, songTitle, songAuthor, defaultDrawable, image);
        newCard.setOnCardDimissedListener(new CardModel.OnCardDimissedListener() {
            //yes these are reversed, because the library thinks left is like. we want the opposite
            @Override
            public void onLike() {
                Log.d("disliked", newCard.getTitle());
                reportDislike(newCard.getUniqueId());
            }

            @Override
            public void onDislike() {
                Log.d("liked", newCard.getTitle());
                reportLike(newCard.getUniqueId());
            }
        });
        mCardAdapter.add(newCard);
    }

    private void reportDislike(String uniqueId) {
        VotedTrack votedTrack = new VotedTrack();
        votedTrack.id = uniqueId;
        votedTrack.weight = -1;
        mVotedTracks.add(votedTrack);
        sendListIfNeeded();
    }

    private void reportLike(String uniqueId){
        VotedTrack votedTrack = new VotedTrack();
        votedTrack.id = uniqueId;
        votedTrack.weight = 1;
        mVotedTracks.add(votedTrack);
        sendListIfNeeded();
    }

    private void sendListIfNeeded(){
        if(mNumSongs == mVotedTracks.size() || mVotedTracks.size() > 9){
            JSONObject jsonObject = new JSONObject();
            JSONObject jsonObjectTracks = new JSONObject();

            for(VotedTrack track : mVotedTracks){
                try {
                    jsonObjectTracks.put(track.id, track.weight);
                } catch (JSONException e) {
                }
            }

            try {
                jsonObject.put("userId", UserUtil.getUserId());
                jsonObject.put("tracks", jsonObjectTracks);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            skiprApi.postVotedTracks(mRoomId, jsonObject, new Callback<Response>() {
                @Override
                public void success(Response s, Response response) {
                    return;
                }

                @Override
                public void failure(RetrofitError error) {
                    return;
                }
            });
        }
    }

    private int mNumSongs = 0;
    private void requestSongs(){
        final String roomId = mRoomId;
        skiprApi.tracks(roomId, UserUtil.getUserId(), new Callback<List<Track>>() {
            @Override
            public void success(List<Track> tracks, Response response) {
                mNumSongs = tracks.size();
                for(Track track : tracks){
                    addSongToQueue(track.id, track.name, track.artist, track.image);
                }
                mCardContainer.setAdapter(mCardAdapter);
            }

            @Override
            public void failure(RetrofitError error) {
                return;
            }
        });
    }
}