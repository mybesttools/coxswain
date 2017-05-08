/*
 * Copyright 2015 Sven Meier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package svenmeier.coxswain.view;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import svenmeier.coxswain.R;
import svenmeier.coxswain.util.PermissionBlock;
import svenmeier.coxswain.view.preference.ResultPreference;

public class SettingsFragment extends PreferenceFragmentCompat {

    private Map<ResultPreference, Integer> requestCodes = new HashMap<>();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        Preference bindings = findPreference(getString(R.string.preference_workout_bindings_reset));
        bindings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                propoid.util.content.Preference.getEnum(getActivity(), ValueBinding.class, R.string.preference_workout_binding).setList(new ArrayList<ValueBinding>());
                propoid.util.content.Preference.getEnum(getActivity(), ValueBinding.class, R.string.preference_workout_binding_pace).setList(new ArrayList<ValueBinding>());

                return true;
            }
        });

        Preference trace = findPreference(getString(R.string.preference_hardware_trace));
        trace.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                if (Boolean.TRUE.equals(o)) {
                    new PermissionBlock(getActivity()).acquirePermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }

                return true;
            }
        });

        Preference devices = findPreference(getString(R.string.preference_devices));
        devices.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.settings_fragment, new DevicesFragment());
                transaction.addToBackStack(null);
                transaction.commit();
                return true;
            }
        });
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof ResultPreference) {
            ResultPreference resultPreference = (ResultPreference) preference;

            Intent intent = resultPreference.getRequest();

            int requestCode = requestCode(resultPreference);
            startActivityForResult(intent, requestCode);

            return true;
        }

        return super.onPreferenceTreeClick(preference);
    }

    private int requestCode(ResultPreference preference) {
        Integer code = requestCodes.get(preference);
        if (code == null) {
            code = requestCodes.size();
            requestCodes.put(preference, code);
        }

        return code;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != 0) {
            for (Map.Entry<ResultPreference, Integer> entry : requestCodes.entrySet()) {
                if (entry.getValue() == requestCode) {
                    entry.getKey().onResult(intent);
                    return;
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }
}