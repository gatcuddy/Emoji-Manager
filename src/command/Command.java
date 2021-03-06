package command;

import java.io.IOException;
import java.util.ArrayList;

import discordapi.Shard;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

abstract public class Command{
	
	public  String trigger;
	public boolean restricted, deleteCommand;
	public    long userID;
	
	public ArrayList<String> args;
	public MessageReceivedEvent event;
	public Shard shard;
	
	public static final boolean DELETECOMMAND = true;
	public static final boolean KEEPCOMMAND = false;
	
	//constructor for restricted command
	public Command(String trig, boolean del, long id) {
		trigger = trig;
		restricted = true;
		userID = id;
		deleteCommand = del;
	}
	//constructor for unrestricted command
	public Command(String trig, boolean del) {
		trigger = trig;
		restricted = false;
		deleteCommand = del;
	}
	
	abstract public void subRun() throws Exception;
	
	public void run(Shard s, MessageReceivedEvent e, ArrayList<String> in) {
		args  = in;
		shard =  s;
		event =  e;
		if(restricted && e.getAuthor().getIdLong()!=userID) {
			tempSay("You're not authorized to run this command!");
		}else {
			try {
				subRun();
			}catch(IOException ex) {
				tempSay("IO Exception!");
			}catch(NumberFormatException ex) {
				tempSay("That's not a number!");
			}catch(EmoteNotFoundException ex) {
				tempSay("There's no emoji named "+ex.errorName+"!");
			}catch(NoEmotesException ex) {
				tempSay("There are no custom emoji on this server!");
			}catch(CommandArgumentException ex) {
				tempSay("Argument Exception! No argument at position "+ex.errorPosition);
			}catch(Exception ex) {
				tempSay("There was an unexpected error...");
				ex.printStackTrace();
			}
			
			if(deleteCommand) {
				event.getChannel().deleteMessageById(e.getMessage().getIdLong()).complete();
			}
		}
	}
	
	public void say(String mes) {
		event.getChannel().sendMessage(mes).complete();
	}
	
	public void tempSay(String mes) {
		event.getChannel().sendMessage(mes).complete();
		shard.shouldDelete = true;
	}
	
	//when there is no argument at a crucial position
	public class CommandArgumentException extends Exception{
		private static final long serialVersionUID = -4343508984231209791L;
		
		int errorPosition;
		
		public CommandArgumentException(int argumentIndex) {
			errorPosition = argumentIndex;
		}
	}
	
	//when the server has no emotes
	public class NoEmotesException extends Exception{
		private static final long serialVersionUID = -434350898431209791L;
	}
	
	//when trying to access a nonexistant emote
	public class EmoteNotFoundException extends Exception{
		private static final long serialVersionUID = -434350898423120971L;
		
		String errorName;
		
		public EmoteNotFoundException(String EmojiName) {
			errorName = EmojiName;
		}
	}
	
	public int findEmote(String emojiName) throws EmoteNotFoundException{
		for(int i=0;i<shard.numEmotes;i++) {
			String compare = shard.emotes.get(i).getName();
			if(emojiName.equals(compare)) {
				return i;
			}
		}
		throw new EmoteNotFoundException(emojiName);
	}
	
	public Emote findActualEmote(String emoteName) throws EmoteNotFoundException{
		for(int i=0;i<shard.numEmotes;i++) {
			String compare = shard.emotes.get(i).getName();
			if(emoteName.equals(compare)) {
				return shard.emotes.get(i);
			}
		}
		throw new EmoteNotFoundException(emoteName);
	}
	
	//returns the specified argument, throws exception if it can't find and argument
	public String getArgument(int argIndex) throws CommandArgumentException {
		try {
			return args.get(argIndex);
		}catch(IndexOutOfBoundsException ex) {
			throw new CommandArgumentException(argIndex);
		}
	}
}
