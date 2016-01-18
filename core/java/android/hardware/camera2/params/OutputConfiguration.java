/*
 * Copyright (C) 2015 The Android Open Source Project
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


package android.hardware.camera2.params;

import android.annotation.SystemApi;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.utils.HashCodeHelpers;
import android.hardware.camera2.utils.SurfaceUtils;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.os.Parcel;
import android.os.Parcelable;

import static com.android.internal.util.Preconditions.*;

/**
 * A class for describing camera output, which contains a {@link Surface} and its specific
 * configuration for creating capture session.
 *
 * @see CameraDevice#createCaptureSessionByOutputConfiguration
 *
 */
public final class OutputConfiguration implements Parcelable {

    /**
     * Rotation constant: 0 degree rotation (no rotation)
     *
     * @hide
     */
    @SystemApi
    public static final int ROTATION_0 = 0;

    /**
     * Rotation constant: 90 degree counterclockwise rotation.
     *
     * @hide
     */
    @SystemApi
    public static final int ROTATION_90 = 1;

    /**
     * Rotation constant: 180 degree counterclockwise rotation.
     *
     * @hide
     */
    @SystemApi
    public static final int ROTATION_180 = 2;

    /**
     * Rotation constant: 270 degree counterclockwise rotation.
     *
     * @hide
     */
    @SystemApi
    public static final int ROTATION_270 = 3;

    /**
     * Invalid surface set ID.
     *
     *<p>An {@link OutputConfiguration} with this value indicates that the included surface
     *doesn't belong to any surface set.</p>
     */
    public static final int SURFACE_SET_ID_INVALID = -1;

    /**
     * Create a new {@link OutputConfiguration} instance with a {@link Surface}.
     *
     * @param surface
     *          A Surface for camera to output to.
     *
     * <p>This constructor creates a default configuration.</p>
     *
     */
    public OutputConfiguration(Surface surface) {
        this(surface, ROTATION_0);
    }

    /**
     * Create a new {@link OutputConfiguration} instance.
     *
     * <p>This constructor takes an argument for desired camera rotation</p>
     *
     * @param surface
     *          A Surface for camera to output to.
     * @param rotation
     *          The desired rotation to be applied on camera output. Value must be one of
     *          ROTATION_[0, 90, 180, 270]. Note that when the rotation is 90 or 270 degree,
     *          application should make sure corresponding surface size has width and height
     *          transposed corresponding to the width and height without rotation. For example,
     *          if application needs camera to capture 1280x720 picture and rotate it by 90 degree,
     *          application should set rotation to {@code ROTATION_90} and make sure the
     *          corresponding Surface size is 720x1280. Note that {@link CameraDevice} might
     *          throw {@code IllegalArgumentException} if device cannot perform such rotation.
     * @hide
     */
    @SystemApi
    public OutputConfiguration(Surface surface, int rotation) {
        checkNotNull(surface, "Surface must not be null");
        checkArgumentInRange(rotation, ROTATION_0, ROTATION_270, "Rotation constant");
        mSurfaceSetId = SURFACE_SET_ID_INVALID;
        mSurface = surface;
        mRotation = rotation;
        mConfiguredSize = SurfaceUtils.getSurfaceSize(surface);
        mConfiguredFormat = SurfaceUtils.getSurfaceFormat(surface);
        mConfiguredDataspace = SurfaceUtils.getSurfaceDataspace(surface);
    }

    /**
     * Create a new {@link OutputConfiguration} instance with another {@link OutputConfiguration}
     * instance.
     *
     * @param other Another {@link OutputConfiguration} instance to be copied.
     *
     * @hide
     */
    @SystemApi
    public OutputConfiguration(OutputConfiguration other) {
        if (other == null) {
            throw new IllegalArgumentException("OutputConfiguration shouldn't be null");
        }

        this.mSurface = other.mSurface;
        this.mRotation = other.mRotation;
        this.mSurfaceSetId = other.mSurfaceSetId;
        this.mConfiguredDataspace = other.mConfiguredDataspace;
        this.mConfiguredFormat = other.mConfiguredFormat;
        this.mConfiguredSize = other.mConfiguredSize;
    }

