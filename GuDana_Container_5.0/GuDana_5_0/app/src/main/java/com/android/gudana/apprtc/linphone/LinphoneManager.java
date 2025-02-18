/*
LinphoneManager.java
Copyright (C) 2010  Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package com.android.gudana.apprtc.linphone;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.preference.CheckBoxPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.gudana.R;

import org.linphone.core.CallDirection;
import org.linphone.core.LinphoneAccountCreator;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneBuffer;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCall.State;
import org.linphone.core.LinphoneCallParams;
import org.linphone.core.LinphoneCallStats;
import org.linphone.core.LinphoneChatMessage;
import org.linphone.core.LinphoneChatRoom;
import org.linphone.core.LinphoneContent;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCore.AuthMethod;
import org.linphone.core.LinphoneCore.EcCalibratorStatus;
import org.linphone.core.LinphoneCore.GlobalState;
import org.linphone.core.LinphoneCore.LogCollectionUploadState;
import org.linphone.core.LinphoneCore.RegistrationState;
import org.linphone.core.LinphoneCore.RemoteProvisioningState;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneCoreListener;
import org.linphone.core.LinphoneEvent;
import org.linphone.core.LinphoneFriend;
import org.linphone.core.LinphoneFriendList;
import org.linphone.core.LinphoneInfoMessage;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.core.OpenH264DownloadHelperListener;
import org.linphone.core.PayloadType;
import org.linphone.core.PresenceActivityType;
import org.linphone.core.PresenceModel;
import org.linphone.core.PublishState;
import org.linphone.core.SubscriptionState;
import org.linphone.core.TunnelConfig;
import org.linphone.mediastream.Log;
import org.linphone.mediastream.Version;
import org.linphone.mediastream.video.capture.hwconf.AndroidCameraConfiguration;
import org.linphone.mediastream.video.capture.hwconf.AndroidCameraConfiguration.AndroidCamera;
import org.linphone.mediastream.video.capture.hwconf.Hacks;
import org.linphone.tools.OpenH264DownloadHelper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.media.AudioManager.MODE_RINGTONE;
import static android.media.AudioManager.STREAM_RING;
import static android.media.AudioManager.STREAM_VOICE_CALL;


import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.gudana.hify.utils.Config;
import com.android.gudana.R;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;


/**
 *
 * Manager of the low level LibLinphone stuff.<br />
 * Including:<ul>
 * <li>Starting C liblinphone</li>
 * <li>Reacting to C liblinphone state changes</li>
 * <li>Calling Linphone android service listener methods</li>
 * <li>Interacting from Android GUI/service with low level SIP stuff/</li>
 * </ul>
 *
 * Add Service Listener to react to Linphone state changes.
 *
 * @author Guillaume Beraudo
 *
 */
public class LinphoneManager {

	private static LinphoneManager instance;
	private Context mServiceContext;
	private AudioManager mAudioManager;
	private PowerManager mPowerManager;
	private Resources mR;
	private LinphonePreferences mPrefs;
	private LinphoneCore mLc;
	private OpenH264DownloadHelper mCodecDownloader;
	private OpenH264DownloadHelperListener mCodecListener;
	private String lastLcStatusMessage;
	private String basePath;
	private static boolean sExited;
	private boolean mAudioFocused;
	private boolean echoTesterIsRunning;
	private int mLastNetworkType=-1;
	private ConnectivityManager mConnectivityManager;
	private BroadcastReceiver mKeepAliveReceiver;
	private IntentFilter mKeepAliveIntentFilter;
	private Handler mHandler = new Handler();
	private WakeLock mIncallWakeLock;
	private LinphoneAccountCreator accountCreator;
	private static List<LinphoneChatMessage> mPendingChatFileMessage;
	private static LinphoneChatMessage mUploadPendingFileMessage;


	public String wizardLoginViewDomain = null;

	private static List<LinphoneChatMessage.LinphoneChatMessageListener> simpleListeners = new ArrayList<LinphoneChatMessage.LinphoneChatMessageListener>();
	public static void addListener(LinphoneChatMessage.LinphoneChatMessageListener listener) {
		if (!simpleListeners.contains(listener)) {
			simpleListeners.add(listener);
		}
	}
	public static void removeListener(LinphoneChatMessage.LinphoneChatMessageListener listener) {
		simpleListeners.remove(listener);
	}

