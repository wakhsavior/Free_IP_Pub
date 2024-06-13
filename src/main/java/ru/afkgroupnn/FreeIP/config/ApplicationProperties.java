package ru.afkgroupnn.FreeIP.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;

@Data
@Configuration
@PropertySource("file:./config/daemon.properties")
@ConfigurationProperties(prefix = "app")
@EnableAspectJAutoProxy
public class ApplicationProperties {

  private Integer timeBetweenStart;
  private Boolean daemonStatus;
  private String exportDataPath;
  private String clustersDataPath;
  private String clustersFile;
  private String poolIpAddressFile;
  private String poolSwitchesFile;
  private String ipRangeFile;
  private String fileClasses;
  private String fileStaticEnd;
  private String fileDenyEnd;
  private int daysToDeleteIP;

  @Override
  public String toString() {
    return "ApplicationProperties{" +
            "timeBetweenStart=" + timeBetweenStart +
            ", daemonStatus=" + daemonStatus +
            ", exportDataPath='" + exportDataPath + '\'' +
            ", clustersDataPath='" + clustersDataPath + '\'' +
            ", clustersFile='" + clustersFile + '\'' +
            ", poolIpAddressFile='" + poolIpAddressFile + '\'' +
            ", poolSwitchesFile='" + poolSwitchesFile + '\'' +
            ", ipRangeFile='" + ipRangeFile + '\'' +
            ", daysToDeleteIP=" + daysToDeleteIP +
            '}';
  }
}
