package com.venta_productos.delivery;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class c_ingreso_a_la_app extends Fragment {


    public c_ingreso_a_la_app() {
        // Required empty public constructor
    }

    Button btn_ingresar_con_el_numero_de_telefono, btn_codigo_telefono_verificar_el_codigo_paso2, btn_ver_mapas, btn_ver_negocios;
    EditText edt_verificar_codigo_ingresar_numero_telefono, edt_verificar_codigo_ingresar_codigo_de_verificacion;
    LinearLayout linear_ingresar_codigo_telefono, linear_ingresar_codigo_telefono_paso2;
    String str_numero_telefono, str_codigo_telefono;
    String mVerificationId;
    PhoneAuthProvider.ForceResendingToken mResendToken;
    String userid;

    TextView TW_pedir_sin_registrarse;

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;

    SignInButton mGoogleBtn;
    int RC_SIGN_IN = 1;
    GoogleApiClient mGoogleApiClient;

    LoginButton ingresar_con_facebook;
    Button BTN_ingresar_con_facebook;

    FloatingActionButton fab;
    private FirebaseAuth mAuth;
    CallbackManager mCallbackManager;

    Context contex;
    SharedPreferences sharpref;

    String TAG = "asdf";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View Ingresar_a_la_app = inflater.inflate(R.layout.f_ingresar_a_la_app, container, false);

        btn_ingresar_con_el_numero_de_telefono = Ingresar_a_la_app.findViewById(R.id.login_button_numero_de_telefono);
        btn_codigo_telefono_verificar_el_codigo_paso2 = Ingresar_a_la_app.findViewById(R.id.bnt_verificacion_paso_2);
        edt_verificar_codigo_ingresar_numero_telefono = Ingresar_a_la_app.findViewById(R.id.edittext_ingresar_numero_telefono);
        edt_verificar_codigo_ingresar_codigo_de_verificacion = Ingresar_a_la_app.findViewById(R.id.edittext_ingresar_codigo_de_verificacion);
        linear_ingresar_codigo_telefono = Ingresar_a_la_app.findViewById(R.id.linear_ingresar_con_el_numero_de_telefono);
        linear_ingresar_codigo_telefono_paso2 = Ingresar_a_la_app.findViewById(R.id.linear_ingresar_codigo_paso_2);

        TW_pedir_sin_registrarse = Ingresar_a_la_app.findViewById(R.id.textView3);

        ingresar_con_facebook = Ingresar_a_la_app.findViewById(R.id.login_button_facebook);
        BTN_ingresar_con_facebook = Ingresar_a_la_app.findViewById(R.id.BTN_login_button_facebook);
        mGoogleBtn = Ingresar_a_la_app.findViewById(R.id.login_button_google);

        btn_ver_mapas = Ingresar_a_la_app.findViewById(R.id.btn_ingresar_a_la_app_ver_mapas);
        btn_ver_negocios = Ingresar_a_la_app.findViewById(R.id.btn_ingresar_a_la_app_ver_negocios);

        mAuth = FirebaseAuth.getInstance();

        if (getActivity() != null) {
            ((MainActivity) getActivity()).mostrar_fab(false);
        }

        contex = getActivity();
        sharpref = getContext().getSharedPreferences("usar_app", Context.MODE_PRIVATE);

        String valor_guerdado = sharpref.getString("usuario", "no hay dato");
        String[] dato = valor_guerdado.split(",");
        if (!valor_guerdado.equals("no hay dato")) {
            userid = dato[1];
        }
        if (mAuth != null) {
            userid = mAuth.getUid();
        }

        // Configure Phone Number Sign In
        btn_ingresar_con_el_numero_de_telefono.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edt_verificar_codigo_ingresar_numero_telefono.getText().toString().length() > 2) {
                    boolean los_caracteres_estan_bien = metodos.method_chekear_texto_de_los_edittext(getActivity(),edt_verificar_codigo_ingresar_numero_telefono.getText().toString());
                    if (los_caracteres_estan_bien) {
                        linear_ingresar_codigo_telefono.setVisibility(View.VISIBLE);
                        str_numero_telefono = edt_verificar_codigo_ingresar_numero_telefono.getText().toString();
                        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                str_numero_telefono,
                                60,
                                TimeUnit.SECONDS,
                                getActivity(),
                                mCallBacks
                        );
                    }
                } else {
                    Toast.makeText(getActivity(), "llene el campo telefono", Toast.LENGTH_SHORT).show();
                }

            }
        });

        mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                metodos.alertdialog_error_accion_solo_cerrar_el_alert(getActivity(),"Error en el formato del Telefono",
                        "El telefono tiene que tener esta caracteristica= \n (+)(codigo pais)(codigo de provincia)(numero) \n EJ=+54294*******",
                        "ACEPTAR",false);
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                // Save the verification id somewhere
                mVerificationId = verificationId;
                mResendToken = forceResendingToken;
                linear_ingresar_codigo_telefono_paso2.setVisibility(View.VISIBLE);
                // The corresponding whitelisted code above should be used to complete sign-in.
                //getActivity().enableUserManuallyInputCode();
            }
        };

        btn_codigo_telefono_verificar_el_codigo_paso2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                str_codigo_telefono = edt_verificar_codigo_ingresar_codigo_de_verificacion.getText().toString();
                if (mVerificationId.length()>3) {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, str_codigo_telefono);
                    signInWithPhoneAuthCredential(credential);
                } else {
                    Toast.makeText(getActivity(), "todavia no se completo el paso 1", Toast.LENGTH_SHORT).show();
                }

            }
        });


        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage(getActivity(), new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(getActivity(), "Hubo un error :(", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edt_verificar_codigo_ingresar_numero_telefono.getText().length() < 8 || edt_verificar_codigo_ingresar_numero_telefono.getText().length() > 18) {
                    Toast.makeText(getActivity(), "Error formato de el numero de telefono", Toast.LENGTH_SHORT).show();
                } else {
                    boolean los_caracteres_estan_bien = metodos.method_chekear_texto_de_los_edittext(getActivity(),edt_verificar_codigo_ingresar_numero_telefono.getText().toString());
                    if (los_caracteres_estan_bien) {
                        metodos.alerdialog_descargando_informacion(getActivity(),true,"ingresando");

                        signIn();
                    }
                }
            }
        });


        // Configure Facebook Sign In


        BTN_ingresar_con_facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edt_verificar_codigo_ingresar_numero_telefono.getText().length() < 8 || edt_verificar_codigo_ingresar_numero_telefono.getText().length() > 18) {
                    Toast.makeText(getActivity(), "Error formato de el numero de telefono", Toast.LENGTH_SHORT).show();
                } else {
                    boolean los_caracteres_estan_bien = metodos.method_chekear_texto_de_los_edittext(getActivity(),edt_verificar_codigo_ingresar_numero_telefono.getText().toString());
                    if (los_caracteres_estan_bien) {
                        ingresar_con_facebook.callOnClick();
                    }
                }
            }
        });
        mCallbackManager = CallbackManager.Factory.create();
        ingresar_con_facebook.setReadPermissions("email", "public_profile");
        ingresar_con_facebook.setFragment(this);
        ingresar_con_facebook.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());

            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });

        btn_ver_mapas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ((MainActivity) getActivity()).boolean_solo_mostrar_las_areas_del_mapa = false;
                final FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.content_main, new c_mapas_ver_covertura()).addToBackStack(toString());
                ft.commit();

            }
        });

        btn_ver_negocios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.content_main, new c_ver_negocios_sin_coneccion()).addToBackStack(toString());
                ft.commit();
            }
        });


        if (getActivity() != null) {
            ((MainActivity) getActivity()).chekear_internet();
        }
        return Ingresar_a_la_app;
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
        userid = mAuth.getUid();
        metodos.ingresando = true;
        llevar_a_otro_fragment(1);
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            llevar_a_otro_fragment(4);
            userid = mAuth.getUid();
            if (getActivity() != null) {
                metodos.alerdialog_descargando_informacion(getActivity(),true,"Buscando informacion del usuario");
            }
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            llevar_a_otro_fragment(3);
                            userid = mAuth.getUid();
                            FirebaseUser user = task.getResult().getUser();
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (getActivity() != null) {
                                metodos.alertdialog_error_accion_solo_cerrar_el_alert(getActivity(),"Error en el formato del Telefono",
                                        "El telefono tiene que tener esta caracteristica= \n (+)(codigo pais)(codigo de provincia)(numero) \n EJ=+54294*******",
                                        "ACEPTAR",false);
                            }
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid

                            }
                        }
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            llevar_a_otro_fragment(2);
                            userid = mAuth.getUid();
                            Toast.makeText(getActivity(), "ingresado correctamente", Toast.LENGTH_SHORT).show();
                            BTN_ingresar_con_facebook.setEnabled(true);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getActivity(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            BTN_ingresar_con_facebook.setEnabled(true);
                        }

                        // ...
                    }
                });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
