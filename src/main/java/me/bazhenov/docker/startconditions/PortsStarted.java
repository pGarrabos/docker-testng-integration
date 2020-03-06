package me.bazhenov.docker.startconditions;

import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import me.bazhenov.docker.Docker;

public class PortsStarted implements Condition  {

  private static final int MIN_PORT_RANGE = 0;
  private static final int MAX_PORT_RANGE = 65536;
  private static final Logger log = getLogger(PortsStarted.class);
  private static final String IPV6_FILE_PATH = "/proc/self/net/tcp6";
  
  
  @Override
  public boolean unsure(String cid, List<String> parameters, int timeout) throws IOException, InterruptedException {
    Docker docker = Docker.getInstance();
    
    Set<Integer> ports = extractPorts(parameters);
    
    Thread self = currentThread();
    long start = currentTimeMillis();
    boolean reported = false;
    while (!self.isInterrupted() || currentTimeMillis() < start + timeout) {
        Set<Integer> openPorts = new HashSet<>();
        openPorts.addAll(readListenPorts(docker.executeDockerCommands("exec", cid, "cat", "/proc/self/net/tcp")));
        openPorts.addAll(readListenPorts(docker.executeDockerCommands("exec", cid, "sh", "-c", "if [ -f " + IPV6_FILE_PATH + " ]; then cat " + IPV6_FILE_PATH + "; fi")));
        
        if (openPorts.containsAll(ports))
            return true;

        docker.checkContainerRunning(cid);

        if (!reported && currentTimeMillis() - start > 5000) {
            reported = true;
            log.warn("Waiting for ports {} to open in container {}", ports, cid);
        }

        MILLISECONDS.sleep(200);
    }
    return false;
  }

  private Set<Integer> extractPorts(List<String> parameters) {
    return parameters.stream()
                        .map(this::convertToPort)
                        .collect(Collectors.toSet());
  }

  public int convertToPort(String portToConvert){
    try {
      int port = Integer.valueOf(portToConvert);
      
      if(port < MIN_PORT_RANGE || port > MAX_PORT_RANGE) new IllegalArgumentException(portToConvert + " is not a valid port");
        
      return port;
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(portToConvert + " is not a valid port");
    }
    
  }
  
  public static Set<Integer> readListenPorts(String output) {
    try(Scanner scanner = new Scanner(output)) { 
      scanner.useRadix(16).useDelimiter("[\\s:]+");
      Set<Integer> result = new HashSet<>();
      if (scanner.hasNextLine())
          scanner.nextLine();

      while (scanner.hasNextLine()) {
          scanner.nextInt();
          scanner.next();
          int localPort = scanner.nextInt();
          result.add(localPort);
          scanner.nextLine();
      }
      return result;
    }
  }

  
}
