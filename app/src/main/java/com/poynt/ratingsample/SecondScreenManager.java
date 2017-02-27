package com.poynt.ratingsample;

/**
 * Created by dennis on 1/30/17.
 */

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import co.poynt.os.services.v1.IPoyntRatingAndReviewServiceListener;
import co.poynt.os.services.v1.IPoyntSecondScreenRatingEntryListener;
import co.poynt.os.services.v1.IPoyntSecondScreenService;

/**
 * Created by palavilli on 1/25/17.
 */

public class SecondScreenManager {
    public static final String TAG = SecondScreenManager.class.getSimpleName();

    private IPoyntSecondScreenService secondScreenService;
    private Context context;
    private static SecondScreenManager secondScreenManager;


    private Handler handler;

    private SecondScreenManager(Context context) {
        this.context = context;
        bind();
    }

    public static SecondScreenManager getInstance(Context context) {
        if (secondScreenManager == null) {
            secondScreenManager = new SecondScreenManager(context);
        }
        return secondScreenManager;
    }

    public synchronized void bind() {
        if (secondScreenService == null) {
            context.bindService(new Intent(IPoyntSecondScreenService.class.getName()),
                    mConnection, Context.BIND_AUTO_CREATE);
        } else {
            // already connected ?
        }
    }

    public void shutdown() {
        context.unbindService(mConnection);
    }

    /**
     * Class for interacting with the SecondScreenService
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.e(TAG, "IPoyntSecondScreenService is now connected");
            // Following the example above for an AIDL interface,
            // this gets an instance of the IRemoteInterface, which we can use to call on the service
            secondScreenService = IPoyntSecondScreenService.Stub.asInterface(service);
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG, "IPoyntSecondScreenService has unexpectedly disconnected - reconnecting");
            secondScreenService = null;
            bind();
        }
    };

    public void collectRating(final String requestId,
                              final IPoyntRatingAndReviewServiceListener listener) throws RemoteException {
        if (secondScreenService != null) {
            Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.mipmap.tru_rating_logo);
            Bitmap scaleImage = BitmapFactory.decodeResource(context.getResources(), R.mipmap.scale_image);

            secondScreenService.collectRating(0 /* min rating 8 */, 9 /* max raiting */, 1 /* increment */,
                    "How likely are you to recommend this place?", logo, scaleImage,
                    new IPoyntSecondScreenRatingEntryListener.Stub() {
                        @Override
                        public void onRatingEntered(int i) throws RemoteException {
                            Log.d(TAG, "Rating entered:" + i);
                            if (secondScreenService != null) {
                                // second argument is the background image for the screen
                                secondScreenService.displayMessage("Thank you for rating", null);
                                handler = new Handler(Looper.getMainLooper());
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            listener.onRatingCollected(requestId);
                                        } catch (RemoteException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, 3000l);

                            }
                        }

                        @Override
                        public void onRatingEntryCanceled() throws RemoteException {
                            Log.d(TAG, "Rating entry canceled");
                            if (secondScreenService != null) {
                                // second argument is the background image for the screen
                                secondScreenService.displayMessage("Sorry you did not rate", null);
                                handler = new Handler(Looper.getMainLooper());
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            listener.onRatingCanceled(requestId);
                                        } catch (RemoteException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, 3000l);

                            }

                        }

                    }
            );
        } else {
            Log.e(TAG, "Not connected to second screen service");
            listener.onRatingCanceled(requestId);
            bind();
        }
    }
}