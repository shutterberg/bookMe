package com.aqua.bookmeapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
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

public class DonateActivity extends AppCompatActivity {


    private CircleImageView book_image;
    private TextInputEditText uploadBookName, uploadFullName, uploadEmail, uploadPhone, uploadAuthorName;
    private Button uploadButton;

    private Uri resUri;
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabaseRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);

        uploadAuthorName = findViewById(R.id.uploadAuthorName);
        uploadFullName = findViewById(R.id.uploadFullName);
        uploadBookName = findViewById(R.id.uploadBookName);
        uploadEmail = findViewById(R.id.uploadEmail);
        uploadPhone = findViewById(R.id.uploadPhone);
        book_image = findViewById(R.id.book_image);
        uploadButton = findViewById(R.id.uploadButton);

        book_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String bookName = uploadBookName.getText().toString().trim();
                final String authorName = uploadAuthorName.getText().toString().trim();
                final String fullName = uploadFullName.getText().toString().trim();
                final String phoneNumber = uploadPhone.getText().toString().trim();
                final String email = uploadEmail.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    uploadEmail.setError("Email is required!");
                    return;
                }
                if (TextUtils.isEmpty(phoneNumber)) {
                    uploadPhone.setError("Phone Number is required!");
                    return;
                }
                if (TextUtils.isEmpty(fullName)) {
                    uploadFullName.setError("Name is required!");
                    return;
                }
                if (TextUtils.isEmpty(bookName)) {
                    uploadBookName.setError("Book Name is required!");
                    return;
                }
                if (TextUtils.isEmpty(authorName)) {
                    uploadAuthorName.setError("Author name is required!");
                    return;
                }
                else{
                    String currentUserId=mAuth.getCurrentUser().getUid();
                    userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("books").child(currentUserId);
                    HashMap bookInfo = new HashMap();
                    bookInfo.put("id", currentUserId);
                    bookInfo.put("name", fullName);
                    bookInfo.put("bookname", bookName);
                    bookInfo.put("authorname", authorName);
                    bookInfo.put("email", email);
                    bookInfo.put("phonenumber", phoneNumber);

                    userDatabaseRef.updateChildren(bookInfo).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task task) {
                            if(task.isSuccessful()){
                                Toast.makeText(DonateActivity.this, "Data Set Successfully", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(DonateActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }
                            finish();
                        }
                    });

                    if(resUri !=null){
                        final StorageReference filePath= FirebaseStorage.getInstance().getReference().child("book images").child(currentUserId);
                        Bitmap bitmap= null;
                        try {
                            bitmap= MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(),resUri);
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
                                Toast.makeText(DonateActivity.this,"Image Upload Failed",Toast.LENGTH_SHORT).show();

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
                                            newImageMap.put("bookimageurl",imageUrl);

                                            userDatabaseRef.updateChildren(newImageMap).addOnCompleteListener(new OnCompleteListener() {
                                                @Override
                                                public void onComplete(@NonNull @NotNull Task task) {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(DonateActivity.this,"Image url added to db",Toast.LENGTH_SHORT).show();
                                                    }else{
                                                        Toast.makeText(DonateActivity.this,task.getException().toString(),Toast.LENGTH_SHORT).show();
                                                    }

                                                }
                                            });
                                            finish();
                                        }
                                    });
                                }

                            }
                        });
                        Intent intent=new Intent(DonateActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });
    }


@Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK && data!=null){
            resUri=data.getData();
            book_image.setImageURI(resUri);
        }
    }
}
