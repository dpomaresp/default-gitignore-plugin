package ie.despadina.bitbucket.plugin;

import com.atlassian.bitbucket.nav.NavBuilder;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DefaultGitignoreConfigServlet extends AbstractServlet {
    private static final Logger log = LoggerFactory.getLogger(DefaultGitignoreConfigServlet.class);

    private final NavBuilder navBuilder;
    private Map<String, Object> fields;
    private Map<String, Object> fieldErrors;

    public DefaultGitignoreConfigServlet(SoyTemplateRenderer soyTemplateRenderer, NavBuilder navBuilder) {
        super(soyTemplateRenderer);
        this.navBuilder = navBuilder;
        this.fields = new HashMap<>();
        this.fieldErrors = new HashMap<>();
    }


    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        fields.clear();
        fieldErrors.clear();

        for (Map.Entry<String, Object> entry : PluginGlobalSettings.getSettings().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String) {
                fields.put(key, value);
            } else {

            }
        }

        resp.setContentType("text/html;charset=UTF-8");
        try {
            render(resp, "plugin.gitignore.config",
                ImmutableMap
                    .<String, Object>builder()
                    .put("config", fields)
                    .put("errors", fieldErrors)
                    .build()
            );
        } catch (SoyException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new ServletException(e);
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        fields.clear();
        fieldErrors.clear();

        for (Object key : req.getParameterMap().keySet()) {
            String parameterName = (String) key;

            if(!parameterName.equals("submit")) {
                fields.put(parameterName, (String) req.getParameter(parameterName));
            }
        }

        validateFields();

        if (fieldErrors.size() > 0) {
            resp.reset();
            resp.setContentType("text/html;charset=UTF-8");
            try {
                render(resp, "plugin.gitignore.config",
                    ImmutableMap
                        .<String, Object>builder()
                        .put("config", fields)
                        .put("errors", fieldErrors)
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

        PluginGlobalSettings.saveSettings(fields);

        String redirectUrl;
        redirectUrl = navBuilder.addons().buildRelative();
        log.debug("redirect: " + redirectUrl);
        resp.sendRedirect(redirectUrl);
    }

    private void validateFields() {
        // Validates the ssh key path
        try {
            String path = (String) fields.get("sshKeyPath");
            File sshKey = new File(path);
            if(!sshKey.exists() || sshKey.isDirectory()){
                fieldErrors.put("sshKeyPath", "'" + path + "' does not exists and/or is a directory");
            }
        } catch (Exception e) {
            fieldErrors.put("sshKeyPath", e.getMessage());
        } finally {
            if(fields.get("gitignoreContent") == null || fields.get("gitignoreContent") == "") {
                fieldErrors.put("gitignoreContent", "This field can not be empty");
            }

            if(fields.get("userName") == null || fields.get("userName") == "") {
                fieldErrors.put("userName", "This field can not be empty");
            }

            if(fields.get("userEmail") == null || fields.get("userEmail") == "") {
                fieldErrors.put("userEmail", "This field can not be empty");
            }
        }

    }

}