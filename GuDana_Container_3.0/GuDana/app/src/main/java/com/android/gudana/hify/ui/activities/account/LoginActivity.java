package com.android.gudana.hify.ui.activities.account;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.transition.Fade;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.gudana.hify.ui.activities.MainActivity;
import com.android.gudana.hify.utils.AnimationUtil;
import com.android.gudana.hify.utils.database.UserHelper;
import com.android.gudana.R;
import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;

import java.util.HashMap;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LoginActivity extends AppCompatActivity {

    public static Activity activity;
    private EditText email,password;
    private Button login,register;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private UserHelper userHelper;
    private ProgressDialog mDialog;

    public static void startActivityy(Context context) {
        Intent intent=new Intent(context,LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hi_activity_login);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        activity = this;
        mAuth= FirebaseAuth.getInstance();
        mFirestore= FirebaseFirestore.getInstance();
        userHelper = new UserHelper(this);

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Please wait..");
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);

        email=findViewById(R.id.email);
        password=findViewById(R.id.password);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Fade fade = new Fade();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fade.excludeTarget(findViewById(R.id.layout), true);
                fade.excludeTarget(android.R.id.statusBarBackground, true);
                fade.excludeTarget(android.R.id.navigationBarBackground, true);
                getWindow().setEnterTransition(fade);
                getWindow().setExitTransition(fade);
            }
        }

    }


    public void performLogin() {

        final String email_, pass_;
        email_ = email.getText().toString();
        pass_ = password.getText().toString();
        if (!TextUtils.isEmpty(email_) && !TextUtils.isEmpty(pass_)) {
            mDialog.show();

            mAuth.signInWithEmailAndPassword(email_, pass_).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull final Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        if (task.getResult().getUser().isEmailVerified()) {

                            final String token_id = FirebaseInstanceId.getInstance().getToken();
                            Log.i("TOKEN",token_id);
                            final String current_id = task.getResult().getUser().getUid();

                            mFirestore.collection("Users").document(current_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if (documentSnapshot.getString("token_id").equals(token_id) || documentSnapshot.getString("token_id").equals("") ) {

                                        Map<String, Object> tokenMap = new HashMap<>();
                                        tokenMap.put("token_id", token_id);

                                        mFirestore.collection("Users").document(current_id).update(tokenMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                FirebaseFirestore.getInstance().collection("Users").document(current_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                                                        userHelper.insertContact(
                                                                documentSnapshot.getString("username")
                                                                ,documentSnapshot.getString("name")
                                                                , documentSnapshot.getString("email")
                                                                , documentSnapshot.getString("image")
                                                                , pass_
                                                                ,documentSnapshot.getString("location")
                                                                ,documentSnapshot.getString("bio")
                                                        );

                                                        mDialog.dismiss();
                                                        MainActivity.startActivity(LoginActivity.this);
                                                        finish();

                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.e("Error", ".." + e.getMessage());
                                                    }
                                                });

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                mDialog.dismiss();
                                                //Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                new LovelyInfoDialog(LoginActivity.this)
                                                        .setTopColorRes(R.color.colorPrimary)
                                                        .setIcon(R.mipmap.ic_infos)
                                                        //This will add Don't show again checkbox to the dialog. You can pass any ID as argument
                                                        //.setNotShowAgainOptionEnabled(0)
                                                        //.setNotShowAgainOptionChecked(true)
                                                        .setTitle("Infos")
                                                        .setMessage("Error: " + e.getMessage())
                                                        .show();
                                            }
                                        });
                                    } else {
                                        mDialog.dismiss();
                                        new BottomDialog.Builder(LoginActivity.this)
                                                .setTitle("Information")
                                                .setContent("This account is being used in another device, please logout from that device and try again.")
                                                .setPositiveText("Ok")
                                                .setPositiveBackgroundColorResource(R.color.colorAccentt)
                                                .setCancelable(true)
                                                .onPositive(new BottomDialog.ButtonCallback() {
                                                    @Override
                                                    public void onClick(@NonNull BottomDialog dialog) {
                                                        dialog.dismiss();
                                                    }
                                                })
                                                .show();

                                        if (mAuth.getCurrentUser() != null) {
                                            mAuth.signOut();
                                        }

                                    }
                                }
                            });


                        } else{

                            mDialog.dismiss();
                            new BottomDialog.Builder(LoginActivity.this)
                                    .setTitle("Information")
                                    .setContent("Email has not been verified, please verify and continue.")
                                    .setPositiveText("Send again")
                                    .setPositiveBackgroundColorResource(R.color.colorAccentt)
                                    .setCancelable(true)
                                    .onPositive(new BottomDialog.ButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull final BottomDialog dialog) {
                                          task.getResult()
                                                  .getUser()
                                                  .sendEmailVerification()
                                                  .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                      @Override
                                                      public void onSuccess(Void aVoid) {
                                                          dialog.dismiss();
                                                          //Toast.makeText(LoginActivity.this, "Verification email sent", Toast.LENGTH_SHORT).show();
                                                          new LovelyInfoDialog(LoginActivity.this)
                                                                  .setTopColorRes(R.color.colorPrimary)
                                                                  .setIcon(R.mipmap.ic_infos)
                                                                  //This will add Don't show again checkbox to the dialog. You can pass any ID as argument
                                                                  //.setNotShowAgainOptionEnabled(0)
                                                                  //.setNotShowAgainOptionChecked(true)
                                                                  .setTitle("Infos")
                                                                  .setMessage("Verification email sent")
                                                                  .show();
                                                      }
                                                  })
                                                  .addOnFailureListener(new OnFailureListener() {
                                                      @Override
                                                      public void onFailure(@NonNull Exception e) {
                                                          Log.e("Error",e.getMessage());
                                                      }
                                                  });
                                        }
                                    })
                                    .setNegativeText("Ok")
                                    .onNegative(new BottomDialog.ButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull BottomDialog dialog) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .show();

                            if (mAuth.getCurrentUser() != null) {
                                mAuth.signOut();
                            }

                        }

                    } else {
                        if (task.getException().getMessage().contains("The password is invalid")) {
                            Toast.makeText(LoginActivity.this, "Error: The password you have entered is invalid.", Toast.LENGTH_SHORT).show();
                            mDialog.dismiss();
                            new LovelyInfoDialog(LoginActivity.this)
                                    .setTopColorRes(R.color.colorPrimary)
                                    .setIcon(R.mipmap.ic_infos)
                                    //This will add Don't show again checkbox to the dialog. You can pass any ID as argument
                                    //.setNotShowAgainOptionEnabled(0)
                                    //.setNotShowAgainOptionChecked(true)
                                    .setTitle("Infos")
                                    .setMessage("Error: The password you have entered is invalid.")
                                    .show();

                        } else if (task.getException().getMessage().contains("There is no user record")) {
                            Toast.makeText(LoginActivity.this, "Error: Invalid user, Please register using the button below.", Toast.LENGTH_SHORT).show();
                            mDialog.dismiss();
                            new LovelyInfoDialog(LoginActivity.this)
                                    .setTopColorRes(R.color.colorPrimary)
                                    .setIcon(R.mipmap.ic_infos)
                                    //This will add Don't show again checkbox to the dialog. You can pass any ID as argument
                                    //.setNotShowAgainOptionEnabled(0)
                                    //.setNotShowAgainOptionChecked(true)
                                    .setTitle("Infos")
                                    .setMessage("Error: Invalid user, Please register using the button below.")
                                    .show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            mDialog.dismiss();
                            new LovelyInfoDialog(LoginActivity.this)
                                    .setTopColorRes(R.color.colorPrimary)
                                    .setIcon(R.mipmap.ic_infos)
                                    //This will add Don't show again checkbox to the dialog. You can pass any ID as argument
                                    //.setNotShowAgainOptionEnabled(0)
                                    //.setNotShowAgainOptionChecked(true)
                                    .setTitle("Infos")
                                    .setMessage("Error: " + task.getException().getMessage())
                                    .show();
                        }

                    }
                }
            });

        } else if (TextUtils.isEmpty(email_)) {

            AnimationUtil.shakeView(email, this);

        } else if (TextUtils.isEmpty(pass_)) {

            AnimationUtil.shakeView(password, this);

        } else {

            AnimationUtil.shakeView(email, this);
            AnimationUtil.shakeView(password, this);

        }

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransitionExit();
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransitionEnter();
    }

    /**
     * Overrides the pending Activity transition by performing the "Enter" animation.
     */
    protected void overridePendingTransitionEnter() {
        overridePendingTransition(R.anim.hi_slide_from_right, R.anim.hi_slide_to_left);
    }

    /**
     * Overrides the pending Activity transition by performing the "Exit" animation.
     */
    protected void overridePendingTransitionExit() {
        overridePendingTransition(R.anim.hi_slide_from_left, R.anim.hi_slide_to_right);
    }

    public void onLogin(View view) {
        performLogin();
    }

    public void onRegister(View view) {
        RegisterActivity.startActivity(this, this, findViewById(R.id.button));
    }

}
