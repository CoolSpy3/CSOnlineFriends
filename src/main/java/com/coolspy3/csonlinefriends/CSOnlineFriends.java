package com.coolspy3.csonlinefriends;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import com.coolspy3.csmodloader.GameArgs;
import com.coolspy3.csmodloader.interfaces.ExceptionSupplier;
import com.coolspy3.csmodloader.mod.Entrypoint;
import com.coolspy3.csmodloader.mod.Mod;
import com.coolspy3.csmodloader.network.PacketHandler;
import com.coolspy3.csmodloader.network.SubscribeToPacketStream;
import com.coolspy3.csmodloader.util.Utils;
import com.coolspy3.cspackets.datatypes.MCColor;
import com.coolspy3.hypixelapi.APIConfig;
import com.coolspy3.util.ModUtil;
import com.coolspy3.util.ServerJoinEvent;

import me.kbrewster.exceptions.APIException;
import me.kbrewster.mojangapi.MojangAPI;
import net.hypixel.api.HypixelAPI;
import net.hypixel.api.reply.FriendsReply;
import net.hypixel.api.reply.FriendsReply.FriendShip;
import net.hypixel.api.reply.StatusReply.Session;

@Mod(id = "csonlinefriends", name = "CSOnlineFriends",
        description = "Provides listing of currently online Hypixel friends.", version = "2.0.0",
        dependencies = {"csmodloader:[1,2)", "cspackets:[1,2)", "csutils:[1,2)",
                "cshypixelapi:[1.1,2)"})
public class CSOnlineFriends implements Entrypoint
{

    @Override
    public void init(PacketHandler handler)
    {
        handler.register(this);
        handler.register(new ListFriendsCommand());
    }

    @SubscribeToPacketStream
    public void onServerJoined(ServerJoinEvent event)
    {
        list();
    }

    public static void list()
    {
        ModUtil.executeAsync(() -> {
            HypixelAPI api = Utils.reporting((ExceptionSupplier<HypixelAPI>) APIConfig::requireAPI);

            if (api == null) return;

            UUID playerUUID = GameArgs.get().uuid;
            FriendsReply friends = api.getFriends(playerUUID).join();
            ArrayList<String> onlineFriends = new ArrayList<>();
            for (FriendShip friendship : friends.getFriendShips())
            {
                UUID other =
                        friendship.getUuidSender().equals(playerUUID) ? friendship.getUuidReceiver()
                                : friendship.getUuidSender();
                Session status = api.getStatus(other).join().getSession();
                if (status.isOnline())
                {
                    try
                    {
                        onlineFriends.add(MCColor.YELLOW + MojangAPI.getName(other) + " - "
                                + status.getServerType().getName() + "_" + status.getMode() + "_"
                                + status.getMap());
                    }
                    catch (APIException | IOException e)
                    {
                        onlineFriends.add(MCColor.YELLOW + other.toString());
                    }
                }
            }
            ModUtil.sendMessage(MCColor.AQUA + "Online Friends:");
            if (onlineFriends.isEmpty())
            {
                ModUtil.sendMessage(MCColor.YELLOW + "<None>");
            }
            else
            {
                for (String friend : onlineFriends)
                {
                    ModUtil.sendMessage(friend);
                }
            }
        });
    }

}
