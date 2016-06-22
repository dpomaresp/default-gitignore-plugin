package ie.despadina.bitbucket.plugin;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.FS;

/**
 * Created by despadina on 15/06/2016.
 */
public class WindowsSSHFactory implements SSHFactory {
  private String  sshKeyPath = null;

  public WindowsSSHFactory(String sshKeyPath) {
    this.sshKeyPath = sshKeyPath;
  }

  public void setSshKeyPath(String sshKeyPath) {
    this.sshKeyPath = sshKeyPath;
  }

  public void config() {
    JschConfigSessionFactory sessionFactory = new JschConfigSessionFactory() {
      @Override
      protected void configure(OpenSshConfig.Host hc, Session session) {
        CredentialsProvider provider = new CredentialsProvider() {
          @Override
          public boolean isInteractive() {
            return false;
          }

          @Override
          public boolean supports(CredentialItem... items) {
            return true;
          }

          @Override
          public boolean get(URIish uri, CredentialItem... items) throws UnsupportedCredentialItem {
            return true;
          }
        };
        UserInfo userInfo = new CredentialsProviderUserInfo(session, provider);
        session.setConfig("StrictHostKeyChecking", "false");
        session.setUserInfo(userInfo);

      }

      @Override
      protected JSch createDefaultJSch(FS fs) throws JSchException {
        JSch defaultJSch = super.createDefaultJSch( fs );
        defaultJSch.addIdentity(sshKeyPath);
        return defaultJSch;
      }
    };
    SshSessionFactory.setInstance(sessionFactory);
  }
}