	public LinphoneManager(final Context c) {
		sExited = false;
		echoTesterIsRunning = false;
		mServiceContext = c;
		basePath = c.getFilesDir().getAbsolutePath();
		mLPConfigXsd = basePath + "/lpconfig.xsd";
		mLinphoneFactoryConfigFile = basePath + "/linphonerc";
		mLinphoneConfigFile = basePath + "/.linphonerc";
		mLinphoneRootCaFile = basePath + "/rootca.pem";
		mRingSoundFile = basePath + "/ringtone.mkv";
		mRingbackSoundFile = basePath + "/ringback.wav";
		mPauseSoundFile = basePath + "/hold.mkv";
		mChatDatabaseFile = basePath + "/linphone-lin_history.db";
		mCallLogDatabaseFile = basePath + "/linphone-log-lin_history.db";
		mFriendsDatabaseFile = basePath + "/linphone-friends.db";
		mErrorToneFile = basePath + "/error.wav";
		mUserCertificatePath = basePath;

		mPrefs = LinphonePreferences.instance();
		mAudioManager = ((AudioManager) c.getSystemService(Context.AUDIO_SERVICE));
		mVibrator = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
		mPowerManager = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
		mConnectivityManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		mR = c.getResources();
		mPendingChatFileMessage = new ArrayList<LinphoneChatMessage>();
	}

	private static final int LINPHONE_VOLUME_STREAM = STREAM_VOICE_CALL;
	private static final int dbStep = 4;
	/** Called when the activity is first created. */
	private final String mLPConfigXsd;
	private final String mLinphoneFactoryConfigFile;
	private final String mLinphoneRootCaFile;
	public final String mLinphoneConfigFile;
	private final String mRingSoundFile;
	private final String mRingbackSoundFile;
	private final String mPauseSoundFile;
	private final String mChatDatabaseFile;
	private final String mCallLogDatabaseFile;
	private final String mFriendsDatabaseFile;
	private final String mErrorToneFile;
	private final String mUserCertificatePath;
	private ByteArrayInputStream mUploadingImageStream;
	private Timer mTimer;

	private MediaPlayer mRingerPlayer, mediaPlayer;
	private Vibrator mVibrator;
	private int savedMaxCallWhileGsmIncall;
	private synchronized void preventSIPCalls() {
		if (savedMaxCallWhileGsmIncall != 0) {
			Log.w("SIP calls are already blocked due to GSM call running");
			return;
		}
		savedMaxCallWhileGsmIncall = mLc.getMaxCalls();
		mLc.setMaxCalls(0);
	}
	private synchronized void allowSIPCalls() {
		if (savedMaxCallWhileGsmIncall == 0) {
			Log.w("SIP calls are already allowed as no GSM call known to be running");
			return;
		}
		mLc.setMaxCalls(savedMaxCallWhileGsmIncall);
		savedMaxCallWhileGsmIncall = 0;
	}
	public static void setGsmIdle(boolean gsmIdle) {
		LinphoneManager mThis = instance;
		if (mThis == null) return;
		if (gsmIdle) {
			mThis.allowSIPCalls();
		} else {
			mThis.preventSIPCalls();
		}
	}


	private boolean isRinging;

