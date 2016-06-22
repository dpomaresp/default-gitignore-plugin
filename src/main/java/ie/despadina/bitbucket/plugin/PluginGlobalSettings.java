package ie.despadina.bitbucket.plugin;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by despadina on 16/06/2016.
 */
public class PluginGlobalSettings {
  private static final Logger log = LoggerFactory.getLogger(PluginGlobalSettings.class);

  private static final String PLUGIN_SETTINGS = "ie.despadina.bitbucket.plugin.gitignore.settings";
  private static Map<String, Object> settings = null;
  private static PluginSettings pluginSettings = null;

  public PluginGlobalSettings(PluginSettingsFactory pluginSettingsFactory) {
    pluginSettings = pluginSettingsFactory.createGlobalSettings();

    if(pluginSettings.get(PLUGIN_SETTINGS) == null) {
      settings = new HashMap<String, Object>();

      Map<String, Object> initSettingsMap = new HashMap<>();

      try{
        settings.put("userName", DefaultGitignoreActions.DEFAULT_AUTHOR_NAME);
        settings.put("userEmail", DefaultGitignoreActions.DEFAULT_AUTHOR_EMAIL);
        settings.put("sshKeyPath", DefaultGitignoreActions.DEFAULT_SSH_KEY_PATH);
        settings.put("gitignoreContent", DefaultGitignoreActions.getGitignoreTemplate());
      } catch(IOException e) {
        log.error(e.getMessage());
      }

      pluginSettings.put(PLUGIN_SETTINGS, settings);
    } else {
      settings = (Map<String, Object>) pluginSettings.get(PLUGIN_SETTINGS);
    }
  }

  private static void saveSettingsAndReload() {
    if(settings == null) {
      settings = new HashMap<String, Object>();
    }

    pluginSettings.put(PLUGIN_SETTINGS, settings);

    // Need for this?
    settings = (Map<String, Object>) pluginSettings.get(PLUGIN_SETTINGS);
  }

  // Return a hard copy of the map to prevent directly modifying it outside the class
  public static Map<String, Object> getSettings() {
    Map<String, Object> clone = new HashMap<>(settings);

    return clone;
  }

  public static Object getSetting(String key) {
    if(settings != null) {
      return settings.get(key);
    } else {
      throw new NullPointerException();
    }
  }

  public static Object saveSetting(String key, Object value) {
    if(settings != null) {
      settings.put(key, value);
      saveSettingsAndReload();

      return settings.get(key);
    } else {
      throw new NullPointerException();
    }
  }

  public static void saveSettings(Map<String, Object> newSettingsMap) {
    if(settings != null) {
      settings.putAll(newSettingsMap);
      saveSettingsAndReload();
    } else {
      throw new NullPointerException();
    }
  }

  public static Object removeSetting(String key) {
    if(settings != null) {
      Object obj = settings.remove(key);
      saveSettingsAndReload();

      if(settings.get(key) == null){
        return obj;
      } else {
        return null;
      }
    } else {
      throw new NullPointerException();
    }
  }

  public static Boolean containsSetting(String key) {
    if(settings != null) {
      return settings.containsKey(key);
    } else {
      throw new NullPointerException();
    }
  }
}
