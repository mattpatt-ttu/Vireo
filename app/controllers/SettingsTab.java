package controllers;

import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.mozilla.javascript.Context;
import org.tdl.vireo.model.AbstractOrderedModel;
import org.tdl.vireo.model.CustomActionDefinition;
import org.tdl.vireo.model.Person;
import org.tdl.vireo.model.Preference;
import org.tdl.vireo.model.RoleType;

import controllers.settings.ApplicationSettingsTab;
import controllers.settings.UserPreferencesTab;

import play.mvc.Controller;
import play.mvc.With;

/**
 * Parent class for all setting tab controllers.
 * 
 * This class shares code between the several children controllers, as well as
 * handles the ajax updates for the profile information since they are on every
 * page.
 * 
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 */
@With(Authentication.class)
public class SettingsTab extends AbstractVireoController {

	/**
	 * Redirect to the user's preference tab.
	 */
	@Security(RoleType.REVIEWER)
	public static void settingsRedirect() {
		UserPreferencesTab.userPreferences();
	}

	/**
	 * Update a user's admin profile information. This includes three things.
	 * The user's display name, their preferred email address, and a flag to be
	 * cc'ed anytime something is emailed.
	 * 
	 * @param field
	 *            The name of the field either: displayName,
	 *            currentEmailAddress, or ccEmail.
	 * @param value
	 *            The new value of the field.
	 */
	@Security(RoleType.REVIEWER)
	public static void updateProfileJSON(String field, String value) {
		try {
			if (value != null && value.trim().length() == 0)
				value = null;
			
			Person person = context.getPerson();
			
			if ("displayName".equals(field)) {
				person.setDisplayName(value);
			} else if ("currentEmailAddress".equals(field)) {
				if (value == null || value.trim().length() == 0)
					throw new IllegalArgumentException("An email address is required.");
				
				new InternetAddress(value).validate();
				person.setCurrentEmailAddress(value);
			} else if ("ccEmail".equals(field)) {
				
				Preference ccEmail = person.getPreference("ccEmail");
				if (value == null && ccEmail != null) {
					// remove the setting
					ccEmail.delete();
				} else if (value != null && ccEmail == null) {
					person.addPreference("ccEmail", "true").save();
				}
			} else {
				throw new IllegalArgumentException("Unknown field type.");
			}
			
			person.save();
			
			String displayName = person.getDisplayName();
			String currentEmailAddress = person.getCurrentEmailAddress();
			
			displayName = escapeJavaScript(displayName);
			currentEmailAddress = escapeJavaScript(currentEmailAddress);
						
			renderJSON("{ \"success\": \"true\", \"displayName\": \""+displayName+"\", \"currentEmailAddress\": \""+currentEmailAddress+"\" }");
			
		} catch (AddressException ae) {
			renderJSON("{ \"failure\": \"true\", \"message\": \"The email address is not valid. It should be formatted like 'your-username@your-domain' without any spaces and includes one @ sign.\" }");
			
		} catch (RuntimeException re) {
			String message = escapeJavaScript(re.getMessage());
			renderJSON("{ \"failure\": \"true\", \"message\": \""+message+"\" }");
			
		}
	}
	
	/**
	 * Internal method to persist the current ordering of the model.
	 * 
	 * Each item in the list will have it's display order updated according to
	 * it's position in the provided list. Then the item will be saved() to
	 * persistant storage.
	 * 
	 * @param models
	 *            A List of ordered models.
	 */
	protected static void saveModelOrder(
			List<? extends AbstractOrderedModel> models) {
		int i = 0;
		for (AbstractOrderedModel model : models) {
			model.setDisplayOrder(i);
			model.save();
			i = i + 10;
		}
	}
	
	/**
	 * Internal method to resolve the ids from a sortable element.
	 * 
	 * The jQuery sortable library expects all ids to be of the format: type_id.
	 * This method will process a comma sepeareted list of these jquery-based
	 * ids, resolving them into their actual model objects according to the ids
	 * position within the idString.
	 * 
	 * 
	 * @param idString
	 *            Comma seperated list of ids and labels: i.e. action_1,
	 *            action_2, etc...
	 * @param type
	 *            The type of model.
	 * @return A list of models
	 */
	protected static <T extends AbstractOrderedModel> List<T> resolveIds(String idString, Class<T> type) {
		
		List<T> models = new ArrayList<T>();
		
		String[] idArray = idString.split(",");
		for (String idElement : idArray) {
			
			String[] parts = idElement.split("_");
			
			Long id = Long.valueOf(parts[1]);
			
			if (type.equals(CustomActionDefinition.class)){
				CustomActionDefinition action = settingRepo.findCustomActionDefinition(id);
				models.add((T) action);		
			} else {
				throw new IllegalArgumentException("Unknown model type: "+type.getName());
			}
		}
		
		return models;
	}
	
}
