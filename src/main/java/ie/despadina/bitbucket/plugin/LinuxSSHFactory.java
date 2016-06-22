package ie.despadina.bitbucket.plugin;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.util.FS;

/**
 * Created by despadina on 15/06/2016.
 */
public class LinuxSSHFactory implements SSHFactory {

  public void config() {
    JschConfigSessionFactory sessionFactory = new JschConfigSessionFactory() {
      @Override
      protected void configure(OpenSshConfig.Host hc, Session session) {
        session.setConfig("StrictHostKeyChecking", "false");
      }

      @Override
      protected JSch createDefaultJSch(FS fs) throws JSchException {
        JSch defaultJSch = super.createDefaultJSch( fs );
        return defaultJSch;
      }
    };
    SshSessionFactory.setInstance(sessionFactory);
  }
}
