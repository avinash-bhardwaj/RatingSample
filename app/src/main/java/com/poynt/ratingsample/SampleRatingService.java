package com.poynt.ratingsample;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import co.poynt.os.model.Payment;
import co.poynt.os.services.v1.IPoyntRatingAndReviewService;
import co.poynt.os.services.v1.IPoyntRatingAndReviewServiceListener;

public class SampleRatingService extends Service {
    private static final String TAG = SampleRatingService.class.getSimpleName();

    public SampleRatingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IPoyntRatingAndReviewService.Stub mBinder = new IPoyntRatingAndReviewService.Stub() {


        @Override
        // The rating will have to be entered within 15 seconds, otherwise it will auto timeout
        // The first time Payment Fragment loads the rating screen may get skipped. This is because
        // the first initialization can take a bit of time
        public void collect(Payment payment,
                            String requestId,
                            IPoyntRatingAndReviewServiceListener iPoyntRatingAndReviewServiceListener)
                throws RemoteException {
            Log.d(TAG, "collect(): " + requestId);
            // MUST always call the listener
            if (RatingSampleApplication.getInstance().getSecondScreenManager() != null) {
                RatingSampleApplication.getInstance().getSecondScreenManager()
                        .collectRating(requestId, iPoyntRatingAndReviewServiceListener);
            } else {
                Log.d(TAG, "Not connecting to second screen manager - canceling the request " + requestId);
                iPoyntRatingAndReviewServiceListener.onRatingCanceled(requestId);
            }
        }

        // If the Payment Fragment does not receive a callback via IPoyntRatingAndReviewServiceListener
        // either via .onRatingCanceled or .onRatingCollected
        @Override
        public void cancel(String requestId)
                throws RemoteException {
            Log.d(TAG, "cancel() requested for requestId: " + requestId);
        }
    };
}
