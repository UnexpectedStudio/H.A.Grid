package hagrid;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

import hagrid.config.Config;
import hagrid.config.JsonConfiguration;
import hagrid.config.Save;
import hagrid.server.Server;

public class Main {
    private static File configFile;
    public static Config config = new Config();
    public static File saveFile = new File("save.json");
    public static Save save = new Save();
    
    public static void main(String[] args)
            throws Exception {
        Server s = new Server(new InetSocketAddress(42043));

        System.out.println("Starting server...");
        s.start();
        
        configFile = new File("config.json");
        reload();
        boolean running = true;
        while (running) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String str = br.readLine();
            if (str.equalsIgnoreCase("stop")) running = false;
            else if (str.equalsIgnoreCase("reload")) {
                try {
                    reload();
                } catch (Exception e) {
                    System.out.println("CANNOT RELOAD.");
                    e.printStackTrace();
                }
            } else if (str.startsWith("msg")) {
                try {
                    s.sendToAll("MSG " + str.substring(4));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (str.startsWith("percent")) {
            	try {
            		float percent = 0;
            		synchronized(save) {
            			save.percent = Float.parseFloat(str.substring(8));
            			percent = save.percent;
            			save.save(saveFile);
            		}
                    s.sendToAll("PERCENT " + percent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (str.startsWith("endtime")) {
            	try {
            		long endTimer = 0;
            		synchronized(Main.config) {
            			Main.config.endTime = Long.parseLong(str.substring(8));
            			endTimer = Main.config.endTime;
            			Main.config.save(saveFile);
            		}
                    s.sendToAll("END " + endTimer);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Stopping in 5s...");
        Thread.sleep(5000);
        s.stop();
        System.out.println("Stopped.");
    }
    
    private static void reload() throws Exception {
        System.out.println("Reloading...");
        System.out.println("Reloading configs...");
        synchronized(config) {
            if (!configFile.exists()) {
                configFile.createNewFile();
                config.save(configFile);
            }
            config = JsonConfiguration.load(configFile, Config.class);
        }
        System.out.println("Done.");
        
        System.out.println("Reloading saves...");
        synchronized(save) {
        	if (!saveFile.exists()) {
        		saveFile.createNewFile();
        		save.save(saveFile);
        	}
        	save = JsonConfiguration.load(saveFile, Save.class);
        }
        System.out.println("Done.");
        System.out.println("Reload ended.");
    }
}
