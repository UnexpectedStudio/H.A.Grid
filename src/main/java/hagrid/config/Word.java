package hagrid.config;

public class Word {
	public String word;
	public long timeoutTimestamp;
	
	public Word(String word, long timeout) {
		this.word = word;
		timeoutTimestamp = timeout;
	}
}
