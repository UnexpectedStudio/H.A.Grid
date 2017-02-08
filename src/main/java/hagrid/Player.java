package hagrid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.java_websocket.WebSocketAdapter;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.WebSocketListener;
import org.java_websocket.drafts.Draft;

import hagrid.config.Word;

public class Player extends WebSocketImpl {
	public enum Team {
		Andromeda,
		Hacker,
		None
	}
	public Team team = Team.None;
	private List<Word> mWordList = new ArrayList<Word>();
	public String ip;

    public Player(WebSocketListener listener, List<Draft> drafts) {
        super(listener, drafts);
    }

    public Player(WebSocketAdapter a, Draft d) {
        super(a, d);
    }
    
    public void addToList(Word word) {
    	mWordList.add(word);
    }
    
    public Word getWord(String wordToCheck) {
    	for (Word word : mWordList) if (word.word.equals(wordToCheck)) return word;
    	return null;
    }
    
    public Word[] getWords() {
    	return mWordList.toArray(new Word[mWordList.size()]);
    }

	public void setWords(Word[] usedWords) {
		mWordList = new ArrayList<>(Arrays.asList(usedWords));
	}
	
	public void send(String s) {
    	System.out.println("> " + ip + " " + team + ": " + s);
    	super.send(s);
	}
}
