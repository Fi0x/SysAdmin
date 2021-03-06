package unimessenger.abstraction.interfaces.wire;

import unimessenger.abstraction.interfaces.IMessages;
import unimessenger.abstraction.storage.Message;
import unimessenger.abstraction.storage.WireStorage;
import unimessenger.abstraction.wire.crypto.MessageCreator;
import unimessenger.abstraction.wire.structures.WireConversation;
import unimessenger.userinteraction.tui.Out;

import java.io.File;
import java.sql.Timestamp;
import java.util.UUID;

public class WireMessages implements IMessages
{
    private final WireMessageSender sender = new WireMessageSender();
    private final WireMessageReceiver receiver = new WireMessageReceiver();

    @Override
    public boolean sendTextMessage(String chatID, String text)
    {
        WireConversation conversation = WireStorage.getConversationByID(chatID);
        if(conversation == null)
        {
            Out.newBuilder("ConversationID not found").origin(this.getClass().getName()).d().WARNING().print();
            return false;
        }
        conversation.addMessage(new Message(text, new Timestamp(System.currentTimeMillis() - (1000 * 60 * 60)), WireStorage.selfProfile.userName));
        return sender.sendMessage(chatID, MessageCreator.createGenericTextMessage(text));
    }
    @Override
    public boolean sendTimedText(String chatID, String text, long millis)
    {
        WireConversation conversation = WireStorage.getConversationByID(chatID);
        if(conversation == null)
        {
            Out.newBuilder("ConversationID not found").origin(this.getClass().getName()).d().WARNING().print();
            return false;
        }
        conversation.addMessage(new Message(text, new Timestamp(System.currentTimeMillis() - (1000 * 60 * 60)), WireStorage.selfProfile.userName, millis));
        return sender.sendMessage(chatID, MessageCreator.createGenericTimedMessage(text, millis));
    }
    @Override
    public boolean sendFile(String chatID, File file)
    {
        WireConversation conversation = WireStorage.getConversationByID(chatID);
        if(conversation != null) conversation.addMessage(new Message("FILE", new Timestamp(System.currentTimeMillis()), WireStorage.selfProfile.userName));
        else
        {
            Out.newBuilder("ConversationID not found").origin(this.getClass().getName()).d().WARNING().print();
            return false;
        }
        UUID id = UUID.randomUUID();
        boolean previewSent = sender.sendMessage(chatID, MessageCreator.createGenericFilePreviewMessage(file, id));
        boolean assetSent = sender.sendMessage(chatID, MessageCreator.createGenericFileMessage(file, id));
        return previewSent && assetSent;
    }

    @Override
    public boolean receiveNewMessages()
    {
        return receiver.receiveNewMessages();
    }
}