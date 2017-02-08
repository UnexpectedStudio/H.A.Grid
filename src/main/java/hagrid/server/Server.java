package hagrid.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketAdapter;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import hagrid.Main;
import hagrid.Player;
import hagrid.config.Save;
import hagrid.config.Word;

public class Server extends WebSocketServer {
    private static Server instance;
    private static final Random rand = new Random();
    private final List<Player> mPlayers = new ArrayList<>();

    public Server(InetSocketAddress address) {
        super(address);
        setWebSocketFactory(new WebSocketServerFactory() {
            
            public SocketChannel wrapChannel(SocketChannel channel, SelectionKey key)
                    throws IOException {
                return (SocketChannel) channel;
            }
            
            public WebSocketImpl createWebSocket(WebSocketAdapter a,
                    List<Draft> d, Socket s) {
                return new Player(a, d);
            }
            
            public WebSocketImpl createWebSocket(WebSocketAdapter a, Draft d,
                    Socket s) {
                return new Player(a, d);
            }
        });
        instance = this;
    }
    
    public static Server getInstance() {
        return instance;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
		Player p = (Player) conn;
    	synchronized(mPlayers) {
    		p.ip = p.getRemoteSocketAddress().getAddress().getHostAddress();
    		Save.Player saved = null;
    		synchronized(Main.save) {
    			saved = Main.save.players.get(p.ip);
    		}
    		if (saved != null) {
    			p.team = saved.team;
    			p.setWords(saved.usedWords);
	    		String firstMessage = p.team == Player.Team.Andromeda ? Main.config.andromeda.firstMessage : Main.config.hacker.firstMessage;
	    		p.send("TEAM " + p.team.name().toLowerCase());
	    		p.send("MSG " + firstMessage);
    		}
    		mPlayers.add(p);
    	}
    	synchronized(Main.save) {
    		p.send("PERCENT " + Main.save.percent);
    	}
    	synchronized(Main.config) {
			p.send("END " + Main.config.endTime);
		}
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason,
            boolean remote) {
        synchronized(mPlayers) {
            mPlayers.remove((Player)conn);
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Player p = (Player) conn;
    	System.out.println("< " + p.ip + " " + p.team + ": " + message);
    	synchronized(Main.config) {
	    	if (p.team == Player.Team.None) {
	    		if (message.equals("hacker")) p.team = Player.Team.Hacker;
	    		else if (message.equals("andromeda")) p.team = Player.Team.Andromeda;
				else {
					p.send("ALERT Team not found.");
					return;
				}
	    		String firstMessage = p.team == Player.Team.Andromeda ? Main.config.andromeda.firstMessage : Main.config.hacker.firstMessage;
	    		p.send("TEAM " + p.team.name().toLowerCase());
	    		p.send("MSG " + firstMessage);
	    		savePlayer(p);
	    	} else {
	    		String[] wordList = p.team == Player.Team.Andromeda ? Main.config.andromeda.words : Main.config.hacker.words;
	    		String[] badWordList = p.team != Player.Team.Andromeda ? Main.config.andromeda.words : Main.config.hacker.words;
	    		String[] found = p.team == Player.Team.Andromeda ? Main.config.andromeda.found : Main.config.hacker.found;
	    		String[] notFound = p.team == Player.Team.Andromeda ? Main.config.andromeda.notFound : Main.config.hacker.notFound;
	    		
	    		String word = getWord(wordList, message);
	    		Word savedWord = p.getWord(message);
	    		if (getWord(badWordList, message) != null) {
	    			String toSend = "MSG <font color='red'>";
	    			toSend += p.team == Player.Team.Andromeda ? "Andromeda> " : "Hacker> ";
	    			toSend += "TRAITRE.</font>";
	    			p.send(toSend);
	    			return;
	    		}
	    		if (word == null || (savedWord != null && System.currentTimeMillis() < savedWord.timeoutTimestamp)) {
	    			p.send("MSG " + getRandom(notFound));
	    			return;
	    		}
	    		
	    		float currentPercent = 0;
	    		synchronized(Main.save) {
	    			currentPercent = Main.save.percent;
		    		
		    		if (p.team == Player.Team.Andromeda && currentPercent >= 100) return;
		    		if (p.team == Player.Team.Hacker && currentPercent <= 0) return;
		    		
		    		currentPercent += (p.team == Player.Team.Andromeda ? Main.config.percentChange : -Main.config.percentChange);
		    		
		    		p.addToList(new Word(word, System.currentTimeMillis() + Main.config.wordTimeout));
		    		
		    		if (currentPercent > 100) currentPercent = 100;
		    		if (currentPercent < 0) currentPercent = 0;
		    		
	    			Main.save.percent = currentPercent;
	    		}
	    		
	    		savePlayer(p);
	    		p.send("MSG " + getRandom(found));
	    		sendToAll("PERCENT " + currentPercent);
	    	}
    	}
    }
    
    private void savePlayer(Player p) {
    	synchronized(Main.save) {
    		Main.save.players.put(p.ip, new Save.Player(p.getWords(), p.team));
    		Main.save.save(Main.saveFile);
    	}
    }
    
    private String getRandom(String[] words) {
    	return words[rand.nextInt(words.length)];
    }
    
    private String getWord(String[] wordList, String toCheck) {
    	for (String word : wordList) if (word.equals(toCheck)) return word;
    	return null;
    }
    
    public void sendToAll(String text) {
        synchronized(mPlayers) {
            for (Player p : mPlayers) {
                try {
                    p.send(text);
                } catch (Exception e) {}
            }
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }
}
