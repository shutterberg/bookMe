package com.aqua.bookmeapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReceiverRegistrationActivity extends AppCompatActivity {
    private CircleImageView profile_image;
    private TextInputEditText registerFullName,registerEmail,registerPhone,registerPassword;
    private Button registerButton;
    private TextView backButton;
    private Uri resultUri;
    private ProgressDialog loader;

    private FirebaseAuth mAuth;
    private DatabaseReference userDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver_registration);

        backButton=findViewById(R.id.backButton);
        registerButton=findViewById(R.id.registerButton);
        registerFullName=findViewById(R.id.registerFullName);
        registerEmail=findViewById(R.id.registerEmail);
        registerPhone=findViewById(R.id.registerPhone);
        registerPassword=findViewById(R.id.registerPassword);
        profile_image=findViewById(R.id.profile_image);
        loader = new ProgressDialog( this);

        mAuth=FirebaseAuth.getInstance();
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReceiverRegistrationActivity.this,LoginActivity.class);
                startActivity(intent);

            }

        });
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email =registerEmail.getText().toString().trim();
                final String password =registerPassword.getText().toString().trim();
                final String fullName =registerFullName.getText().toString().trim();
                final String phoneNumber =registerPhone.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    registerEmail.setError("Email is required!");
                    return;
                }
                if(TextUtils.isEmpty(phoneNumber)){
                    registerPhone.setError("Phone Number is required!");
                    return;
                }
                if(TextUtils.isEmpty(fullName)){
                    registerFullName.setError("Name is required!");
                    return;
                }
                if(TextUtils.isEmpty(email)){
                    registerEmail.setError("Email is required!");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    registerPassword.setError("Password is required!");
                    return;
                }
                else{
                    loader.setMessage("Registering you..");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();

                    mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                            if(!task.isSuccessful()){
                                String error =task.getException().toString();
                                Toast.makeText(ReceiverRegistrationActivity.this,"Error!" + error,Toast.LENGTH_SHORT).show();
                            }
                            else{
                                String currentUserID =mAuth.getCurrentUser().getUid();
                                userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserID);
                                HashMap userInfo = new HashMap();
                                userInfo.put("id", currentUserID);
                                userInfo.put("name", fullName);
                                userInfo.put("email", email);
                                userInfo.put("phonenumber", phoneNumber);
                                userInfo.put("type","receiver");

                                userDatabaseRef.updateChildren(userInfo).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(ReceiverRegistrationActivity.this, "Data Set Successfully", Toast.LENGTH_SHORT).show();
                                        }else{
                                            Toast.makeText(ReceiverRegistrationActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                        }
                                        finish();
                                        //loader.dismiss();
                                    }
                                });
                                if(resultUri !=null){
                                    final StorageReference filePath= FirebaseStorage.getInstance().getReference().child("profile images").child(currentUserID);
                                    Bitmap bitmap= null;
                                    try {
                                        bitmap= MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(),resultUri);
                                    }catch (IOException e){
                                        e.printStackTrace();
                                    }
                                    ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 20,byteArrayOutputStream);
                                    byte[] data = byteArrayOutputStream.toByteArray();
                                    UploadTask uploadTask = filePath.putBytes(data);

                                    uploadTask.addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull @NotNull Exception e) {
                                            Toast.makeText(ReceiverRegistrationActivity.this,"Image Upload Failed",Toast.LENGTH_SHORT).show();

                                        }
                                    });
                                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                            if(taskSnapshot.getMetadata() !=null && taskSnapshot.getMetadata().getReference() !=null){
                                                Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                                result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        String imageUrl=uri.toString();
                                                        Map newImageMap=new HashMap();
                                                        newImageMap.put("profilepictureurl",imageUrl);

                                                        userDatabaseRef.updateChildren(newImageMap).addOnCompleteListener(new OnCompleteListener() {
                                                            @Override
                                                            public void onComplete(@NonNull @NotNull Task task) {
                                                                if(task.isSuccessful()){
                                                                    Toast.makeText(ReceiverRegistrationActivity.this,"Image url added to db",Toast.LENGTH_SHORT).show();
                                                                }else{
                                                                    Toast.makeText(ReceiverRegistrationActivity.this,task.getException().toString(),Toast.LENGTH_SHORT).show();
                                                                }

                                                            }
                                                        });
                                                        finish();
                                                    }
                                                });
                                            }

                                        }
                                    });
                                    Intent intent=new Intent(ReceiverRegistrationActivity.this,MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                    loader.dismiss();
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK && data !=null){
            resultUri=data.getData();
            profile_image.setImageURI(resultUri);
        }

    }

}
