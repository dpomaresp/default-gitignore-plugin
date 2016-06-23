package ie.despadina.bitbucket.plugin;

import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class DefaultGitignoreActions {
  private String localPath, remotePath;
  private Git git;
  public final static String DEFAULT_AUTHOR_NAME = "admin";
  public final static String DEFAULT_AUTHOR_EMAIL = "admin@example.com";
  private final String COMMIT_MESSAGE= "Default .gitignore added";
  private static final String GITIGNORE_TEMPLATE = "gitignore_template.txt";
  public static final String DEFAULT_SSH_KEY_PATH = "E:/.ssh/bitbucket_admin_rsa";

  private final String WINDOWS_TEMP_FOLDER_PATH = "C:/Windows/Temp/bitbucket/plugins/gitignore/";
  private final String LINUX_TEMP_FOLDER_PATH = "/tmp/bitbucket/plugins/gitignore/";
  private final String BASE_REMOTE_URL = "ssh://git@localhost:7999/";
  private final String BASE_LOCAL_URL;
  private SSHFactory sshFactory = null;
  private String sshKeyPath = null;
  private InputStream templateInputStream = null;
  private String userName = null;
  private String userEmail = null;
  private Repository bitbucketRepository;

  public DefaultGitignoreActions(Repository bitbucketRepository, PluginSettings pluginSettings) {
    loadPluginSettings();
    this.bitbucketRepository = bitbucketRepository;

    // Check whether the server's OS is Windows or Linux
    if(System.getProperty("os.name").contains("Windows")) {
      sshFactory = new WindowsSSHFactory(sshKeyPath);
      this.BASE_LOCAL_URL = WINDOWS_TEMP_FOLDER_PATH;
    } else {
      this.BASE_LOCAL_URL = LINUX_TEMP_FOLDER_PATH;
      sshFactory = new LinuxSSHFactory();
    }

    sshFactory.config();

    localPath = BASE_LOCAL_URL + bitbucketRepository.getProject().getKey() + "/" + bitbucketRepository.getSlug();
    remotePath = BASE_REMOTE_URL + bitbucketRepository.getProject().getKey()  + "/" +
        bitbucketRepository.getSlug() + ".git";
  }

  private Boolean isPluginEnable() {
    Boolean isEnabled = true;
    if(PluginGlobalSettings.containsSetting(bitbucketRepository.getProject().getKey())) {

      Boolean pluginEnabled = Boolean.valueOf((String)PluginGlobalSettings.getSetting(bitbucketRepository.getProject().getKey()));

      if(!pluginEnabled) {
        isEnabled = false;
      }
    }

    return isEnabled;
  }

  public void addToNewBitbucketRepository() throws IOException, GitAPIException{
    if(isPluginEnable() && !bitbucketRepository.isFork()) {
      cloneRepository();
      copyAndStageGitignoreFile();
      commitGitignore();
      push();
      removeClonedRepositoryFolder();
    }
  }

  public static String getGitignoreTemplate() throws IOException {
      InputStream inputStream = DefaultGitignoreActions.class.getResourceAsStream("/" + GITIGNORE_TEMPLATE);

      return IOUtils.toString(inputStream);
  }

  private void cloneRepository() throws GitAPIException {
    git = Git.cloneRepository()
      .setURI(remotePath)
      .setDirectory(new File(localPath))
      .call();
  }

  private void copyAndStageGitignoreFile() throws IOException, GitAPIException {
    File targetFile = new File(localPath + "\\.gitignore");
    FileUtils.copyInputStreamToFile(this.templateInputStream, targetFile);
    git.add().addFilepattern(".gitignore").call();
  }

  private void commitGitignore() throws GitAPIException {
    git.commit().setMessage(COMMIT_MESSAGE).setAuthor(DEFAULT_AUTHOR_NAME, DEFAULT_AUTHOR_EMAIL).call();
  }

  private void push() throws GitAPIException {
    git.push().call();
    git.getRepository().close();
  }

  private void removeClonedRepositoryFolder() throws IOException {
    FileUtils.deleteDirectory(new File(BASE_LOCAL_URL));
  }

  private void loadPluginSettings() {
    String templateContent = (String) PluginGlobalSettings.getSetting("gitignoreContent");
    this.templateInputStream = new ByteArrayInputStream(templateContent.getBytes(StandardCharsets.UTF_8));
    this.userName = (String) PluginGlobalSettings.getSetting("userName");
    this.userEmail = (String) PluginGlobalSettings.getSetting("userEmail");
    this.sshKeyPath = (String) PluginGlobalSettings.getSetting("sshKeyPath");
  }
}