//            Task<GoogleSignInAccount> task = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            //si aca hay un error puedo cambiar la linea de abajo
            GoogleSignInResult task = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            // Google Sign In was successful, authenticate with Firebase
            if (task.isSuccess()) {
                GoogleSignInAccount account = task.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                String status = String.valueOf(task.getStatus());
                Toast.makeText(getActivity(), "Problema con la cuenta = " + status, Toast.LENGTH_SHORT).show();
                Log.d("asdf", status);
            }

        }

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            llevar_a_otro_fragment(4);
                            userid = mAuth.getUid();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
//                            Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            Toast.makeText(getActivity(), "Problema en la carga del usuario", Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });


    }

    private void llevar_a_otro_fragment(int con_que_ingreso) {
        //las 3 variables van a ser = 1-google , 2-facebook, 3-telefono, 4-si no necesito pasar ningun valor
        if (getActivity() != null) {
            metodos.alerdialog_descargando_informacion(getActivity(),false,"");
        }
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String valor_guerdado = sharpref.getString("usuario", "no hay dato");
            SharedPreferences.Editor editor = sharpref.edit();
            userid = currentUser.getUid();
            String telefono = edt_verificar_codigo_ingresar_numero_telefono.getText().toString();

            if (valor_guerdado.equals("no hay dato")) {
                editor.putString("usuario", telefono + "," + userid);
                guardar_usuario(userid, telefono);
            }
            if (con_que_ingreso == 2) {
                editor.putString("forma_ingreso", "facebook");
            }
            editor.apply();
            if (userid != null) {

                checkear_si_el_usuario_guardo_domicilio(new FirebaseCallBack() {
                    @Override
                    public void onCallBack(String estado) {
                        if (estado.equals("con_ubicacion")) {

                            if (getActivity() != null) {
                                metodos.alerdialog_descargando_informacion(getActivity(),false,"");
                                final FragmentTransaction ft = getFragmentManager().beginTransaction();
                                ft.replace(R.id.content_main, new c_principal()).addToBackStack(toString());
                                ft.commit();
                            }

                        } else {
                            metodos.alerdialog_descargando_informacion(getActivity(),false,"");
                            ((MainActivity) getActivity()).boolean_ingreso_a_la_app_sin_ubicacion=false;
                            final FragmentTransaction ft = getFragmentManager().beginTransaction();
                            ft.replace(R.id.content_main, new c_ingresar_direccion()).addToBackStack(toString());
                            ft.commit();

//                            alertdialog_crear_ubicacion();
                        }
                    }
                });
            }
        } else {
            final FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.content_main, new c_principal()).addToBackStack(toString());
            ft.commit();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.stopAutoManage(getActivity());
        mGoogleApiClient.disconnect();
    }

    private void guardar_usuario(String usuario_id, String telefono) {

        DatabaseReference nDataBase = FirebaseDatabase.getInstance().getReference().child("usuario").child("clientes");
        nDataBase.child(usuario_id).child("telefono").setValue(telefono);
        nDataBase.child(usuario_id).child("cantidad_modificaciones").setValue("4");

    }

    private interface FirebaseCallBack {
        void onCallBack(String estado);
    }

    private void checkear_si_el_usuario_guardo_domicilio(final FirebaseCallBack firebaseCallBack) {
        DatabaseReference chekear_domicilio = FirebaseDatabase.getInstance().getReference().child("usuario").child("clientes").child(userid).child("ubicacion");
        chekear_domicilio.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    firebaseCallBack.onCallBack("con_ubicacion");
                } else {
                    firebaseCallBack.onCallBack("sin_ubicacion");
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
