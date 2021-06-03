package com.sarks.instagum.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sarks.instagum.Adapter.PhotoAdapter;
import com.sarks.instagum.Model.Post;
import com.sarks.instagum.Model.User;
import com.sarks.instagum.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {


    private RecyclerView recyclerView;
    private PhotoAdapter photoAdapter;
    private List<Post> myPhotoList;

    private RecyclerView recyclerViewSaves;
    private PhotoAdapter photoAdapterSaves;
    private List<Post> mySavedPosts;


    private CircleImageView imageProfile;
    private ImageView options, myPictures, savedPictures;
    private TextView followers, following, posts, fullname, username, bio;

    private FirebaseUser fUser;

    String profileId;

    private Button editProfile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        fUser = FirebaseAuth.getInstance().getCurrentUser();

        String data = getContext().getSharedPreferences("PROFILE", Context.MODE_PRIVATE).getString("profileId", "none");

        if (data.equals("none")){
            profileId = fUser.getUid();

        }else{
            profileId = data;
        }


        imageProfile = view.findViewById(R.id.image_profile);
        options = view.findViewById(R.id.options);
        myPictures = view.findViewById(R.id.my_pictures);
        savedPictures = view.findViewById(R.id.saved_pictures);
        followers = view.findViewById(R.id.followers);
        following = view.findViewById(R.id.following);
        posts = view.findViewById(R.id.posts);
        fullname = view.findViewById(R.id.fullname);
        username = view.findViewById(R.id.username);
        bio = view.findViewById(R.id.bio);
        editProfile = view.findViewById(R.id.edit_profile);

        recyclerView = view.findViewById(R.id.recycler_view_pictures);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        myPhotoList = new ArrayList<>();
        photoAdapter = new PhotoAdapter(getContext(), myPhotoList);
        recyclerView.setAdapter(photoAdapter);


        recyclerViewSaves = view.findViewById(R.id.recycler_view_saved);
        recyclerViewSaves.setHasFixedSize(true);
        recyclerViewSaves.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mySavedPosts = new ArrayList<>();
        photoAdapterSaves = new PhotoAdapter(getContext(), mySavedPosts);
        recyclerViewSaves.setAdapter(photoAdapterSaves);


        userInfo();
        getFollowersAndFollowingCount();
        getPostCout();
        myPhotos();
        getSavedPost();

        if(profileId.equals(fUser.getUid())){
            editProfile.setText("Edit Profile");
        }else{
            checkFollowingStatus();
        }

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btnText = editProfile.getText().toString();
                if (btnText.equals("Edit Profile")){
                    // TODO GO TO edit k
                }else{
                    if(btnText.equals("follow")){
                        FirebaseDatabase.getInstance().getReference().child("Follow").child(fUser.getUid()).child("following").child(profileId).setValue(true);
                        FirebaseDatabase.getInstance().getReference().child("Follow").child(profileId).child("followers").child(fUser.getUid()).setValue(true);
                    }else{
                        FirebaseDatabase.getInstance().getReference().child("Follow").child(fUser.getUid()).child("following").child(profileId).removeValue();
                        FirebaseDatabase.getInstance().getReference().child("Follow").child(profileId).child("followers").child(fUser.getUid()).removeValue();
                    }
                }
            }
        });

        recyclerView.setVisibility(View.VISIBLE);
        recyclerViewSaves.setVisibility(View.GONE);

        myPictures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility(View.VISIBLE);
                recyclerViewSaves.setVisibility(View.GONE);
            }
        });

        savedPictures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility(View.GONE);
                recyclerViewSaves.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }

    private void getSavedPost() {

        final List<String> savedIds = new ArrayList<>();

        FirebaseDatabase.getInstance().getReference().child("Saves").child(fUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1: snapshot.getChildren()){
                    savedIds.add(snapshot1.getKey());
                }

                FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot2) {
                        mySavedPosts.clear();
                        for(DataSnapshot snapshot3 : snapshot2.getChildren()){
                            Post post= snapshot3.getValue(Post.class);

                            for (String id: savedIds){
                                if (post.getPostid().equals(id)){
                                    mySavedPosts.add(post);
                                }
                            }
                        }
                        photoAdapterSaves.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void myPhotos() {

        FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myPhotoList.clear();
                for (DataSnapshot snapshot1: snapshot.getChildren()){
                    Post post = snapshot1.getValue(Post.class);
                    if (post.getPublisher().equals(profileId)){
                        myPhotoList.add(post);
                    }
                }

                Collections.reverse(myPhotoList);
                photoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void checkFollowingStatus() {

        FirebaseDatabase.getInstance().getReference().child("Follow").child(fUser.getUid()).child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(profileId).exists()){
                    editProfile.setText("following");
                }
                else{
                    editProfile.setText("follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getPostCout() {
        FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int counter =0;
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    Post post = snapshot1.getValue(Post.class);

                    if (post.getPublisher().equals(profileId)) counter++;
                }
                posts.setText(String.valueOf(counter));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFollowersAndFollowingCount() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Follow").child(profileId);

        ref.child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followers.setText(""+ snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ref.child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                following.setText("" + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void userInfo() {

        FirebaseDatabase.getInstance().getReference().child("Users").child(profileId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                Picasso.get().load(user.getImageurl()).into(imageProfile);
                username.setText(user.getUsername());
                fullname.setText(user.getName());
                bio.setText(user.getBio());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}