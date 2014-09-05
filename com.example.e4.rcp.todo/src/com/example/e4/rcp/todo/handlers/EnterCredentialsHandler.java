package com.example.e4.rcp.todo.handlers;

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.prefs.BackingStoreException;

import com.example.e4.rcp.todo.dialogs.ServerLoginDialog;
import com.example.e4.rcp.todo.preferences.PreferenceConstants;

public class EnterCredentialsHandler {
	
	@Inject
	@Preference(nodePath = PreferenceConstants.TODO_NODEPATH, value = PreferenceConstants.USER_PREF_KEY)
	String userPref;
	@Inject
	@Preference(nodePath = PreferenceConstants.TODO_NODEPATH, value = PreferenceConstants.PASSWORD_PREF_KEY)
	String passwordPref;
	@Inject
	@Preference(nodePath = PreferenceConstants.TODO_NODEPATH, value = PreferenceConstants.SERVER_URI_PREF_KEY)
	String serverUri;
	
	@Execute
	public void execute(
			Shell shell,
			@Preference IEclipsePreferences prefs) {
		ServerLoginDialog dialog = new ServerLoginDialog(shell);
		
		if (userPref!=null) {
			dialog.setUser(userPref);
		}
		
		if (passwordPref!=null) {
			dialog.setPassword(passwordPref);
		}
		if (serverUri != null) {
			dialog.setServerUri(serverUri);
		}
		
		// get the new values from the dialog
		if (dialog.open() == Window.OK) {
			prefs.put("user", dialog.getUser());
			prefs.put("password", dialog.getPassword());
			try {
				prefs.flush();
			} catch (BackingStoreException e) {
				e.printStackTrace();
			}
		}
	}

	
}
