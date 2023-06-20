package com.cringe_studios.cringe_authenticator;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.cringe_studios.cringe_authenticator.databinding.ActivityMainBinding;
import com.cringe_studios.cringe_authenticator.fragment.DynamicFragment;
import com.cringe_studios.cringe_authenticator.fragment.HomeFragment;
import com.cringe_studios.cringe_authenticator.fragment.MenuFragment;
import com.cringe_studios.cringe_authenticator.fragment.SettingsFragment;
import com.cringe_studios.cringe_authenticator.util.NavigationUtil;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt prompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                finishAffinity();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                launchApp();
            }
        });

        /*BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential")
                .setAllowedAuthenticators(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)
                .build();

        prompt.authenticate(info);*/

        launchApp();
    }

    private void launchApp() {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(view -> NavigationUtil.navigate(this, MenuFragment.class, null));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        if(NavigationUtil.getCurrentFragment(this) instanceof DynamicFragment) {
            //getMenuInflater().inflate(R.menu.menu_dynamic, menu);
            getMenuInflater().inflate(R.menu.menu_dynamic, menu);
            return true;
        }

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Log.i("AMOGUS", "navigateUp");
        if(NavigationUtil.getCurrentFragment(this) instanceof DynamicFragment) {
            NavigationUtil.navigate(this, HomeFragment.class, null);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void openSettings(MenuItem item) {
        NavigationUtil.navigate(this, SettingsFragment.class, null);
    }

    public void addCode(MenuItem item) {
        // TODO: add code
    }

}