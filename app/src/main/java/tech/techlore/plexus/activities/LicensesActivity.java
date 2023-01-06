/*
 * Copyright (c) 2022 Techlore
 *
 *  This file is part of Plexus.
 *
 *  Plexus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plexus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Plexus.  If not, see <https://www.gnu.org/licenses/>.
 */

package tech.techlore.plexus.activities;

import static tech.techlore.plexus.utils.IntentUtils.OpenURL;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import java.util.Objects;

import tech.techlore.plexus.databinding.ActivityLicensesBinding;

public class LicensesActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLicensesBinding activityBinding = ActivityLicensesBinding.inflate(getLayoutInflater());
        setContentView(activityBinding.getRoot());
    
        setSupportActionBar(activityBinding.toolbarBottom);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        
        activityBinding.toolbarBottom.setNavigationOnClickListener(v -> onBackPressed());
        
        // Plexus
        activityBinding.plexus.setOnClickListener(v ->
                  OpenURL(this, "https://github.com/techlore/Plexus-app/blob/main/LICENSE",
                          activityBinding.coordinatorLayout, activityBinding.appBarBottom));
    
        // Jackson
        activityBinding.jackson.setOnClickListener(v ->
                  OpenURL(this, "https://github.com/FasterXML/jackson-core/blob/2.15/LICENSE",
                          activityBinding.coordinatorLayout, activityBinding.appBarBottom));
        // OkHttp
        activityBinding.okhttp.setOnClickListener(v ->
                  OpenURL(this, "https://github.com/square/okhttp/blob/master/LICENSE.txt",
                          activityBinding.coordinatorLayout, activityBinding.appBarBottom));
        // Plexus
        activityBinding.fastscroll.setOnClickListener(v ->
                  OpenURL(this, "https://github.com/zhanghai/AndroidFastScroll/blob/master/LICENSE",
                          activityBinding.coordinatorLayout, activityBinding.appBarBottom));
    
        // Material Design Icons
        activityBinding.mdIcons.setOnClickListener(v ->
                  OpenURL(this, "https://github.com/Templarian/MaterialDesign/blob/master/LICENSE",
                          activityBinding.coordinatorLayout, activityBinding.appBarBottom));

    }
    
}