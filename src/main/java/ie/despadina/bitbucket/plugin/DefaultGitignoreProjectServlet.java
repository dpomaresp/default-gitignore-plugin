package ie.despadina.bitbucket.plugin;

import com.atlassian.bitbucket.nav.NavBuilder;
import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.project.ProjectService;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.google.common.collect.ImmutableMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by despadina on 15/06/2016.
 */
public class DefaultGitignoreProjectServlet extends AbstractServlet {
  private final ProjectService projectService;
  private final Boolean DEFAULT_PLUGIN_ENABLED = true;
  private Map<String, Object> fields;
  private Map<String, Object> fieldErrors;
  private Project project;

  public DefaultGitignoreProjectServlet(SoyTemplateRenderer soyTemplateRenderer, ProjectService projectService,
      NavBuilder navBuilder) {
    super(soyTemplateRenderer);
    this.projectService = projectService;

    this.fields = new HashMap<>();
    this.fieldErrors = new HashMap<>();
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    fields.clear();
    getProject(req, resp);

    Object obj = PluginGlobalSettings.getSetting(project.getKey());

    if(obj == null) {
      obj = PluginGlobalSettings.saveSetting(project.getKey(), DEFAULT_PLUGIN_ENABLED);
    }

    for (Map.Entry<String, Object> entry : PluginGlobalSettings.getSettings().entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      fields.put(key, value);
    }

    render(resp, "plugin.project.config",
        ImmutableMap.<String, Object>builder()
            .put("config", fields)
            .put("errors", fieldErrors)
            .put("project", project)
            .build()
    );
  }

  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    fields.clear();
    fieldErrors.clear();

    for (Object key : req.getParameterMap().keySet()) {
      String parameterName = (String) key;

      if(parameterName.equals(project.getKey())) {
        Boolean enabled = req.getParameter(parameterName).equals("Enabled") ? true : false;
        fields.put(parameterName, enabled);
      }
    }

    validateSettings();

    if (fieldErrors.isEmpty()) {
      PluginGlobalSettings.saveSetting(project.getKey(), fields.get(project.getKey()));
    }

      resp.reset();
      resp.setContentType("text/html;charset=UTF-8");
      try {
        render(resp, "plugin.project.config",
            ImmutableMap.<String, Object>builder()
                .put("config", fields)
                .put("errors", fieldErrors)
                .put("project", project)
                .build()
        );
      } catch (SoyException e) {
        Throwable cause = e.getCause();
        if (cause instanceof IOException) {
          throw (IOException) cause;
        }
        throw new ServletException(e);
      }
      return;
  }

  private void validateSettings() {
    if(fields.get("Enabled") == null && fields.get("Disabled") == null) {
      fieldErrors.put("pluginEnabled", "Expecting a disabled or enabled value for the plugin");
    }
  }

  private void getProject(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // Get projectKey from path
    String pathInfo = req.getPathInfo();

    String[] components = pathInfo.split("/");

    if (components.length < 2) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    project = projectService.getByKey(components[1]);

    if (project == null) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
  }
}
