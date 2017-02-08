package hagrid.config;

public class Config extends JsonConfiguration {
	public Entity andromeda;
	public Entity hacker;
	public float percentChange;
	public long wordTimeout;
	public long endTime;
    
    public static class Entity {
    	public String[] words = new String[] {};
    	public String[] notFound = new String[] {};
    	public String[] found = new String[] {};
    	public String firstMessage = "";
    }
}
