package com.coolspy3.csonlinefriends;

import com.coolspy3.csmodloader.network.SubscribeToPacketStream;
import com.coolspy3.cspackets.packets.ClientChatSendPacket;

public class ListFriendsCommand
{

    @SubscribeToPacketStream
    public boolean register(ClientChatSendPacket event)
    {
        if (event.msg.matches("/of( .*)?"))
        {
            CSOnlineFriends.list();

            return true;
        }

        return false;
    }

}
