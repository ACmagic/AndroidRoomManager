package edu.cmu.sv.arm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class AlertDialogHelper {
	public static void buildAlertDialog(Context context, String title, String message, String button) {
		AlertDialog.Builder alert = new AlertDialog.Builder(context);
    	alert.setTitle(title);
    	alert.setMessage(message);
    	alert.setPositiveButton(button, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
    	alert.show();
	}
}
