/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.ambientcontext;

import android.Manifest;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.UserIdInt;
import android.app.ambientcontext.AmbientContextEvent;
import android.content.ComponentName;
import android.util.Slog;

import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;

/**
 * Per-user manager service for {@link AmbientContextEvent}s.
 */
public class DefaultAmbientContextManagerPerUserService extends
        AmbientContextManagerPerUserService {
    private static final String TAG =
            DefaultAmbientContextManagerPerUserService.class.getSimpleName();

    @Nullable
    @VisibleForTesting
    DefaultRemoteAmbientContextDetectionService mRemoteService;

    private ComponentName mComponentName;
    private final ServiceType mServiceType;
    private final String mServiceName;

    DefaultAmbientContextManagerPerUserService(
            @NonNull AmbientContextManagerService master, Object lock,
            @UserIdInt int userId, ServiceType serviceType, String serviceName) {
        super(master, lock, userId);
        this.mServiceType = serviceType;
        this.mServiceName = serviceName;
        this.mComponentName = ComponentName.unflattenFromString(mServiceName);
        Slog.d(TAG, "Created DefaultAmbientContextManagerPerUserService"
                + "and service type: " + mServiceType.name() + " and service name: " + serviceName);
    }


    @GuardedBy("mLock")
    @Override
    protected void ensureRemoteServiceInitiated() {
        if (mRemoteService == null) {
            mRemoteService = new DefaultRemoteAmbientContextDetectionService(
                    getContext(), mComponentName, getUserId());
        }
    }

    @VisibleForTesting
    @Override
    ComponentName getComponentName() {
        return mComponentName;
    }

    @Override
    protected void setComponentName(ComponentName componentName) {
        this.mComponentName = componentName;
    }

    @Override
    protected RemoteAmbientDetectionService getRemoteService() {
        return mRemoteService;
    }

    @Override
    protected String getProtectedBindPermission() {
        return Manifest.permission.BIND_AMBIENT_CONTEXT_DETECTION_SERVICE;
    }

    @Override
    public ServiceType getServiceType() {
        return mServiceType;
    }

    @Override
    protected int getAmbientContextPackageNameExtraKeyConfig() {
        return com.android.internal.R.string.config_ambientContextPackageNameExtraKey;
    }

    @Override
    protected int getAmbientContextEventArrayExtraKeyConfig() {
        return com.android.internal.R.string.config_ambientContextEventArrayExtraKey;
    }

    @Override
    protected int getConsentComponentConfig() {
        return com.android.internal.R.string.config_defaultAmbientContextConsentComponent;
    }

    @Override
    protected void clearRemoteService() {
        mRemoteService = null;
    }
}
