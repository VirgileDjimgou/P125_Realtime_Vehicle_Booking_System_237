package com.android.gudana.apprtc.compatibility;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.android.gudana.R;
/*
ApiTwentyOnePlus.java
Copyright (C) 2012  Belledonne Communications, Grenoble, France

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

/**
 * @author Sylvain Berfini
 */
@TargetApi(21)
public class ApiTwentyOnePlus {

	@SuppressWarnings("deprecation")
	public static Notification createMessageNotification(Context context,
                                                         int msgCount, String msgSender, String msg, Bitmap contactIcon,
                                                         PendingIntent intent) {
		String title;
		if (msgCount == 1) {
			title = msgSender;
		} else {
			title = context.getString(R.string.unread_messages).replace("%i", String.valueOf(msgCount));
		}
		
		Notification notif = new Notification.Builder(context)
			.setContentTitle(title)
			.setContentText(msg)
			.setSmallIcon(R.drawable.topbar_chat_notification)
			.setAutoCancel(true)
			.setContentIntent(intent)
			.setDefaults(Notification.DEFAULT_ALL)
			.setLargeIcon(contactIcon)
			.setCategory(Notification.CATEGORY_MESSAGE)
			.setVisibility(Notification.VISIBILITY_PRIVATE)
			.setPriority(Notification.PRIORITY_HIGH)
			.setNumber(msgCount)
			.build();

		return notif;
	}

	public static Notification createInCallNotification(Context context,
                                                        String title, String msg, int iconID, Bitmap contactIcon,
                                                        String contactName, PendingIntent intent) {

		Notification notif = new Notification.Builder(context).setContentTitle(contactName)
			.setContentText(msg)
			.setSmallIcon(iconID)
			.setAutoCancel(false)
			.setContentIntent(intent)
			.setLargeIcon(contactIcon)
			.setCategory(Notification.CATEGORY_CALL)
			.setVisibility(Notification.VISIBILITY_PUBLIC)
			.setPriority(Notification.PRIORITY_HIGH)
			.build();
		
		return notif;
	}
	
	public static Notification createNotification(Context context, String title, String message, int icon, int level, Bitmap largeIcon, PendingIntent intent, boolean isOngoingEvent, int priority) {
		Notification notif;
		
		if (largeIcon != null) {
			notif = new Notification.Builder(context)
		        .setContentTitle(title)
		        .setContentText(message)
		        .setSmallIcon(icon, level)
		        .setLargeIcon(largeIcon)
		        .setContentIntent(intent)
				.setCategory(Notification.CATEGORY_SERVICE)
				.setVisibility(Notification.VISIBILITY_SECRET)
				.setPriority(priority)
		        .build();
		} else {
			notif = new Notification.Builder(context)
		        .setContentTitle(title)
		        .setContentText(message)
		        .setSmallIcon(icon, level)
		        .setContentIntent(intent)
				.setCategory(Notification.CATEGORY_SERVICE)
				.setVisibility(Notification.VISIBILITY_SECRET)
				.setPriority(priority)
		        .build();
		}
		
		return notif;
	}

	public static void removeGlobalLayoutListener(ViewTreeObserver viewTreeObserver, OnGlobalLayoutListener keyboardListener) {
		viewTreeObserver.removeOnGlobalLayoutListener(keyboardListener);		
	}

	public static Notification createMissedCallNotification(Context context, String title, String text, PendingIntent intent) {
		Notification notif = new Notification.Builder(context)
		.setContentTitle(title)
		.setContentText(text)
		.setSmallIcon(R.drawable.call_status_missed)
		.setAutoCancel(true)
		.setContentIntent(intent)
		.setDefaults(Notification.DEFAULT_ALL)
		.setCategory(Notification.CATEGORY_MESSAGE)
		.setVisibility(Notification.VISIBILITY_PRIVATE)
		.setPriority(Notification.PRIORITY_HIGH)
		.build();

		return notif;
	}

	public static Notification createSimpleNotification(Context context, String title, String text, PendingIntent intent) {
		Notification notif = new Notification.Builder(context)
		.setContentTitle(title)
		.setContentText(text)
		.setSmallIcon(R.drawable.linphone_logo)
		.setAutoCancel(true)
		.setContentIntent(intent)
		.setDefaults(Notification.DEFAULT_ALL)
		.setCategory(Notification.CATEGORY_MESSAGE)
		.setVisibility(Notification.VISIBILITY_PRIVATE)
		.setPriority(Notification.PRIORITY_HIGH)
		.build();

		return notif;
	}
}
