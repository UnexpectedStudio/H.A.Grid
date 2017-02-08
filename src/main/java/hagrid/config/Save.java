package hagrid.config;

import java.util.HashMap;
import java.util.Map;

public class Save extends JsonConfiguration {
	public Map<String, Player> players = new HashMap<>();
	public float percent = 100;
	
	public static class Player {
		public Word[] usedWords;
		public hagrid.Player.Team team;
		
		public Player(Word[] words, hagrid.Player.Team team) {
			usedWords = words;
			this.team = team;
		}
	}
}