    /**
     * Create an OutputConfiguration from Parcel.
     */
    private OutputConfiguration(Parcel source) {
        int rotation = source.readInt();
        int surfaceSetId = source.readInt();
        Surface surface = Surface.CREATOR.createFromParcel(source);
        checkNotNull(surface, "Surface must not be null");
        checkArgumentInRange(rotation, ROTATION_0, ROTATION_270, "Rotation constant");
        mSurfaceSetId = surfaceSetId;
        mSurface = surface;
        mRotation = rotation;
        mConfiguredSize = SurfaceUtils.getSurfaceSize(mSurface);
        mConfiguredFormat = SurfaceUtils.getSurfaceFormat(mSurface);
        mConfiguredDataspace = SurfaceUtils.getSurfaceDataspace(mSurface);
    }

    /**
     * Get the {@link Surface} associated with this {@link OutputConfiguration}.
     *
     * @return the {@link Surface} associated with this {@link OutputConfiguration}.
     */
    public Surface getSurface() {
        return mSurface;
    }

    /**
     * Get the rotation associated with this {@link OutputConfiguration}.
     *
     * @return the rotation associated with this {@link OutputConfiguration}.
     *         Value will be one of ROTATION_[0, 90, 180, 270]
     *
     * @hide
     */
    @SystemApi
    public int getRotation() {
        return mRotation;
    }

    /**
     * Set the surface set ID to this {@link OutputConfiguration}.
     *
     * <p>
     * A surface set ID is used to identify which surface set this output surface belongs to. A
     * surface set is a group of output surfaces that are not intended to receive camera output
     * buffer streams simultaneously. The {@link CameraDevice} may be able to share the buffers used
     * by all the surfaces from the same surface set, therefore may save the overall memory
     * footprint. The application should only set the same set ID for the streams that are not
     * simultaneously streaming. A negative ID indicates that this surface doesn't belong to any
     * surface set. The default value will be {@value #SURFACE_SET_ID_INVALID}.
     * </p>
     *
     * @param setId
     */
    public void setSurfaceSetId(int setId) {
        if (setId < 0) {
            setId = SURFACE_SET_ID_INVALID;
        }
        mSurfaceSetId = setId;
    }

    /**
     * Get the surface set Id associated with this {@link OutputConfiguration}.
     *
     * @return the surface set Id associated with this {@link OutputConfiguration}.
     *         Value will be one of ROTATION_[0, 90, 180, 270]
     */
    public int getSurfaceSetId() {
        return mSurfaceSetId;
    }

    public static final Parcelable.Creator<OutputConfiguration> CREATOR =
            new Parcelable.Creator<OutputConfiguration>() {
        @Override
        public OutputConfiguration createFromParcel(Parcel source) {
            try {
                OutputConfiguration outputConfiguration = new OutputConfiguration(source);
                return outputConfiguration;
            } catch (Exception e) {
                Log.e(TAG, "Exception creating OutputConfiguration from parcel", e);
                return null;
            }
        }

        @Override
        public OutputConfiguration[] newArray(int size) {
            return new OutputConfiguration[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (dest == null) {
            throw new IllegalArgumentException("dest must not be null");
        }
        dest.writeInt(mRotation);
        dest.writeInt(mSurfaceSetId);
        mSurface.writeToParcel(dest, flags);
    }

    /**
     * Check if this {@link OutputConfiguration} is equal to another {@link OutputConfiguration}.
     *
     * <p>Two output configurations are only equal if and only if the underlying surfaces, surface
     * properties (width, height, format, dataspace) when the output configurations are created,
     * and all other configuration parameters are equal. </p>
     *
     * @return {@code true} if the objects were equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (this == obj) {
            return true;
        } else if (obj instanceof OutputConfiguration) {
            final OutputConfiguration other = (OutputConfiguration) obj;
            return mSurface == other.mSurface &&
                   mRotation == other.mRotation &&
                   mConfiguredSize.equals(other.mConfiguredSize) &&
                   mConfiguredFormat == other.mConfiguredFormat &&
                   mConfiguredDataspace == other.mConfiguredDataspace &&
                   mSurfaceSetId == other.mSurfaceSetId;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return HashCodeHelpers.hashCode(mSurface.hashCode(), mRotation);
    }

    private static final String TAG = "OutputConfiguration";
    private final Surface mSurface;
    private final int mRotation;
    private int mSurfaceSetId;

    // The size, format, and dataspace of the surface when OutputConfiguration is created.
    private final Size mConfiguredSize;
    private final int mConfiguredFormat;
    private final int mConfiguredDataspace;
}
