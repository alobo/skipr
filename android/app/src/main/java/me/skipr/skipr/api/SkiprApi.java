package me.skipr.skipr.api;

import org.json.JSONObject;

import java.util.List;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by Navjot on 6/13/2015.
 */
public interface SkiprApi {
    /*Join room*/
    @Multipart
    @PUT("/join")
    public void join(@Part("userId") String userId, @Part("roomName") String roomName, Callback<RoomJoinResponse> callback);

    /*Create room*/
    @Multipart
    @POST("/room")
    public void create(@Part("userId") String userId, @Part("token") String token, Callback<RoomCreateResponse> callback);


     /*Get all tracks to vote on*/
    @GET("/room/{roomId}/tracks")
    public void tracks(@Path("roomId") String roomId, @Query("userId") String userId, Callback<List<Track>> callback);

    /*Send voted tracks*/
    @PUT("/room/{roomId}/vote")
    public void postVotedTracks(@Path("roomId") String roomId, @Body JSONObject body, Callback<Response> callback);

    /*get which task should be played*/
    @GET("/room/{roomId}/next")
    public void getFreshestTrack(@Path("roomId") String roomId, Callback<TopTrack> callback);

    /*get which task should be played*/
    @DELETE("/room/{roomId}/tracks/{trackId}")
    public void deleteTrack(@Path("roomId") String roomId, @Path("trackId") String trackId, Callback<Response> callback);
}