	private void requestAudioFocus(int stream){
		if (!mAudioFocused){
			int res = mAudioManager.requestAudioFocus(null, stream, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT );
			// android.util.Log.d()
			android.util.Log.d("log ", "Audio focus requested: " + (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED ? "Granted" : "Denied"));
			if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) mAudioFocused=true;
		}
	}


	public  synchronized void startRinging()  {
		/*
		if (!LinphonePreferences.instance().isDeviceRingtoneEnabled()) {
			// Enable speaker lin_audio route, linphone library will do the ringing itself automatically
			routeAudioToSpeaker();
			return;
		}

		*/

		if (Hacks.needGalaxySAudioHack())
			mAudioManager.setMode(MODE_RINGTONE);

		try {
			if ((mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE || mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) && mVibrator != null) {
				long[] patern = {0,1000,1000};
				mVibrator.vibrate(patern, 1);
			}
			if (mRingerPlayer == null) {
				requestAudioFocus(STREAM_RING);
				mRingerPlayer = new MediaPlayer();
				mRingerPlayer.setAudioStreamType(STREAM_RING);

				// hier konnte wir eine Problem haben ....
				String ringtone = Settings.System.DEFAULT_RINGTONE_URI.toString();
				try {
					if (ringtone.startsWith("content://")) {
						mRingerPlayer.setDataSource(mServiceContext, Uri.parse(ringtone));

						// mRingerPlayer.setDataSource(mServiceContext, Uri.parse("android.resource://" + mServiceContext.getPackageName() + "/raw/librem_by_feandesign_call"));
						// Uri.parse("android.resource://" + mContext.getPackageName() + "/raw/librem_by_feandesign_call");
					} else {
						FileInputStream fis = new FileInputStream(ringtone);
						mRingerPlayer.setDataSource(fis.getFD());
						fis.close();
					}
				} catch (Exception e) {
					// Log.e(e, "Cannot set ringtone");
					e.printStackTrace();
				}

				mRingerPlayer.prepare();
				mRingerPlayer.setLooping(true);
				mRingerPlayer.start();
			} else {
				//Log.w("already ringing");
				android.util.Log.d("ringing", "already ringing");
			}
		} catch (Exception e) {
			e.printStackTrace();
			// Log.e(e,"cannot handle incoming call");
		}
		isRinging = true;
	}

	public void PlayRingBack(Context mContext){
		//String callRingtonePreferenceString = appPreferences.getCallRingtoneUri();
		Uri ringtoneUri;

			ringtoneUri = Uri.parse("android.resource://" + mContext.getPackageName() +
					"/raw/librem_by_feandesign_call");


		if (ringtoneUri != null) {
			mediaPlayer = MediaPlayer.create(mContext, ringtoneUri);
			mediaPlayer.setLooping(true);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
			mediaPlayer.start();
		}
	}


	public  synchronized void startRinging_without_vibrate(Context mContext)  {
		/*
		if (!LinphonePreferences.instance().isDeviceRingtoneEnabled()) {
			// Enable speaker lin_audio route, linphone library will do the ringing itself automatically
			routeAudioToSpeaker();
			return;
		}

		*/

		if (Hacks.needGalaxySAudioHack())
			mAudioManager.setMode(MODE_RINGTONE);

		try {
			if ((mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE || mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) && mVibrator != null) {
				long[] patern = {0,1000,1000};
				// mVibrator.vibrate(patern, 1);
			}
			if (mRingerPlayer == null) {
				requestAudioFocus(STREAM_RING);
				mRingerPlayer = new MediaPlayer();
				mRingerPlayer.setAudioStreamType(STREAM_RING);

				// hier konnte wir eine Problem haben ....
				// String ringtone = Settings.System.RI
				//String ringtone = Settings.System.DEFAULT_RINGTONE_URI.toString();
				//String packName = mContext.getPackageName().toString();
				//String ringtone = Uri.parse("android.resource://"+packName+"/"+R.raw.ringback).toString();
				// Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.hify_sound)

				try {

					mRingerPlayer.setDataSource(mServiceContext, Uri.parse("android.resource://" + mServiceContext.getPackageName() + "/raw/librem_by_feandesign_call"));


					//FileInputStream fis = new FileInputStream(ringtone);
						//mRingerPlayer.setDataSource(fis.getFD());
						//fis.close();
					System.out.println("text");

				} catch (Exception e) {
					// Log.e(e, "Cannot set ringtone");
					e.printStackTrace();
				}

				mRingerPlayer.prepare();
				mRingerPlayer.setLooping(true);
				mRingerPlayer.start();
			} else {
				//Log.w("already ringing");
				android.util.Log.d("ringing", "already ringing");
			}
		} catch (Exception e) {
			e.printStackTrace();
			// Log.e(e,"cannot handle incoming call");
		}
		isRinging = true;
	}

	public synchronized void stopRinging() {
		try{

			if (mRingerPlayer != null) {
				mRingerPlayer.stop();
				mRingerPlayer.release();
				mRingerPlayer = null;
			}
			if (mVibrator != null) {
				mVibrator.cancel();
			}

			if (Hacks.needGalaxySAudioHack())
				mAudioManager.setMode(AudioManager.MODE_NORMAL);

			isRinging = false;

		}catch(Exception ex){
			ex.printStackTrace();
		}

	}


	public void adjustVolume(int i) {
		if (Build.VERSION.SDK_INT < 15) {
			int oldVolume = mAudioManager.getStreamVolume(LINPHONE_VOLUME_STREAM);
			int maxVolume = mAudioManager.getStreamMaxVolume(LINPHONE_VOLUME_STREAM);

			int nextVolume = oldVolume +i;
			if (nextVolume > maxVolume) nextVolume = maxVolume;
			if (nextVolume < 0) nextVolume = 0;

			mLc.setPlaybackGain((nextVolume - maxVolume)* dbStep);
		} else
			// starting from ICS, volume must be adjusted by the application, at least for STREAM_VOICE_CALL volume stream
			mAudioManager.adjustStreamVolume(LINPHONE_VOLUME_STREAM, i < 0 ? AudioManager.ADJUST_LOWER : AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
	}



}
