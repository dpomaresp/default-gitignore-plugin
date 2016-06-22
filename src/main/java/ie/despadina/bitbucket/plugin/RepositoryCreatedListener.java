package ie.despadina.bitbucket.plugin;

import com.atlassian.bitbucket.event.repository.RepositoryCreatedEvent;
import com.atlassian.event.api.EventListener;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepositoryCreatedListener {

    private static final Logger log = LoggerFactory.getLogger(RepositoryCreatedListener.class);

    private final ApplicationProperties applicationProperties;
    private DefaultGitignoreActions defaultGitIgnoreActions;

    private PluginSettings pluginSettings = null;

    public RepositoryCreatedListener(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @EventListener
    public void addDefaultGitignoreOnRepositoryCreated(RepositoryCreatedEvent event) {
        defaultGitIgnoreActions = new DefaultGitignoreActions(event.getRepository(), pluginSettings);

        try {
            defaultGitIgnoreActions.addToNewBitbucketRepository();
        } catch(Exception e) {
            log.error(e.getMessage());
        }
    }
}
