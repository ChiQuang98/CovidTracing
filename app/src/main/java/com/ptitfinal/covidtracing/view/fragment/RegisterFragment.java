package com.ptitfinal.covidtracing.view.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.firebase.messaging.FirebaseMessaging;
import com.ptitfinal.covidtracing.MainActivity;
import com.ptitfinal.covidtracing.R;
import com.ptitfinal.covidtracing.model.SpecificID;
import com.ptitfinal.covidtracing.model.User;
import com.ptitfinal.covidtracing.repository.firebase.FirebaseDAO;

public class RegisterFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = RegisterFragment.class.getSimpleName();
    EditText edtPhoneNumber;
    Button btnRegister;
    private NavController navController;
    public RegisterFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register, container, false);
//        navController = Navigation.findNavController(view);
        if(checkCreatedKey()){
//            navController.navigate(R.id.action_registerFragment_to_bottom_nav_home_page);

        }
        BottomNavigationView navView = getActivity().findViewById(R.id.bottom_navigation);
        navView.setVisibility(View.GONE);
        navView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_SELECTED);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        if(checkCreatedKey()==true){
            navController.navigate(R.id.action_registerFragment_to_bottom_nav_home_page);

        }
        initWidget(view);

    }

    private boolean checkCreatedKey() {
        SharedPreferences prefs = getContext().getSharedPreferences("MyPreferences",getContext().MODE_PRIVATE);
        String ID = prefs.getString("keyCovid", null);
        if(ID==null){
            return false;
//            Toast.makeText(getContext(),"Creating key",Toast.LENGTH_SHORT).show();
        }
        else{
            Log.e(TAG,ID);
            return true;
        }
    }

    private void initWidget(View view) {
        btnRegister = view.findViewById(R.id.btnRegister);
        edtPhoneNumber = view.findViewById(R.id.edtNumber);
        btnRegister.setOnClickListener(this);
    }
    private String createKey() {
        StringBuilder ID = new StringBuilder(SpecificID.genID(getContext()));
        ID.setLength(9);
        SharedPreferences sharedPref = getContext().getSharedPreferences("MyPreferences", getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("keyCovid", ID.toString());
        editor.putString("isExposed","Chưa nhiễm");
        editor.apply();
        return ID.toString();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnRegister:
                if(edtPhoneNumber.getText().toString().equalsIgnoreCase("")){
                    Toast.makeText(getContext(),"Khong duoc de trong",Toast.LENGTH_SHORT).show();
                }
                else{
                    String keyCreated = createKey();
                    String phone = edtPhoneNumber.getText().toString();
                    pushKeyToDBOnline(keyCreated,phone);
                    String nameTopic = "covidtracing";
                    registerTopicFCM(nameTopic);
                    navController.navigate(R.id.action_registerFragment_to_bottom_nav_home_page);
                }
                break;
            default:
                break;
        }
    }

    private void registerTopicFCM(String nameTopic) {
        FirebaseMessaging.getInstance().subscribeToTopic(nameTopic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = getString(R.string.msg_subscribed);
                        if (!task.isSuccessful()) {
                            msg = getString(R.string.msg_subscribe_failed);
                        }
                        Log.d(TAG, msg);
                        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void pushKeyToDBOnline(String keyCreated, String phone) {
        String isExposed = "Chưa nhiễm";
        User user = new User(keyCreated,phone,isExposed);
        FirebaseDAO.getInstance().addKeyToDB(user);
    }
}