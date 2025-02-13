package handmadeguns.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;

import java.io.*;

public class PacketPlaysound implements IMessage {
    public int shooterid;
    public String sound;
    public float speed;
    public float level;
    public int time = -1;
    public boolean hasCustomPos;
    public double posX;
    public double posY;
    public double posZ;

    public boolean isreload = false;

    public PacketPlaysound() {
    }

    public PacketPlaysound(Entity shootentity, String so, float sp, float lv) {
        shooterid = shootentity.getEntityId();
        sound = so;
        speed = sp;
        level = lv;
    }

    public PacketPlaysound(Entity shootentity, String so, float sp, float lv, boolean isreload) {
        this(shootentity, so, sp, lv);
        this.isreload = isreload;
    }

    public PacketPlaysound(Entity shootentity, String so, float sp, float lv,
                           double primePosX,
                           double primePosY,
                           double primePosZ)
    {
        this(shootentity,so,sp,lv);
        hasCustomPos = true;
        posX = primePosX;
        posY = primePosY;
        posZ = primePosZ;
    }
    public PacketPlaysound(Entity shootentity, String so,float sp,float lv,int time){
        this(shootentity,so,sp,lv);
        this.time = time;
    }


    @Override
    public void toBytes(ByteBuf buffer)
    {
        buffer.writeInt(shooterid);
        buffer.writeInt(time);
        try {
            byte[] soundname = fromObject(this.sound);
            buffer.writeInt(soundname.length);
            buffer.writeBytes(soundname);
        } catch (IOException e) {
            e.printStackTrace();
        }
        buffer.writeFloat(speed);
        buffer.writeFloat(level);
        buffer.writeBoolean(isreload);
        buffer.writeBoolean(hasCustomPos);
        if(hasCustomPos){
            buffer.writeDouble(posX);
            buffer.writeDouble(posY);
            buffer.writeDouble(posZ);
        }
//        System.out.println("debug");
    }
    @Override
    public void fromBytes(ByteBuf buffer)
    {
        shooterid = buffer.readInt();
        time = buffer.readInt();
        byte[] temp = new byte[buffer.readInt()];
        buffer.readBytes(temp);
        try {
            this.sound = (String)toObject(temp);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        speed = buffer.readFloat();
        level = buffer.readFloat();
        isreload = buffer.readBoolean();
        hasCustomPos = buffer.readBoolean();
        if(hasCustomPos){
            posX = buffer.readDouble();
            posY = buffer.readDouble();
            posZ = buffer.readDouble();
        }
    }

    public static byte[] fromObject(Object o) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeObject(o);
        byte[] bytes = bos.toByteArray();
        out.close();
        bos.close();
        return bytes;
    }
    public static Object toObject(byte[] bytes) throws OptionalDataException, StreamCorruptedException, ClassNotFoundException, IOException{
        return new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject();
    }
}
